/*
 * Copyright 2020 RtBrick Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.leitstand.event.webhook.model;

import static io.leitstand.commons.etc.Environment.getSystemProperty;
import static io.leitstand.event.webhook.service.ReasonCode.WHK0100I_WEBHOOK_EVENT_LOOP_STARTED;
import static io.leitstand.event.webhook.service.ReasonCode.WHK0101I_WEBHOOK_EVENT_LOOP_STOPPED;
import static io.leitstand.event.webhook.service.WebhookEventLoopStatus.newWebhookEventLoopStatus;
import static java.lang.Integer.parseInt;
import static java.lang.Math.min;
import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINER;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response.StatusType;

import io.leitstand.commons.ShutdownListener;
import io.leitstand.commons.StartupListener;
import io.leitstand.event.webhook.service.WebhookEventLoopService;
import io.leitstand.event.webhook.service.WebhookEventLoopStatus;
import io.leitstand.event.webhook.service.WebhookService;

@ApplicationScoped
public class WebhookEventLoop implements Runnable, StartupListener, ShutdownListener, WebhookEventLoopService{
	
	private static final Logger LOG = Logger.getLogger(WebhookEventLoop.class.getName());
	private static final int THREADS = parseInt(getSystemProperty("leitstand.webhook.event.loop.threads", "10"));

	
	private volatile boolean enabled;
	private Date lastModified;
	
	@Resource
	private ManagedExecutorService wm;
	
	@Inject
	private WebhookInvocationService service;
	
	private Semaphore permits;
	
	@PostConstruct
	void initThreads() {
		this.permits = new Semaphore(THREADS);
	}
	
	/**
	 * Starts the webhook event loop when leitstand is started.
	 */
	@Override
	public void onStartup() {
		startEventLoop();
	}

	/**
	 * Stops the webhook event loop when leitstand is stopped.
	 */
	@Override
	public void onShutdown() {
		stopEventLoop();
	}

	/**
	 * Starts the webhook event loop to process all domain event and forward them to the configured webhooks and their respective endpoint.
	 */
	public void startEventLoop() {
		if(!enabled) {
			lastModified = new Date();
			enabled = true;
			try {
				wm.execute(this);
			} catch (Exception e) {
				LOG.severe("Unable to start webhook event loop: "+e);
				LOG.log(FINER,e.getMessage(),e);
			}
		}
	}

	/**
	 * Stops the webhook event loop.
	 * This effects all configured webhooks as no domain events are processed any longer. 
	 * An alternative is to disable webhooks itself.
	 * @see Webhook#enable()
	 * @see Webhook#disable()
	 * @see WebhookService#enableWebhook(io.leitstand.event.webhook.service.WebhookId)
	 * @see WebhookService#enableWebhook(io.leitstand.event.webhook.service.WebhookName)
	 * @see WebhookService#disableWebhook(io.leitstand.event.webhook.service.WebhookId)
	 * @see WebhookService#disableWebhook(io.leitstand.event.webhook.service.WebhookName)
	 */
	public void stopEventLoop() {
		lastModified = new Date();
		this.enabled = false;
	}
	
	/**
	 * Returns whether the event loop is active or not.
	 * @return <code>true</code> if this event loop is active, <code>false</code> if not.
	 */
	public boolean isEnabled() {
		return enabled;
	}
	
	/**
	 * Runs the webhook event loop.
	 * Queries the database for domain events.
	 * Sleeps for an increasing interval with a ceiling of 180seconds if no messages exists.
	 * The sleeping interval is reset whenever at lease a single message was read.
	 */
	@Override
	public void run() {
		LOG.info(() -> format("%s: Webhook event loop started.",
							  WHK0100I_WEBHOOK_EVENT_LOOP_STARTED.getReasonCode()));
		
		while(enabled) {
			
			service.populateWebhookQueues();
			
			Queue<WebhookBatch> batches = new LinkedList<>(batches());
			
			while(!batches.isEmpty()) {
				WebhookBatch batch = batches.poll();
				try {
					permits.acquire();
					scheduleWebhookBatch(batch);
				} catch(InterruptedException e) {
					LOG.fine(() -> format("Wait for webhook execution permit was interrupted. %d pending invocations. %d available permits",
										  batches.size(),
										  permits.availablePermits()));
					// Restore interrupt status and keep message loop alive.
					currentThread().interrupt();
				}
			}
		}
		
		LOG.info(() -> format("%s: Webhook event loop stopped.",
							  WHK0101I_WEBHOOK_EVENT_LOOP_STOPPED));
	}

	private void scheduleWebhookBatch(WebhookBatch batch) {
		try {
			wm.execute(new WebhookBatchProcessor(this,
												 batch));
			LOG.fine(() -> format("Successfully scheduled batch with %d invocation(s) for webhook %s.",
					 			  batch.getWebhookInvocations().size(),
								  batch.getWebhookName()));
		} catch (Exception e) {
			LOG.warning(() -> format("Failed to schedule batch with %d invocation(s) for webhook %s: %s",
									batch.getWebhookInvocations().size(),
									batch.getWebhookName(),
									e.getMessage()));
			LOG.log(FINE,e.getMessage(),e);
		}
	}

	List<WebhookBatch> batches(){
		List<WebhookBatch> batches = service.findInvocations();
		long waittime = 1;
		while(batches.isEmpty()) {
			try {
				final long logwaittime = waittime;
				LOG.fine(() -> format("No events to be processed. Sleep for %d seconds before polling for new events",logwaittime));
				sleep(TimeUnit.SECONDS.toMillis(waittime));
				batches = service.findInvocations();
				// Wait time shall never exceed a minute (if no messages are there at all).
				waittime = min(2*waittime, 60);
			} catch (InterruptedException e) {
				LOG.fine(() -> "Wait for domain events has been interrupted. Reset wait interval and proceed polling!");
				waittime = 1;
				// Restore interrupt status.
				currentThread().interrupt();
			}
		}
		return batches;
	}

	/**
	 * Notifies the event loop about a successful webhook invocation and releases the thread execution permit.
	 * @param batch the webhook batch job
	 * @param invocation the webhook invocation
	 * @param status the HTTP success family status code (e.g <code>200</code>, <code>201</code>, <code>202</code> or <code>204</code>)
	 */
	void webhookSucceeded(WebhookBatch batch, 
						  WebhookInvocation invocation,
						  StatusType status,
						  long startUnixEpoch) {
		service.invocationSucceeded(batch,
								    invocation,
									status,
									startUnixEpoch);
	}

	/**
	 * Notifies the event loop about a failed webhook invocation and releases the thread execution permit.
	 * @param batch the webhook batch job
	 * @param invocation the webhook invocation
	 * @param status the HTTP client or server error status (e.g. <code>400</code>, <code>401</code>, <code>403</code>, <code>404</code> or <code>500</code>)
	 */
	void webhookFailed(WebhookBatch batch,
					   WebhookInvocation invocation,
					   StatusType status,
					   long startUnixEpoch) {
		service.invocationFailed(batch,
								 invocation,
								 status,
								 startUnixEpoch);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WebhookEventLoopStatus getWebhookEventLoopStatus() {
		return newWebhookEventLoopStatus()
			   .withEnabled(enabled)
			   .withDateModified(lastModified)
			   .build();
	}
	
}

