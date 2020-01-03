/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.webhook.model;

import static io.leitstand.commons.etc.Environment.emptyEnvironment;
import static io.leitstand.commons.template.TemplateService.newTemplateService;
import static io.leitstand.event.queue.model.Topic.findTopicByName;
import static io.leitstand.event.webhook.service.ReasonCode.WHK0002E_WEBHOOK_NOT_FOUND;
import static io.leitstand.event.webhook.service.ReasonCode.WHK0003I_WEBHOOK_REMOVED;
import static io.leitstand.event.webhook.service.WebhookId.randomWebhookId;
import static io.leitstand.event.webhook.service.WebhookSettings.newWebhookSettings;
import static io.leitstand.event.webhook.service.WebhookSettings.HttpMethod.POST;
import static io.leitstand.event.webhook.service.WebhookTemplate.newWebhookTemplate;
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
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import io.leitstand.commons.EntityNotFoundException;
import io.leitstand.commons.messages.Message;
import io.leitstand.commons.messages.Messages;
import io.leitstand.commons.model.Repository;
import io.leitstand.event.queue.model.Topic;
import io.leitstand.event.queue.model.TopicProvider;
import io.leitstand.event.queue.service.TopicName;
import io.leitstand.event.webhook.service.Endpoint;
import io.leitstand.event.webhook.service.WebhookName;
import io.leitstand.event.webhook.service.WebhookService;
import io.leitstand.event.webhook.service.WebhookSettings;
import io.leitstand.event.webhook.service.WebhookTemplate;
import io.leitstand.security.crypto.MasterSecret;

public class WebhookServiceIT extends WebhookIT{

	private static final TopicName TOPIC = TopicName.valueOf("WebhookServiceIT");
	
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
			repository.addIfAbsent(findTopicByName(TOPIC), 
								   () -> new Topic(TOPIC));
			
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
		try {
			service.getWebhookTemplate(randomWebhookId());
		} catch (EntityNotFoundException e) {
			assertEquals(WHK0002E_WEBHOOK_NOT_FOUND, e.getReason());
		}
	}
	
	
	@Test
	public void raise_exception_when_webhook_name_does_not_exist() {
		try {
			service.getWebhookTemplate(WebhookName.valueOf("non-existent"));
		} catch (EntityNotFoundException e) {
			assertEquals(WHK0002E_WEBHOOK_NOT_FOUND, e.getReason());
		}
	}
	
	@Test
	public void create_new_webhook() {
		WebhookSettings settings = newWebhookSettings()
								   .withWebhookId(randomWebhookId())
								   .withWebhookName(WebhookName.valueOf("create_new_webhook"))
								   .withAccesskey("ACCESSKEY")
								   .withBatchSize(10)
								   .withDescription("Unit test webhook")
								   .withEnabled(true)
								   .withEndpoint(Endpoint.valueOf("http://leitstand.io"))
								   .withMethod(POST)
								   .withUserId("unitest")
								   .withPassword("password")
								   .withSelector(".*")
								   .withTopicName(TOPIC)
								   .build();
		
		transaction(()->{
			service.storeWebhook(settings);
		});
		
		transaction(()->{
			assertEquals(settings,service.getWebhook(settings.getWebhookId()));
		});
		
		
	}
	
	@Test
	public void remove_existing_webhook_by_id() {
		WebhookSettings settings = newWebhookSettings()
				   				   .withWebhookId(randomWebhookId())
				   				   .withWebhookName(WebhookName.valueOf("remove_existing_webhook"))
				   				   .withAccesskey("ACCESSKEY")
				   				   .withBatchSize(10)
				   				   .withDescription("Unit test webhook")
				   				   .withEnabled(true)
				   				   .withEndpoint(Endpoint.valueOf("http://leitstand.io"))
				   				   .withMethod(POST)
				   				   .withUserId("unitest")
				   				   .withPassword("password")
				   				   .withSelector(".*")
				   				   .withTopicName(TOPIC)
				   				   .build();

		transaction(()->{
			service.storeWebhook(settings);
		});
		doNothing().when(messages).add(messagesCaptor.capture());
		transaction(()->{
			service.removeWebhook(settings.getWebhookId());
		});
	
		Message message = messagesCaptor.getValue();
		assertEquals(WHK0003I_WEBHOOK_REMOVED.getReasonCode(),message.getReason());
	}
	
	
	@Test
	public void remove_existing_webhook_by_name() {
		WebhookSettings settings = newWebhookSettings()
				   				   .withWebhookId(randomWebhookId())
				   				   .withWebhookName(WebhookName.valueOf("remove_existing_webhook"))
				   				   .withAccesskey("ACCESSKEY")
				   				   .withBatchSize(10)
				   				   .withDescription("Unit test webhook")
				   				   .withEnabled(true)
				   				   .withEndpoint(Endpoint.valueOf("http://leitstand.io"))
				   				   .withMethod(POST)
				   				   .withUserId("unitest")
				   				   .withPassword("password")
				   				   .withSelector(".*")
				   				   .withTopicName(TOPIC)
				   				   .build();

		transaction(()->{
			service.storeWebhook(settings);
		});
		doNothing().when(messages).add(messagesCaptor.capture());
		transaction(()->{
			service.removeWebhook(settings.getWebhookName());
		});
	
		Message message = messagesCaptor.getValue();
		assertEquals(WHK0003I_WEBHOOK_REMOVED.getReasonCode(),message.getReason());
	}
	
	@Test
	public void remove_nonexisting_webhook_does_not_raise_an_error() {
		service.removeWebhook(randomWebhookId());
		verifyZeroInteractions(messages);
	}
	
	@Test
	public void rename_existing_webhook() {
		WebhookSettings settings = newWebhookSettings()
								   .withWebhookId(randomWebhookId())
								   .withWebhookName(WebhookName.valueOf("rename_existing_webhook"))
								   .withAccesskey("ACCESSKEY")
								   .withBatchSize(10)
								   .withDescription("Unit test webhook")
								   .withEnabled(true)
								   .withEndpoint(Endpoint.valueOf("http://leitstand.io"))
								   .withMethod(POST)
								   .withUserId("unitest")
								   .withPassword("password")
								   .withSelector(".*")
								   .withTopicName(TOPIC)
								   .build();

		transaction(()->{
			service.storeWebhook(settings);
		});
		
	}
	
	@Test
	public void attempt_to_set_template_for_nonexistent_template_raises_exception() {
		WebhookTemplate template = newWebhookTemplate()
								   .withWebhookId(randomWebhookId())
								   .withWebhookName(WebhookName.valueOf("non-existent"))
								   .withContentType("application/json")
								   .withTemplate("Hello {{world}}!")
								   .build();
		
		transaction(()->{
			try {
				service.storeWebhookTemplate(template);
				fail("EntityNotFoundException expected!");
			} catch (EntityNotFoundException e) {
				assertEquals(WHK0002E_WEBHOOK_NOT_FOUND,e.getReason());
			}
		});
		
	}
	
	@Test
	public void remove_template_of_existing_webhook_by_id() {
		WebhookSettings settings = newWebhookSettings()
				   				   .withWebhookId(randomWebhookId())
				   				   .withWebhookName(WebhookName.valueOf("remove_template_of_existing_webhook_by_id"))
				   				   .withEnabled(true)
				   				   .withEndpoint(Endpoint.valueOf("http://leitstand.io"))
				   				   .withMethod(POST)
								   .withTopicName(TOPIC)
				   				   .build();
		
		WebhookTemplate template = newWebhookTemplate()
				   				   .withWebhookId(settings.getWebhookId())
				   				   .withWebhookName(settings.getWebhookName())
				   				   .withTemplate("test")
				   				   .build();

		transaction(()->{
			service.storeWebhook(settings);
			service.storeWebhookTemplate(template);
		});
		
		transaction(()->{
			assertEquals("test",service.getWebhookTemplate(settings.getWebhookId()).getTemplate());
			service.removeWebhookTemplate(settings.getWebhookId());
		});
		
		transaction(()->{
			assertNull(service.getWebhookTemplate(settings.getWebhookId()).getTemplate());
			service.removeWebhookTemplate(settings.getWebhookId());
		});

	}
	
	@Test
	public void remove_template_of_existing_webhook_by_name() {
		WebhookSettings settings = newWebhookSettings()
				   				   .withWebhookId(randomWebhookId())
				   				   .withWebhookName(WebhookName.valueOf("remove_template_of_existing_webhook_by_name"))
				   				   .withEnabled(true)
				   				   .withEndpoint(Endpoint.valueOf("http://leitstand.io"))
				   				   .withMethod(POST)
								   .withTopicName(TOPIC)
				   				   .build();
		
		WebhookTemplate template = newWebhookTemplate()
				   				   .withWebhookId(settings.getWebhookId())
				   				   .withWebhookName(settings.getWebhookName())
				   				   .withTemplate("test")
				   				   .build();

		transaction(()->{
			service.storeWebhook(settings);
			service.storeWebhookTemplate(template);
		});
		
		transaction(()->{
			assertEquals("test",service.getWebhookTemplate(settings.getWebhookName()).getTemplate());
			service.removeWebhookTemplate(settings.getWebhookName());
		});
		
		transaction(()->{
			assertNull(service.getWebhookTemplate(settings.getWebhookName()).getTemplate());
			service.removeWebhookTemplate(settings.getWebhookName());
		});

	}
	
	@Test
	public void enable_disabled_webhook() {
		WebhookSettings settings = newWebhookSettings()
								   .withWebhookId(randomWebhookId())
								   .withWebhookName(WebhookName.valueOf("enable_disabled_webhook"))
								   .withAccesskey("ACCESSKEY")
								   .withBatchSize(10)
								   .withDescription("Unit test webhook")
								   .withEnabled(false)
								   .withEndpoint(Endpoint.valueOf("http://leitstand.io"))
								   .withMethod(POST)
								   .withUserId("unitest")
								   .withPassword("password")
								   .withSelector(".*")
								   .withTopicName(TOPIC)
								   .build();
		
		transaction(()->{
			service.storeWebhook(settings);
		});

		transaction(()->{
 			assertFalse(service.getWebhook(settings.getWebhookId()).isEnabled());
			service.enableWebhook(settings.getWebhookId());
		});
		
		transaction(()->{
			assertTrue(service.getWebhook(settings.getWebhookId()).isEnabled());
		});
		
	}
	
	@Test
	public void disable_enabled_webhook() {
		WebhookSettings settings = newWebhookSettings()
								   .withWebhookId(randomWebhookId())
								   .withWebhookName(WebhookName.valueOf("disable_enabled_webhook"))
								   .withEndpoint(Endpoint.valueOf("http://leitstand.io"))
								   .withMethod(POST)
								   .withTopicName(TOPIC)
								   .build();
				
		transaction(()->{
			service.storeWebhook(settings);
		});
		
		transaction(()->{
			assertTrue(service.getWebhook(settings.getWebhookId()).isEnabled());
			service.disableWebhook(settings.getWebhookId());
		});
		
		transaction(()->{
			assertFalse(service.getWebhook(settings.getWebhookId()).isEnabled());
		});
	}

	
}
