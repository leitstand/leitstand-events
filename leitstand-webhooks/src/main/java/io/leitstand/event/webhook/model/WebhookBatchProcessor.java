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

import static io.leitstand.commons.model.ByteArrayUtil.encodeBase64String;
import static io.leitstand.commons.model.StringUtil.isNonEmptyString;
import static io.leitstand.commons.model.StringUtil.toUtf8Bytes;
import static io.leitstand.event.webhook.service.WebhookSettings.HttpMethod.PUT;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static javax.ws.rs.client.ClientBuilder.newClient;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;

import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;

public class WebhookBatchProcessor implements Runnable {
	
	private static final Logger LOG = Logger.getLogger(WebhookBatchProcessor.class.getName());

	private WebhookBatch 	 batch;
	private WebhookEventLoop loop;
	
	public WebhookBatchProcessor(WebhookEventLoop loop,
								 WebhookBatch batch) {
		this.loop  = loop;
		this.batch = batch;
	}
	
	
	@Override
	public void run() {
		List<WebhookInvocation> invocations = batch.getWebhookInvocations();
		for(WebhookInvocation invocation : invocations) {
			call(newClient(),invocation);
		}
	}				
	
	boolean call(Client client, WebhookInvocation invocation) {
				
		try {
			Builder request = client.target(invocation.getEndpoint().toUri())
								    .request();
			authenticate(request);
			return execute(request, invocation);				
		} catch (Exception e) {
			LOG.warning(() -> format("%s webhook invocation (%s %s) for %s (%s) failed: %s", 
									 batch.getWebhookName(),
									 batch.getMethod(),
									 invocation.getEndpoint(),
									 invocation.getEventName(),
									 invocation.getEventId(),
									 e.getMessage()));
			return false;
		} 
	}


	private boolean execute(Builder call, WebhookInvocation invocation) {
		long start = currentTimeMillis();
		Response response = invokeWebhook(call, invocation);
		LOG.info(() -> format("%s webhook invocation (%s %s) for %s (%s) completed with reason code %d (%s)", 
					    	  batch.getWebhookName(),
					    	  batch.getMethod(),
					    	  invocation.getEndpoint(),
					    	  invocation.getEventName(),
					    	  invocation.getEventId(),
					    	  response.getStatus(),
					    	  response.getStatusInfo().getReasonPhrase()));
		
		if(response.getStatusInfo().getFamily() == SUCCESSFUL) {
			loop.webhookSucceeded(batch,
								  invocation,
								  response.getStatusInfo(),
								  start);
			return true;
		} 
		
		loop.webhookFailed(batch,
						   invocation,
						   response.getStatusInfo(),
						   start);
		return false;
	}


	void authenticate(Builder call) {
		if(batch.getUserName() != null) {
			call.header("Authorization", "Basic "+encodeBase64String(toUtf8Bytes(batch.getUserName()+":"+new String(batch.getPassword().getValue()))));
			return;
		} 
		if (isNonEmptyString(batch.getAccesskey())) {
			call.header("Authorization", "Bearer "+batch.getAccesskey());
			return;
		}
	}

	Response invokeWebhook(Builder call, WebhookInvocation invocation) {
		if(batch.getMethod() == PUT) {
			return call.put(entity(invocation.getMessage(),
								   invocation.getContentType()));
		} 
		return call.post(entity(invocation.getMessage(),
								invocation.getContentType()));
	}
	
	WebhookBatch getBatch() {
		return batch;
	}

	
}

