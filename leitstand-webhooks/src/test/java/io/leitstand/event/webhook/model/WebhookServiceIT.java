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

import static io.leitstand.commons.etc.Environment.emptyEnvironment;
import static io.leitstand.commons.template.TemplateService.newTemplateService;
import static io.leitstand.event.queue.model.Topic.findTopicByName;
import static io.leitstand.event.queue.service.TopicName.topicName;
import static io.leitstand.event.webhook.service.Endpoint.endpoint;
import static io.leitstand.event.webhook.service.ReasonCode.WHK0002E_WEBHOOK_NOT_FOUND;
import static io.leitstand.event.webhook.service.ReasonCode.WHK0003I_WEBHOOK_REMOVED;
import static io.leitstand.event.webhook.service.WebhookId.randomWebhookId;
import static io.leitstand.event.webhook.service.WebhookName.webhookName;
import static io.leitstand.event.webhook.service.WebhookSettings.newWebhookSettings;
import static io.leitstand.event.webhook.service.WebhookSettings.HttpMethod.POST;
import static io.leitstand.event.webhook.service.WebhookTemplate.newWebhookTemplate;
import static io.leitstand.testing.ut.LeitstandCoreMatchers.reason;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;

import io.leitstand.commons.EntityNotFoundException;
import io.leitstand.commons.messages.Message;
import io.leitstand.commons.messages.Messages;
import io.leitstand.commons.model.Repository;
import io.leitstand.event.queue.model.Topic;
import io.leitstand.event.queue.model.TopicProvider;
import io.leitstand.event.queue.service.TopicName;
import io.leitstand.event.webhook.service.Endpoint;
import io.leitstand.event.webhook.service.WebhookId;
import io.leitstand.event.webhook.service.WebhookName;
import io.leitstand.event.webhook.service.WebhookService;
import io.leitstand.event.webhook.service.WebhookSettings;
import io.leitstand.event.webhook.service.WebhookTemplate;
import io.leitstand.security.crypto.MasterSecret;
import io.leitstand.testing.ut.LeitstandCoreMatchers;

public class WebhookServiceIT extends WebhookIT{

    private static final WebhookId   WEBHOOK_ID   = randomWebhookId();
    private static final WebhookName WEBHOOK_NAME = webhookName("webhook");
    private static final TopicName   TOPIC_NAME   = topicName("topic");
    private static final String      ACCESSKEY    = "accesskey";
    private static final String      DESCRIPTION  = "description";
    private static final Endpoint    ENDPOINT     = endpoint("http://localhost:8080");
    private static final String      USER_NAME    = "user";
    private static final String      PASSWORD     = "password";
    private static final String      SELECTOR     = "selector";
	
    @Rule
    public ExpectedException exception = ExpectedException.none();
    
	private WebhookService service;
	private Messages messages;
	private ArgumentCaptor<Message> messagesCaptor;
	private Repository repository;
	
	@Before
	public void initService() {
		messages = mock(Messages.class);
		EntityManager em = getEntityManager();
		repository = new Repository(em);
		TopicProvider topics = new TopicProvider(repository, null);
		MasterSecret master = new MasterSecret(emptyEnvironment());
		master.init();
		
		transaction(() -> {
			repository.addIfAbsent(findTopicByName(TOPIC_NAME), 
								   () -> new Topic(TOPIC_NAME));
			
		});
		
		service = new DefaultWebhookService(repository, 
											topics, 
											messages, 
											master,
											new WebhookProvider(repository),
											new WebhookRewritingService(newTemplateService()),
											new WebhookStatisticsService(getDatabase()));
		
		messagesCaptor = ArgumentCaptor.forClass(Message.class);
		
	}
	
	@Test
	public void raise_exception_when_webhook_does_not_exist() {
	    exception.expect(EntityNotFoundException.class);
	    exception.expect(reason(WHK0002E_WEBHOOK_NOT_FOUND));
		service.getWebhookTemplate(WEBHOOK_ID);
	}
	
	
	@Test
	public void raise_exception_when_webhook_name_does_not_exist() {
        exception.expect(EntityNotFoundException.class);
        exception.expect(reason(WHK0002E_WEBHOOK_NOT_FOUND));
        service.getWebhookTemplate(WEBHOOK_NAME);
	}
	
	@Test
	public void create_new_webhook() {
		WebhookSettings settings = newWebhookSettings()
								   .withWebhookId(WEBHOOK_ID)
								   .withWebhookName(WEBHOOK_NAME)
								   .withAccesskey(ACCESSKEY)
								   .withBatchSize(10)
								   .withDescription(DESCRIPTION)
								   .withEnabled(true)
								   .withEndpoint(ENDPOINT)
								   .withMethod(POST)
								   .withUserId(USER_NAME)
								   .withPassword(PASSWORD)
								   .withSelector(SELECTOR)
								   .withTopicName(TOPIC_NAME)
								   .build();
		
		transaction(()->{
			service.storeWebhook(settings);
		});
		
		transaction(()->{
			assertEquals(settings,service.getWebhook(WEBHOOK_ID));
		});
		
		
	}
	
	@Test
	public void remove_existing_webhook_by_id() {
		WebhookSettings settings = newWebhookSettings()
				   				   .withWebhookId(WEBHOOK_ID)
				   				   .withWebhookName(WEBHOOK_NAME)
				   				   .withAccesskey(ACCESSKEY)
				   				   .withBatchSize(10)
				   				   .withDescription(DESCRIPTION)
				   				   .withEnabled(true)
				   				   .withEndpoint(ENDPOINT)
				   				   .withMethod(POST)
				   				   .withUserId(USER_NAME)
				   				   .withPassword(PASSWORD)
				   				   .withSelector(SELECTOR)
				   				   .withTopicName(TOPIC_NAME)
				   				   .build();

		transaction(()->{
			service.storeWebhook(settings);
		});
		doNothing().when(messages).add(messagesCaptor.capture());
		transaction(()->{
			service.removeWebhook(WEBHOOK_ID);
		});
	
		Message message = messagesCaptor.getValue();
		assertEquals(WHK0003I_WEBHOOK_REMOVED.getReasonCode(),message.getReason());
	}
	
	
	@Test
	public void remove_existing_webhook_by_name() {
		WebhookSettings settings = newWebhookSettings()
				   				   .withWebhookId(WEBHOOK_ID)
				   				   .withWebhookName(WEBHOOK_NAME)
				   				   .withAccesskey(ACCESSKEY)
				   				   .withBatchSize(10)
				   				   .withDescription(DESCRIPTION)
				   				   .withEnabled(true)
				   				   .withEndpoint(ENDPOINT)
				   				   .withMethod(POST)
				   				   .withUserId(USER_NAME)
				   				   .withPassword(PASSWORD)
				   				   .withSelector(SELECTOR)
				   				   .withTopicName(TOPIC_NAME)
				   				   .build();

		transaction(()->{
			service.storeWebhook(settings);
		});
		doNothing().when(messages).add(messagesCaptor.capture());
		transaction(()->{
			service.removeWebhook(WEBHOOK_NAME);
		});
	
		Message message = messagesCaptor.getValue();
		assertEquals(WHK0003I_WEBHOOK_REMOVED.getReasonCode(),message.getReason());
	}
	
	@Test
	public void remove_nonexisting_webhook_by_id_does_not_raise_an_error() {
		service.removeWebhook(WEBHOOK_ID);
		verifyZeroInteractions(messages);
	}

	   @Test
	    public void remove_nonexisting_webhook_by_name_does_not_raise_an_error() {
	        service.removeWebhook(WEBHOOK_NAME);
	        verifyZeroInteractions(messages);
	    }
	
	@Test
	public void rename_existing_webhook() {
		WebhookSettings settings = newWebhookSettings()
								   .withWebhookId(WEBHOOK_ID)
								   .withWebhookName(WEBHOOK_NAME)
								   .withAccesskey(ACCESSKEY)
								   .withBatchSize(10)
								   .withDescription(DESCRIPTION)
								   .withEnabled(true)
								   .withEndpoint(ENDPOINT)
								   .withMethod(POST)
								   .withUserId(USER_NAME)
								   .withPassword(PASSWORD)
								   .withSelector(SELECTOR)
								   .withTopicName(TOPIC_NAME)
								   .build();

		transaction(() -> {
			service.storeWebhook(settings);
		});
		
		WebhookName renamed = webhookName("renamed");

		transaction(() -> {
		    WebhookSettings upgrade = newWebhookSettings()
                                      .withWebhookId(WEBHOOK_ID)
                                      .withWebhookName(renamed)
                                      .withAccesskey(ACCESSKEY)
                                      .withBatchSize(10)
                                      .withDescription(DESCRIPTION)
                                      .withEnabled(true)
                                      .withEndpoint(ENDPOINT)
                                      .withMethod(POST)
                                      .withUserId(USER_NAME)
                                      .withPassword(PASSWORD)
                                      .withSelector(SELECTOR)
                                      .withTopicName(TOPIC_NAME)
                                      .build();
		    
		   service.storeWebhook(upgrade); 
		});
		
		transaction(() -> {
		   assertEquals(renamed, service.getWebhook(WEBHOOK_ID)
		                                .getWebhookName()); 
		});
		
	}
	
	@Test
	public void attempt_to_set_template_for_nonexistent_template_raises_exception() {
	    exception.expect(EntityNotFoundException.class);
	    exception.expect(reason(WHK0002E_WEBHOOK_NOT_FOUND));
	    
		WebhookTemplate template = newWebhookTemplate()
								   .withWebhookId(WEBHOOK_ID)
								   .withWebhookName(WEBHOOK_NAME)
								   .withContentType("application/json")
								   .withTemplate("Hello {{world}}!")
								   .build();
		
		transaction(()->{
			service.storeWebhookTemplate(template);
		});
		
	}
	
	@Test
	public void remove_template_of_existing_webhook_by_id() {
		WebhookSettings settings = newWebhookSettings()
				   				   .withWebhookId(WEBHOOK_ID)
				   				   .withWebhookName(WEBHOOK_NAME)
				   				   .withEnabled(true)
				   				   .withEndpoint(ENDPOINT)
				   				   .withMethod(POST)
								   .withTopicName(TOPIC_NAME)
				   				   .build();
		
		WebhookTemplate template = newWebhookTemplate()
				   				   .withWebhookId(WEBHOOK_ID)
				   				   .withWebhookName(WEBHOOK_NAME)
				   				   .withTemplate("test")
				   				   .build();

		transaction(()->{
			service.storeWebhook(settings);
			service.storeWebhookTemplate(template);
		});
		
		transaction(()->{
			assertEquals("test",service.getWebhookTemplate(WEBHOOK_ID).getTemplate());
			service.removeWebhookTemplate(WEBHOOK_ID);
		});
		
		transaction(()->{
			assertNull(service.getWebhookTemplate(WEBHOOK_ID).getTemplate());
			service.removeWebhookTemplate(WEBHOOK_ID);
		});

	}
	
	@Test
	public void remove_template_of_existing_webhook_by_name() {
		WebhookSettings settings = newWebhookSettings()
				   				   .withWebhookId(WEBHOOK_ID)
				   				   .withWebhookName(WEBHOOK_NAME)
				   				   .withEnabled(true)
				   				   .withEndpoint(ENDPOINT)
				   				   .withMethod(POST)
								   .withTopicName(TOPIC_NAME)
				   				   .build();
		
		WebhookTemplate template = newWebhookTemplate()
				   				   .withWebhookId(WEBHOOK_ID)
				   				   .withWebhookName(WEBHOOK_NAME)
				   				   .withTemplate("test")
				   				   .build();

		transaction(()->{
			service.storeWebhook(settings);
			service.storeWebhookTemplate(template);
		});
		
		transaction(()->{
			assertEquals("test",service.getWebhookTemplate(WEBHOOK_NAME).getTemplate());
			service.removeWebhookTemplate(WEBHOOK_NAME);
		});
		
		transaction(()->{
			assertNull(service.getWebhookTemplate(WEBHOOK_NAME).getTemplate());
			service.removeWebhookTemplate(WEBHOOK_NAME);
		});

	}
	
	@Test
	public void enable_disabled_webhook() {
		WebhookSettings settings = newWebhookSettings()
								   .withWebhookId(WEBHOOK_ID)
								   .withWebhookName(WEBHOOK_NAME)
								   .withAccesskey(ACCESSKEY)
								   .withBatchSize(10)
								   .withDescription(DESCRIPTION)
								   .withEnabled(false)
								   .withEndpoint(ENDPOINT)
								   .withMethod(POST)
								   .withUserId(USER_NAME)
								   .withPassword(PASSWORD)
								   .withSelector(SELECTOR)
								   .withTopicName(TOPIC_NAME)
								   .build();
		
		transaction(()->{
			service.storeWebhook(settings);
		});

		transaction(()->{
 			assertFalse(service.getWebhook(WEBHOOK_ID).isEnabled());
			service.enableWebhook(WEBHOOK_ID);
		});
		
		transaction(()->{
			assertTrue(service.getWebhook(WEBHOOK_ID).isEnabled());
		});
		
	}
	
	@Test
	public void disable_enabled_webhook() {
		WebhookSettings settings = newWebhookSettings()
								   .withWebhookId(WEBHOOK_ID)
								   .withWebhookName(WEBHOOK_NAME)
								   .withEndpoint(ENDPOINT)
								   .withMethod(POST)
								   .withTopicName(TOPIC_NAME)
								   .build();
				
		transaction(()->{
			service.storeWebhook(settings);
		});
		
		transaction(()->{
			assertTrue(service.getWebhook(WEBHOOK_ID).isEnabled());
			service.disableWebhook(WEBHOOK_ID);
		});
		
		transaction(()->{
			assertFalse(service.getWebhook(WEBHOOK_ID).isEnabled());
		});
	}

	
}
