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

import static io.leitstand.commons.jpa.SerializableJsonObjectConverter.parseJson;
import static io.leitstand.commons.json.JsonMarshaller.marshal;
import static io.leitstand.commons.jsonb.IsoDateAdapter.parseIsoDate;
import static io.leitstand.commons.model.StringUtil.toUtf8Bytes;
import static io.leitstand.commons.template.TemplateService.newTemplateService;
import static io.leitstand.event.queue.service.DomainEvent.newDomainEvent;
import static io.leitstand.event.queue.service.DomainEventId.domainEventId;
import static io.leitstand.event.queue.service.DomainEventId.randomDomainEventId;
import static io.leitstand.event.queue.service.DomainEventName.domainEventName;
import static io.leitstand.event.queue.service.TopicName.topicName;
import static io.leitstand.event.webhook.model.WebhookInvocation.newWebhookInvocation;
import static io.leitstand.event.webhook.service.Endpoint.endpoint;
import static io.leitstand.event.webhook.service.WebhookId.randomWebhookId;
import static io.leitstand.event.webhook.service.WebhookName.webhookName;
import static io.leitstand.event.webhook.service.WebhookSettings.HttpMethod.POST;
import static io.leitstand.security.auth.UserName.userName;
import static java.lang.Boolean.TRUE;
import static java.util.Arrays.asList;
import static java.util.Base64.getEncoder;
import static java.util.Collections.emptyList;
import static javax.json.Json.createObjectBuilder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;

import io.leitstand.commons.db.DatabaseService;
import io.leitstand.commons.db.ResultSetMapping;
import io.leitstand.commons.db.StatementPreparator;
import io.leitstand.commons.model.Query;
import io.leitstand.commons.model.Repository;
import io.leitstand.event.queue.model.Topic;
import io.leitstand.event.queue.service.DomainEvent;
import io.leitstand.event.queue.service.DomainEventName;
import io.leitstand.event.queue.service.TopicName;
import io.leitstand.event.webhook.service.Endpoint;
import io.leitstand.security.crypto.MasterSecret;

public class WebhookInvocationServiceTest {

	private static final Endpoint WEBHOOK_ENDPOINT = endpoint("http://leitstand.io/unittest");
	private static final byte[] SECRET = toUtf8Bytes("secret");
	private static final String SECRET64 = getEncoder().encodeToString(toUtf8Bytes("secret"));
	
	private MasterSecret secret;
	
	private Repository repository;
	private DatabaseService db;
	
	private WebhookInvocationService service;
	
	private Webhook webhook;
	private Topic topic;
	private WebhookInvocation call;
	private DomainEvent<JsonObject> event;
	
	@Before
	public void initTestEnvironment() {
		repository = mock(Repository.class);
		secret = mock(MasterSecret.class);
		
		topic   = new Topic(new TopicName("unittest"));
		webhook = mock(Webhook.class);
		when(webhook.getHttpMethod()).thenReturn(POST);
		when(webhook.getWebhookId()).thenReturn(randomWebhookId());
		when(webhook.getWebhookName()).thenReturn(webhookName("unittest"));
		when(webhook.getEndpoint()).thenReturn(WEBHOOK_ENDPOINT);
		event   = newDomainEvent(JsonObject.class)
				  .withDomainEventId(randomDomainEventId())
				  .withTopicName(topic.getName())
				  .withDomainEventName(DomainEventName.valueOf("unittest"))
				  .withPayload(createObjectBuilder().add("unit","test").build())
				  .build();
		
		call = newWebhookInvocation()
			   .withDomainEventId(event.getDomainEventId())
			   .withDomainEventName(event.getDomainEventName())
			   .withEndpoint(WEBHOOK_ENDPOINT)
			   .withMessage(marshal(event).toString())
			   .build();
		db = mock(DatabaseService.class);
		service = new WebhookInvocationService(repository, 
											   db,
											   secret, 
											   new WebhookRewritingService(newTemplateService()) );
	}
	
	@Test
	public void return_empty_list_when_no_messages_exist() {
		when(repository.execute(any(Query.class))).thenReturn(emptyList());
		assertTrue(service.findInvocations().isEmpty());
	}
	
	@Test
	public void return_empty_list_when_no_webhooks_exist() {
		when(repository.execute(any(Query.class))).thenReturn(asList(webhook)).thenReturn(emptyList());
		assertTrue(service.findInvocations().isEmpty());
	}
	
	
	@Test
	public void invoke_webhook_without_authentication() {
		when(repository.execute(any(Query.class))).thenReturn(asList(webhook));
		when(db.executeQuery(any(StatementPreparator.class), any(ResultSetMapping.class))).thenReturn(asList(call));
		
		List<WebhookBatch> batches = service.findInvocations();
		assertFalse(batches.isEmpty());
		assertEquals(event.getDomainEventId(),batches.get(0).getWebhookInvocations().get(0).getEventId());
		assertEquals(event.getDomainEventName(),batches.get(0).getWebhookInvocations().get(0).getEventName());
		assertEquals(webhook.getEndpoint(),batches.get(0).getWebhookInvocations().get(0).getEndpoint());
		assertEquals(webhook.getHttpMethod(),batches.get(0).getMethod());
		assertNull(batches.get(0).getUserName());
		assertNull(batches.get(0).getPassword());
		assertNull(batches.get(0).getAccesskey());
		assertEquals(webhook.getWebhookId(),batches.get(0).getWebhookId());
		assertEquals(webhook.getWebhookName(),batches.get(0).getWebhookName());
		
	}
	
	@Test
	public void invoke_webhook_with_basic_authentication() {
		when(repository.execute(any(Query.class))).thenReturn(asList(webhook));
		when(db.executeQuery(any(StatementPreparator.class), any(ResultSetMapping.class))).thenReturn(asList(call));
		
		when(webhook.isBasicAuthentication()).thenReturn(TRUE);
		when(webhook.getUser()).thenReturn("unittest");
		when(webhook.getPassword64()).thenReturn(SECRET64);
		when(secret.decrypt(SECRET)).thenReturn(SECRET);
		
		List<WebhookBatch> batches = service.findInvocations();
		assertFalse(batches.isEmpty());
		assertEquals(event.getDomainEventId(),batches.get(0).getWebhookInvocations().get(0).getEventId());
		assertEquals(event.getDomainEventName(),batches.get(0).getWebhookInvocations().get(0).getEventName());
		assertEquals(webhook.getEndpoint(),batches.get(0).getWebhookInvocations().get(0).getEndpoint());
		assertEquals(webhook.getHttpMethod(),batches.get(0).getMethod());
		assertEquals(userName("unittest"),batches.get(0).getUserName());
		assertTrue(batches.get(0).getPassword().compareTo("secret"));
		assertNull(batches.get(0).getAccesskey());
		assertEquals(webhook.getWebhookId(),batches.get(0).getWebhookId());
		assertEquals(webhook.getWebhookName(),batches.get(0).getWebhookName());
	
	}
	
	@Test
	public void invoke_webhook_with_bearer_token_authentication() {
		when(repository.execute(any(Query.class))).thenReturn(asList(webhook));
		when(db.executeQuery(any(StatementPreparator.class), any(ResultSetMapping.class))).thenReturn(asList(call));
		when(webhook.getAccessKey64()).thenReturn(SECRET64);
		when(secret.decrypt(SECRET)).thenReturn(SECRET);
		
		List<WebhookBatch> batches = service.findInvocations();
		assertFalse(batches.isEmpty());
		assertEquals(event.getDomainEventId(),batches.get(0).getWebhookInvocations().get(0).getEventId());
		assertEquals(event.getDomainEventName(),batches.get(0).getWebhookInvocations().get(0).getEventName());
		assertEquals(webhook.getEndpoint(),batches.get(0).getWebhookInvocations().get(0).getEndpoint());
		assertEquals(webhook.getHttpMethod(),batches.get(0).getMethod());
		assertNull(batches.get(0).getUserName());
		assertNull(batches.get(0).getPassword());
		assertEquals("secret",batches.get(0).getAccesskey());
		assertEquals(webhook.getWebhookId(),batches.get(0).getWebhookId());
		assertEquals(webhook.getWebhookName(),batches.get(0).getWebhookName());
		
		JsonObject payload = parseJson(batches.get(0).getWebhookInvocations().get(0).getMessage());
		assertEquals(event.getDomainEventId(),domainEventId(payload.getString("event_id")));
		assertEquals(event.getDomainEventName(),domainEventName(payload.getString("event_name")));
		assertEquals(event.getDateCreated(),parseIsoDate(payload.getString("date_created")));
		assertEquals(event.getTopicName(),topicName(payload.getString("topic_name")));
		assertEquals("{\"unit\":\"test\"}",payload.getJsonObject("payload").toString());
	}
	
}
