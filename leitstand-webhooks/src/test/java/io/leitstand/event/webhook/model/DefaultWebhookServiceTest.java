/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.webhook.model;

import static io.leitstand.commons.model.StringUtil.toUtf8Bytes;
import static io.leitstand.event.queue.service.DomainEventId.randomDomainEventId;
import static io.leitstand.event.queue.service.ReasonCode.BUS0002E_MESSAGE_NOT_FOUND;
import static io.leitstand.event.webhook.service.ReasonCode.WHK0003I_WEBHOOK_REMOVED;
import static io.leitstand.event.webhook.service.ReasonCode.WHK0010E_WEBHOOK_BASIC_AUTH_PASSWORD_MISMATCH;
import static io.leitstand.event.webhook.service.WebhookId.randomWebhookId;
import static io.leitstand.event.webhook.service.WebhookSettings.HttpMethod.POST;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.Base64;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
import io.leitstand.event.webhook.service.WebhookSettings;
import io.leitstand.security.crypto.MasterSecret;
import io.leitstand.testing.ut.LeitstandCoreMatchers;

@RunWith(MockitoJUnitRunner.class)
public class DefaultWebhookServiceTest {

	private static final Endpoint WEBHOOK_ENDPOINT = Endpoint.valueOf("http://www.rtbrick.com/unittest");
	private static final TopicName TOPIC_NAME = TopicName.valueOf("unittest");
	private static final WebhookName WEBHOOK_NAME = WebhookName.valueOf("unittest");
	private static final String SECRET = "secret";
	private static final String SECRET64 = Base64.getEncoder().encodeToString(toUtf8Bytes(SECRET));

	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Mock
	private Repository repository;

	@Mock
	private Messages messages;

	@Mock
	private MasterSecret secret;
	
	@Mock
	private TopicProvider topics;
	
	@Mock
	private WebhookProvider webhooks;
	
	@InjectMocks
	private DefaultWebhookService service = new DefaultWebhookService();

	private ArgumentCaptor<Message> messageCaptor;

	private ArgumentCaptor<Webhook> webhookCaptor;

	
	@Before
	public void initTestEnvironment() {
		this.messageCaptor = forClass(Message.class);
		doNothing().when(messages).add(messageCaptor.capture());
		this.webhookCaptor = forClass(Webhook.class);
		doNothing().when(repository).add(webhookCaptor.capture());
		when(topics.getOrCreateTopic(TOPIC_NAME)).thenReturn(new Topic(TOPIC_NAME)); 
		when(secret.encrypt(SECRET)).thenReturn(toUtf8Bytes(SECRET));
		when(secret.decrypt(any(byte[].class))).thenReturn(toUtf8Bytes(SECRET));
	}

	@Test
	public void cannot_store_webhook_if_password_and_confirmed_password_misatch() {
		WebhookSettings webhook = mock(WebhookSettings.class);
		when(webhook.getPassword()).thenReturn("foo");
		when(webhook.getConfirmPassword()).thenReturn("bar");
		
		service.storeWebhook(webhook);
		
		assertThat(messageCaptor.getValue().getReason(), is(WHK0010E_WEBHOOK_BASIC_AUTH_PASSWORD_MISMATCH.getReasonCode()));
		verifyZeroInteractions(repository,topics);
	}
	
	
	@Test
	public void create_new_webhook_without_endpoint_authentication() {
		WebhookSettings settings = mock(WebhookSettings.class);
		when(settings.getWebhookName()).thenReturn(WEBHOOK_NAME);
		when(settings.getTopicName()).thenReturn(TOPIC_NAME);
		when(settings.getEndpoint()).thenReturn(WEBHOOK_ENDPOINT);
		when(settings.getBatchSize()).thenReturn(5);
		when(settings.getMethod()).thenReturn(POST);
		boolean created = service.storeWebhook(settings);
		
		assertTrue(created);
		verify(topics).getOrCreateTopic(TOPIC_NAME);
		Webhook webhook = webhookCaptor.getValue();
		assertNotNull(webhook);
		assertEquals(WEBHOOK_NAME,webhook.getWebhookName());
		assertEquals(TOPIC_NAME,webhook.getTopicName());
		assertEquals(WEBHOOK_ENDPOINT,webhook.getEndpoint());
		assertEquals(5,webhook.getBatchSize());
		assertEquals(POST,webhook.getHttpMethod());
		
		verifyZeroInteractions(secret);
	}
	
	@Test
	public void update_existing_webhook_without_authentication() {
		WebhookSettings settings = mock(WebhookSettings.class);
		when(settings.getWebhookName()).thenReturn(WEBHOOK_NAME);
		when(settings.getTopicName()).thenReturn(TOPIC_NAME);
		when(settings.getEndpoint()).thenReturn(WEBHOOK_ENDPOINT);
		when(settings.getBatchSize()).thenReturn(5);
		when(settings.getMethod()).thenReturn(POST);
		
		Webhook webhook = mock(Webhook.class,withSettings().defaultAnswer(CALLS_REAL_METHODS));
		when(webhook.getId()).thenReturn(1L);
		
		when(webhooks.tryFetchWebhook(settings.getWebhookId())).thenReturn(webhook);
		
		boolean created = service.storeWebhook(settings);
		
		assertFalse(created);
		verify(topics).getOrCreateTopic(TOPIC_NAME);
		assertNotNull(webhook);
		assertEquals(WEBHOOK_NAME,webhook.getWebhookName());
		assertEquals(TOPIC_NAME,webhook.getTopicName());
		assertEquals(WEBHOOK_ENDPOINT,webhook.getEndpoint());
		assertEquals(5,webhook.getBatchSize());
		assertEquals(POST,webhook.getHttpMethod());
		
		verifyZeroInteractions(secret);
	}
	
	@Test
	public void create_new_webhook_with_accesskey_if_not_present() {
		WebhookSettings settings = mock(WebhookSettings.class);
		when(settings.getWebhookName()).thenReturn(WEBHOOK_NAME);
		when(settings.getTopicName()).thenReturn(TOPIC_NAME);
		when(settings.getEndpoint()).thenReturn(WEBHOOK_ENDPOINT);
		when(settings.getBatchSize()).thenReturn(5);
		when(settings.getMethod()).thenReturn(POST);
		when(settings.getAccesskey()).thenReturn(SECRET);
		
		boolean created = service.storeWebhook(settings);
		
		assertTrue(created);
		Webhook webhook = webhookCaptor.getValue();
		verify(topics).getOrCreateTopic(TOPIC_NAME);
		assertNotNull(webhook);
		assertEquals(WEBHOOK_NAME,webhook.getWebhookName());
		assertEquals(TOPIC_NAME,webhook.getTopicName());
		assertEquals(WEBHOOK_ENDPOINT,webhook.getEndpoint());
		assertEquals(5,webhook.getBatchSize());
		assertEquals(POST,webhook.getHttpMethod());
		assertEquals(SECRET64,webhook.getAccessKey64());
		verify(secret).encrypt(SECRET);
	}
	
	@Test
	public void update_existing_webhook_with_accesskey() {
		WebhookSettings settings = mock(WebhookSettings.class);
		when(settings.getWebhookName()).thenReturn(WEBHOOK_NAME);
		when(settings.getTopicName()).thenReturn(TOPIC_NAME);
		when(settings.getEndpoint()).thenReturn(WEBHOOK_ENDPOINT);
		when(settings.getBatchSize()).thenReturn(5);
		when(settings.getMethod()).thenReturn(POST);
		when(settings.getAccesskey()).thenReturn(SECRET);
		
		Webhook webhook = mock(Webhook.class,withSettings().defaultAnswer(CALLS_REAL_METHODS));
		when(webhook.getId()).thenReturn(1L);
		
		when(webhooks.tryFetchWebhook(settings.getWebhookId())).thenReturn(webhook);
		
		boolean created = service.storeWebhook(settings);
		
		assertFalse(created);
		verify(topics).getOrCreateTopic(TOPIC_NAME);
		assertNotNull(webhook);
		assertEquals(WEBHOOK_NAME,webhook.getWebhookName());
		assertEquals(TOPIC_NAME,webhook.getTopicName());
		assertEquals(WEBHOOK_ENDPOINT,webhook.getEndpoint());
		assertEquals(5,webhook.getBatchSize());
		assertEquals(POST,webhook.getHttpMethod());
		assertEquals(SECRET64,webhook.getAccessKey64());
		
		verify(secret).encrypt(SECRET);
	}
	
	@Test
	public void create_new_webhook_with_user_password_credentials_if_not_present() {
		WebhookSettings settings = mock(WebhookSettings.class);
		when(settings.getWebhookName()).thenReturn(WEBHOOK_NAME);
		when(settings.getTopicName()).thenReturn(TOPIC_NAME);
		when(settings.getEndpoint()).thenReturn(WEBHOOK_ENDPOINT);
		when(settings.getBatchSize()).thenReturn(5);
		when(settings.getMethod()).thenReturn(POST);
		when(settings.getUserId()).thenReturn("unittest");
		when(settings.getPassword()).thenReturn(SECRET);
		when(settings.getConfirmPassword()).thenReturn(SECRET);
		
		boolean created = service.storeWebhook(settings);
		
		assertTrue(created);
		Webhook webhook = webhookCaptor.getValue();
		verify(topics).getOrCreateTopic(TOPIC_NAME);
		assertNotNull(webhook);
		assertEquals(WEBHOOK_NAME,webhook.getWebhookName());
		assertEquals(TOPIC_NAME,webhook.getTopicName());
		assertEquals(WEBHOOK_ENDPOINT,webhook.getEndpoint());
		assertEquals(5,webhook.getBatchSize());
		assertEquals(POST,webhook.getHttpMethod());
		assertEquals("unittest",webhook.getUser());
		assertEquals(SECRET64,webhook.getPassword64());
		verify(secret).encrypt(SECRET);
	}
	
	@Test
	public void update_existing_webhook_with_user_password_credentials() {
		WebhookSettings settings = mock(WebhookSettings.class);
		when(settings.getWebhookName()).thenReturn(WEBHOOK_NAME);
		when(settings.getTopicName()).thenReturn(TOPIC_NAME);
		when(settings.getEndpoint()).thenReturn(WEBHOOK_ENDPOINT);
		when(settings.getBatchSize()).thenReturn(5);
		when(settings.getMethod()).thenReturn(POST);
		when(settings.getUserId()).thenReturn("unittest");
		when(settings.getPassword()).thenReturn(SECRET);
		when(settings.getConfirmPassword()).thenReturn(SECRET);

		
		Webhook webhook = mock(Webhook.class,withSettings().defaultAnswer(CALLS_REAL_METHODS));
		when(webhook.getId()).thenReturn(1L);

		when(webhooks.tryFetchWebhook(settings.getWebhookId())).thenReturn(webhook);

		
		boolean created = service.storeWebhook(settings);
		
		assertFalse(created);
		verify(topics).getOrCreateTopic(TOPIC_NAME);
		assertNotNull(webhook);
		assertEquals(WEBHOOK_NAME,webhook.getWebhookName());
		assertEquals(TOPIC_NAME,webhook.getTopicName());
		assertEquals(WEBHOOK_ENDPOINT,webhook.getEndpoint());
		assertEquals(5,webhook.getBatchSize());
		assertEquals(POST,webhook.getHttpMethod());
		assertEquals("unittest",webhook.getUser());
		assertEquals(SECRET64,webhook.getPassword64());
		
		verify(secret).encrypt(SECRET);
	}
	
	@Test
	public void attempt_to_remove_non_existent_webhook_fails_silently() {
		service.removeWebhook(randomWebhookId());
		verify(repository,never()).remove(any(Webhook.class));
		service.removeWebhook(WEBHOOK_NAME);
		verify(repository,never()).remove(any(Webhook.class));
	}
	
	@Test
	public void remove_existent_webhook_by_id_and_create_removal_message() {
		Webhook webhook = mock(Webhook.class);
		WebhookId hookId = randomWebhookId();
		when(webhooks.tryFetchWebhook(hookId)).thenReturn(webhook);

		service.removeWebhook(hookId);
		verify(repository).remove(webhook);
		assertThat(messageCaptor.getValue().getReason(),is(WHK0003I_WEBHOOK_REMOVED.getReasonCode()));
	}
	
	@Test
	public void remove_existent_webhook_by_name_and_create_removal_message() {
		Webhook webhook = mock(Webhook.class);
		when(webhooks.tryFetchWebhook(WEBHOOK_NAME)).thenReturn(webhook);

		service.removeWebhook(WEBHOOK_NAME);
		verify(repository).remove(webhook);
		assertThat(messageCaptor.getValue().getReason(),is(WHK0003I_WEBHOOK_REMOVED.getReasonCode()));

	}
	
	@Test
	public void throws_EntityNotFoundException_if_message_does_not_exist() {
		exception.expect(EntityNotFoundException.class);
		exception.expect(LeitstandCoreMatchers.reason(BUS0002E_MESSAGE_NOT_FOUND));
		
		Webhook hook = mock(Webhook.class);
		when(webhooks.fetchWebhook(WEBHOOK_NAME)).thenReturn(hook);
		
		service.getMessage(WEBHOOK_NAME, randomDomainEventId());
	}

}
