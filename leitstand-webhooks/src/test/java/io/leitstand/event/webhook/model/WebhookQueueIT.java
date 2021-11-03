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
import static io.leitstand.commons.etc.Environment.emptyEnvironment;
import static io.leitstand.event.queue.model.Topic.findTopicByName;
import static io.leitstand.event.queue.service.DomainEvent.newDomainEvent;
import static io.leitstand.event.queue.service.DomainEventId.randomDomainEventId;
import static io.leitstand.event.queue.service.DomainEventName.domainEventName;
import static io.leitstand.event.queue.service.TopicName.topicName;
import static io.leitstand.event.webhook.model.Webhook.findWebhookById;
import static io.leitstand.event.webhook.service.MessageFilter.newMessageFilter;
import static io.leitstand.event.webhook.service.MessageState.FAILED;
import static io.leitstand.event.webhook.service.MessageState.IN_PROGRESS;
import static io.leitstand.event.webhook.service.MessageState.PROCESSED;
import static io.leitstand.event.webhook.service.MessageState.READY;
import static io.leitstand.event.webhook.service.WebhookId.randomWebhookId;
import static io.leitstand.event.webhook.service.WebhookName.webhookName;
import static io.leitstand.testing.ut.LeitstandCoreMatchers.hasSizeOf;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import javax.persistence.EntityManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.leitstand.commons.messages.Messages;
import io.leitstand.commons.model.Repository;
import io.leitstand.event.queue.model.Message;
import io.leitstand.event.queue.model.Topic;
import io.leitstand.event.queue.model.TopicProvider;
import io.leitstand.event.queue.service.DomainEventId;
import io.leitstand.event.queue.service.DomainEventName;
import io.leitstand.event.queue.service.TopicName;
import io.leitstand.event.webhook.service.WebhookId;
import io.leitstand.event.webhook.service.WebhookMessage;
import io.leitstand.event.webhook.service.WebhookMessages;
import io.leitstand.event.webhook.service.WebhookName;
import io.leitstand.event.webhook.service.WebhookService;
import io.leitstand.event.webhook.service.WebhookStatistics;
import io.leitstand.security.crypto.MasterSecret;

public class WebhookQueueIT extends WebhookIT{

	private static final DomainEventId   READY_ID        = randomDomainEventId();
	private static final DomainEventId   IN_PROGRESS_ID  = randomDomainEventId();
	private static final DomainEventId   FAILED_ID       = randomDomainEventId();
	private static final DomainEventId   PROCESSED_ID    = randomDomainEventId();
	private static final DomainEventName EVENT_NAME      = domainEventName("event");

	private static final TopicName       TOPIC_NAME      = topicName("topic");
	private static final WebhookId       WEBHOOK_ID      = randomWebhookId();
	private static final WebhookName     WEBHOOK_NAME    = webhookName("webhook");
	private static final String          CORRELATION_ID  = randomUUID().toString();
	
	private WebhookService service;
	private Repository repository;
	
	@Before
	public void initService() {
		EntityManager em = getEntityManager();
		repository = new Repository(em);
		TopicProvider topics = new TopicProvider(repository, null);
		MasterSecret master = new MasterSecret(emptyEnvironment());
		master.init();
		
		transaction(() -> {
			Topic topic = repository.addIfAbsent(findTopicByName(TOPIC_NAME), 
												 () -> new Topic(TOPIC_NAME));
			

			
			Message failed = new Message(topic,
										 newDomainEvent()
										 .withTopicName(TOPIC_NAME)
										 .withDomainEventId(FAILED_ID)
										 .withDomainEventName(EVENT_NAME)
										 .withCorrelationId(CORRELATION_ID)
										 .build());
			repository.add(failed);
			
			Message processed = new Message(topic,
											newDomainEvent()
											.withTopicName(TOPIC_NAME)
											.withDomainEventId(PROCESSED_ID)
											.withDomainEventName(EVENT_NAME)
											.build());
			repository.add(processed);
			
			Message inprogress = new Message(topic,
											 newDomainEvent()
											 .withTopicName(TOPIC_NAME)
											 .withDomainEventId(IN_PROGRESS_ID)
											 .withDomainEventName(EVENT_NAME)
											 .build());
			repository.add(inprogress);

			Message ready = new Message(topic,
										newDomainEvent()
									    .withTopicName(TOPIC_NAME)
									    .withDomainEventId(READY_ID)
								       	.withDomainEventName(EVENT_NAME)
										.build());
			repository.add(ready);
			
			Webhook webhook = repository.addIfAbsent(findWebhookById(WEBHOOK_ID), 
													 () -> new Webhook(topic,WEBHOOK_ID,WEBHOOK_NAME));
			
			Webhook_Message message = new Webhook_Message(webhook,failed);
			message.setMessageState(FAILED);
			message.setExecTime(500L);
			message.setHttpStatus(409);
			repository.add(message);

			
			message = new Webhook_Message(webhook,processed);
			message.setMessageState(PROCESSED);
			message.setExecTime(100L);
			message.setHttpStatus(204);
			repository.add(message);

			message = new Webhook_Message(webhook,inprogress);
			message.setMessageState(IN_PROGRESS);
			repository.add(message);

			message = new Webhook_Message(webhook,ready);
			message.setMessageState(READY);
			repository.add(message);
			
			
		});
		
		service = new DefaultWebhookService(repository, 
											topics, 
											mock(Messages.class), 
											master,
											new WebhookProvider(repository),
											new WebhookStatisticsService(getDatabase()));
		
		
	}
	
	@After
	public void remove_all_messages() {
		transaction(() -> {
			getDatabase().executeUpdate(prepare("DELETE FROM BUS.WEBHOOK_MESSAGE"));
			getDatabase().executeUpdate(prepare("DELETE FROM BUS.MESSAGE"));
		});
	}
	
	
	@Test
	public void walk_message_queue() {
		
		transaction(() -> {
			WebhookMessages messages = service.findMessages(WEBHOOK_ID, 
															newMessageFilter()
															.withOffset(0)
															.withLimit(2)
															.build());
			assertEquals(WEBHOOK_ID,messages.getWebhookId());
			assertEquals(WEBHOOK_NAME,messages.getWebhookName());
			assertThat(messages.getMessages(),hasSizeOf(2));
			assertEquals(READY,messages.getMessages().get(0).getMessageState());
			assertEquals(IN_PROGRESS,messages.getMessages().get(1).getMessageState());			
		});
		
		transaction(() -> {
			WebhookMessages messages = service.findMessages(WEBHOOK_ID, 
															newMessageFilter()
															.withOffset(2)
															.withLimit(2)
															.build());
			assertEquals(WEBHOOK_ID,messages.getWebhookId());
			assertEquals(WEBHOOK_NAME,messages.getWebhookName());
			assertThat(messages.getMessages(),hasSizeOf(2));
			assertEquals(PROCESSED,messages.getMessages().get(0).getMessageState());
			assertEquals(FAILED,messages.getMessages().get(1).getMessageState());			
		});
		
	}
	
	@Test
	public void get_webhook_message_by_hook_name_and_domain_event_id() {
		transaction(()->{
			WebhookMessage message = service.getMessage(WEBHOOK_ID, IN_PROGRESS_ID);
			assertEquals(WEBHOOK_ID,message.getWebhookId());
			assertEquals(WEBHOOK_NAME,message.getWebhookName());
			assertEquals(IN_PROGRESS_ID,message.getEvent().getDomainEventId());
			assertEquals(IN_PROGRESS,message.getMessageState());
		});
	}
	
	@Test
	public void get_webhook_message_by_hook_id_and_domain_event_id() {
		transaction(()->{
			WebhookMessage message = service.getMessage(WEBHOOK_NAME, IN_PROGRESS_ID);
			assertEquals(WEBHOOK_ID,message.getWebhookId());
			assertEquals(WEBHOOK_NAME,message.getWebhookName());
			assertEquals(IN_PROGRESS_ID,message.getEvent().getDomainEventId());
			assertEquals(IN_PROGRESS,message.getMessageState());
		});
	}
	
	@Test
	public void retry_all_failed_messages_of_webhook_identified_by_hook_id() {
		transaction(()->{
			service.retryWebhook(WEBHOOK_ID);
		});
		
		transaction(()->{
			// Check that failed message is set to READY state
			WebhookMessage message = service.getMessage(WEBHOOK_ID, FAILED_ID);
			assertEquals(READY,message.getMessageState());
			assertNull(message.getHttpStatus());
			assertNull(message.getExecTime());
		});
		
	}
	
	@Test
	public void reset_hook_identified_by_hook_id() {
		transaction(()->{
			service.resetWebhook(WEBHOOK_ID,FAILED_ID);
		});
		
		transaction(()->{
			// Check that failed message is set to READY state
			WebhookMessage message = service.getMessage(WEBHOOK_ID, FAILED_ID);
			assertEquals(READY,message.getMessageState());
			assertNull(message.getHttpStatus());
			assertNull(message.getExecTime());

			message = service.getMessage(WEBHOOK_ID, IN_PROGRESS_ID);
			assertEquals(READY,message.getMessageState());
			assertNull(message.getHttpStatus());
			assertNull(message.getExecTime());

			message = service.getMessage(WEBHOOK_ID, PROCESSED_ID);
			assertEquals(READY,message.getMessageState());
			assertNull(message.getHttpStatus());
			assertNull(message.getExecTime());

			message = service.getMessage(WEBHOOK_ID, READY_ID);
			assertEquals(READY,message.getMessageState());
			assertNull(message.getHttpStatus());
			assertNull(message.getExecTime());
		});
	}
	
	
	@Test
	public void reset_hook_identified_by_hook_name() {
		transaction(()->{
			service.resetWebhook(WEBHOOK_NAME,FAILED_ID);
		});
		
		transaction(()->{
			// Check that failed message is set to READY state
			WebhookMessage message = service.getMessage(WEBHOOK_NAME, FAILED_ID);
			assertEquals(READY,message.getMessageState());
			assertNull(message.getHttpStatus());
			assertNull(message.getExecTime());

			message = service.getMessage(WEBHOOK_NAME, IN_PROGRESS_ID);
			assertEquals(READY,message.getMessageState());
			assertNull(message.getHttpStatus());
			assertNull(message.getExecTime());

			message = service.getMessage(WEBHOOK_NAME, PROCESSED_ID);
			assertEquals(READY,message.getMessageState());
			assertNull(message.getHttpStatus());
			assertNull(message.getExecTime());

			message = service.getMessage(WEBHOOK_NAME, READY_ID);
			assertEquals(READY,message.getMessageState());
			assertNull(message.getHttpStatus());
			assertNull(message.getExecTime());
		});
	}
	
	@Test
	public void get_webhook_statistics() {
		transaction(()->{

			Topic topic = repository.execute(findTopicByName(TOPIC_NAME)); 

			Message processed = new Message(topic,
											newDomainEvent()
											.withTopicName(TOPIC_NAME)
											.withDomainEventId(randomDomainEventId())
											.withDomainEventName(EVENT_NAME)
											.build());
			repository.add(processed);
			Webhook_Message message = new Webhook_Message(repository.execute(findWebhookById(WEBHOOK_ID)), processed);
			message.setMessageState(PROCESSED);
			message.setExecTime(200L);
			repository.add(message);
			
			
			Message failed = new Message(topic,
										 newDomainEvent()
										 .withTopicName(TOPIC_NAME)
										 .withDomainEventId(randomDomainEventId())
										 .withDomainEventName(EVENT_NAME)
										 .build());
			repository.add(failed);
			message = new Webhook_Message(repository.execute(findWebhookById(WEBHOOK_ID)), failed);
			message.setMessageState(FAILED);
			message.setExecTime(100L);
			repository.add(message);
			
		});
		
		transaction(()->{
			WebhookStatistics statistics = service.getWebhookStatistics(WEBHOOK_NAME);
			
			assertThat(statistics.getMessageCount(),is(6));
			assertThat(statistics.getStatistics().get(READY).getMessageCount(),is(1));
			assertNull(statistics.getStatistics().get(READY).getMinExecTime());
			assertNull(statistics.getStatistics().get(READY).getAvgExecTime());
			assertNull(statistics.getStatistics().get(READY).getMaxExecTime());
			assertNull(statistics.getStatistics().get(READY).getStddevExecTime());

			assertThat(statistics.getStatistics().get(IN_PROGRESS).getMessageCount(),is(1));
			assertNull(statistics.getStatistics().get(IN_PROGRESS).getMinExecTime());
			assertNull(statistics.getStatistics().get(IN_PROGRESS).getAvgExecTime());
			assertNull(statistics.getStatistics().get(IN_PROGRESS).getMaxExecTime());
			assertNull(statistics.getStatistics().get(IN_PROGRESS).getStddevExecTime());
		
			assertThat(statistics.getStatistics().get(PROCESSED).getMessageCount(),is(2));
			assertThat(statistics.getStatistics().get(PROCESSED).getMinExecTime(),is(100));
			assertEquals(150.0,statistics.getStatistics().get(PROCESSED).getAvgExecTime(),0.001);
			assertThat(statistics.getStatistics().get(PROCESSED).getMaxExecTime(),is(200));
			assertEquals(70.71,statistics.getStatistics().get(PROCESSED).getStddevExecTime(),0.001);
					
			assertThat(statistics.getStatistics().get(FAILED).getMessageCount(),is(2));
			assertThat(statistics.getStatistics().get(FAILED).getMinExecTime(),is(100));
			assertEquals(300.0,statistics.getStatistics().get(FAILED).getAvgExecTime(),0.001);
			assertThat(statistics.getStatistics().get(FAILED).getMaxExecTime(),is(500));
			assertEquals(282.842,statistics.getStatistics().get(FAILED).getStddevExecTime(),0.001);
			
			
			
		});
	}
	
	@Test
	public void filter_messages_by_correlation_id() {
		transaction(() -> {
			WebhookMessages messages = service.findMessages(WEBHOOK_ID, 
														    newMessageFilter()
														    .withCorrelationId(CORRELATION_ID)
														    .withLimit(2)
														    .build());
			assertEquals(WEBHOOK_ID,messages.getWebhookId());
			assertEquals(WEBHOOK_NAME,messages.getWebhookName());
			assertThat(messages.getMessages(),hasSizeOf(1));
			assertEquals(FAILED,messages.getMessages().get(0).getMessageState());
		});
	}
	
	@Test
	public void get_empty_messages_for_when_message_is_not_in_expected_state() {
		transaction(() -> {
			WebhookMessages messages = service.findMessages(WEBHOOK_ID, 
															newMessageFilter()
															.withCorrelationId("unkown")
															.withMessageState(PROCESSED)
															.build());
			assertEquals(WEBHOOK_ID,messages.getWebhookId());
			assertEquals(WEBHOOK_NAME,messages.getWebhookName());
			assertTrue(messages.getMessages().isEmpty());
		});
	}
	
	@Test
	public void get_empty_messages_for_unknown_correlation_id() {
		transaction(() -> {
			WebhookMessages messages = service.findMessages(WEBHOOK_ID, 
															newMessageFilter()
															.withCorrelationId("unkown")
															.build());
			assertEquals(WEBHOOK_ID,messages.getWebhookId());
			assertEquals(WEBHOOK_NAME,messages.getWebhookName());
			assertTrue(messages.getMessages().isEmpty());
		});
	}
}
