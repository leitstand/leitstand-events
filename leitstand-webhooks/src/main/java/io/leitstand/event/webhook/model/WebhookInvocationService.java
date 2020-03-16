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

import static io.leitstand.commons.db.DatabaseService.prepare;
import static io.leitstand.commons.jpa.SerializableJsonObjectConverter.parseJson;
import static io.leitstand.commons.json.NullSafeJsonObjectBuilder.createNullSafeJsonObjectBuilder;
import static io.leitstand.commons.jsonb.IsoDateAdapter.isoDateFormat;
import static io.leitstand.commons.model.ByteArrayUtil.decodeBase64String;
import static io.leitstand.commons.model.ObjectUtil.optional;
import static io.leitstand.commons.model.StringUtil.fromUtf8Bytes;
import static io.leitstand.commons.model.StringUtil.isNonEmptyString;
import static io.leitstand.event.queue.service.DomainEventId.domainEventId;
import static io.leitstand.event.queue.service.DomainEventName.domainEventName;
import static io.leitstand.event.webhook.model.Webhook.findAllEnabledWebhooks;
import static io.leitstand.event.webhook.model.WebhookBatch.newWebhookBatch;
import static io.leitstand.event.webhook.model.WebhookInvocation.newWebhookInvocation;
import static io.leitstand.event.webhook.service.MessageState.FAILED;
import static io.leitstand.event.webhook.service.MessageState.PROCESSED;
import static io.leitstand.security.auth.UserName.userName;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.security.enterprise.credential.Password;
import javax.ws.rs.core.Response.StatusType;

import io.leitstand.commons.db.DatabaseService;
import io.leitstand.commons.model.Repository;
import io.leitstand.commons.model.Service;
import io.leitstand.security.auth.UserName;
import io.leitstand.security.crypto.MasterSecret;

@Service
public class WebhookInvocationService {
	
	private static final Logger LOG = Logger.getLogger(WebhookInvocationService.class.getName());

	@Inject
	@Webhooks
	private Repository repository;
	
	@Inject
	private WebhookRewritingService rewriter;
	
	@Inject
	@Webhooks
	private DatabaseService db;
	
	@Inject
	private MasterSecret secret;
	
	protected WebhookInvocationService() {
		// CDI
	}
	
	protected WebhookInvocationService(Repository repository, 
									   DatabaseService db,
									   MasterSecret secret,
									   WebhookRewritingService rewriter) {
		this.repository = repository;
		this.db = db;
		this.secret = secret;
		this.rewriter = rewriter;
	}
	
	public List<WebhookBatch> findInvocations(){
		List<WebhookBatch> batches = new LinkedList<>();
		for(Webhook webhook : repository.execute(findAllEnabledWebhooks())) {
			UserName userName = null;
			String password = null;
			String accesskey = null;
			if(webhook.isBasicAuthentication()) {
				userName = userName(webhook.getUser());
				password = fromUtf8Bytes(secret.decrypt(decodeBase64String(webhook.getPassword64())));
			} else if (isNonEmptyString(webhook.getAccessKey64())) {
				accesskey = fromUtf8Bytes(secret.decrypt(decodeBase64String(webhook.getAccessKey64())));
			}
			
			List<WebhookInvocation> invocations = db.executeQuery(prepare("WITH batch AS ( "+
																			   "UPDATE bus.webhook_message "+
																			   "SET state = 'IN_PROGRESS' "+
																			   "WHERE message_id "+
																			   "IN (SELECT message_id "+ 
																			   	   "FROM  bus.webhook_message "+ 
																			   	   "WHERE webhook_id = ? "+
																			   	   "AND state ='PENDING' "+
																			   	   "FOR UPDATE SKIP LOCKED "+
																			   	   "LIMIT ?) "+
																			   "RETURNING message_id) "+
																		  "SELECT m.id, m.uuid, m.name, m.correlationid, m.message "+
																		  "FROM bus.message m "+
																		  "WHERE m.id "+
																		  "IN (SELECT message_id "+ 
																		  	  "FROM batch)",
																		  webhook.getId(),
																		  webhook.getBatchSize()),
																  rs -> {
																	JsonObject jsonPayload =  parseJson(rs.getString(4));
																	JsonObject requestEntity = createNullSafeJsonObjectBuilder()
																					   		   .add("event_id",rs.getString(2))
																					   		   .add("event_name",rs.getString(3))
																					   		   .add("correlation_id",rs.getString(4))
																					   		   .add("message",jsonPayload)
																					   		   .add("topic_name", webhook.getTopicName().toString())
																					   		   .add("date_created",isoDateFormat(rs.getTimestamp(5)))
																					   		   .build();	
		
																	return newWebhookInvocation()
																		   .withEndpoint(rewriter.rewriteEndpoint(webhook, requestEntity))
																		   .withDomainEventId(domainEventId(rs.getString(2)))
																		   .withDomainEventName(domainEventName(rs.getString(3)))
																		   .withMessagePK(rs.getLong(1))
																		   .withContentType(webhook.getContentType())
																		   .withMessage(rewriter.rewritePayload(webhook, requestEntity))
																		   .build();
															
														});
			
			
			LOG.fine(() -> format("%d invocations loaded fetched for webhook %s",
								  invocations.size(),
								  webhook.getWebhookName()));
			
			if(!invocations.isEmpty()) {
				batches.add(newWebhookBatch()
							.withWebhookPK(webhook.getId())
							.withWebhookId(webhook.getWebhookId())
							.withWebhookName(webhook.getWebhookName())
							.withHttpMethod(webhook.getHttpMethod())
							.withUserName(userName)
							.withPassword(optional(password, Password::new))
							.withAccesskey(accesskey)
							.withWebhookInvocations(invocations)
							.build());
			}
			
		}
		
		return batches;
	}
	
	public void invocationSucceeded(WebhookBatch batch, 
									WebhookInvocation invocation, 
									StatusType status,
									long start) {
		long duration = currentTimeMillis() - start;
		LOG.info(() -> format("%s message (%s) was successfully processed by webhook %s (%s %s) in %.3f seconds. Returned status was %d %s",
							  invocation.getEventName(),
							  invocation.getEventId(),
							  batch.getWebhookName(),
							  batch.getMethod(),
							  invocation.getEndpoint(),
							  duration/1000d,
							  status.getStatusCode(),
							  status.getReasonPhrase()));
		
		Webhook_Message message = repository.find(Webhook_Message.class, 
												  new Webhook_MessagePK(batch.getWebhookPK(), invocation.getMessagePK()));
		message.setHttpStatus(status.getStatusCode());
		message.setExecTime(duration);
		message.setMessageState(PROCESSED);	

	}

	public void invocationFailed(WebhookBatch batch, 
								 WebhookInvocation invocation, 
								 StatusType status, 
								 long start) {
		long duration = currentTimeMillis() - start;
		
		LOG.warning(() -> format("%s message (%s) processing by webhook %s (%s %s) failed after %.3f seconds with status %d %s",
						   		 invocation.getEventName(),
						   		 invocation.getEventId(),
						   		 batch.getWebhookName(),
						   		 batch.getMethod(),
						   		 invocation.getEndpoint(),
						   		 duration/1000d,
						   		 status.getStatusCode(),
						   		 status.getReasonPhrase()));

		Webhook_Message message = repository.find(Webhook_Message.class, 
												  new Webhook_MessagePK(batch.getWebhookPK(), invocation.getMessagePK()));
		message.setHttpStatus(status.getStatusCode());
		message.setExecTime(duration);
		message.setMessageState(FAILED);
	}

	public void invocationFailed(WebhookBatch batch, 
								 WebhookInvocation invocation, 
								 Exception cause,
								 long start) {
		long duration = currentTimeMillis() - start;
		LOG.warning(() -> format("%s message (%s) processing by webhook %s (%s %s) failed after %.3f seconds due to %s %s",
				   				 invocation.getEventName(),
				   				 invocation.getEventId(),
				   				 batch.getWebhookName(),
				   				 batch.getMethod(),
				   				 invocation.getEndpoint(),
				   				 duration/1000d,
				   				 cause.getClass().getSimpleName(),
				   				 cause.getMessage()));

		Webhook_Message message = repository.find(Webhook_Message.class, 
												  new Webhook_MessagePK(batch.getWebhookPK(), invocation.getMessagePK()));
		message.setMessageState(FAILED);
		message.setExecTime(duration);
	}

	public void populateWebhookQueues() {
		// Write all messages that occurred since the last execution to the webhook queues.
		int messages = db.executeUpdate(prepare("INSERT INTO bus.webhook_message (webhook_id, message_id, state) "+
												"SELECT w.id, m.id, 'NEW' "+
												"FROM bus.message m "+
												"JOIN bus.webhook w "+
												"ON m.topic_id = w.topic_id "+
												"WHERE NOT EXISTS (SELECT wm FROM bus.webhook_message wm WHERE wm.message_id = m.id) "+
												"AND (m.name ~ w.selector OR w.selector is null)"));
		LOG.fine(() -> format("%d messages added to the webhook queue",messages));
	}

}
