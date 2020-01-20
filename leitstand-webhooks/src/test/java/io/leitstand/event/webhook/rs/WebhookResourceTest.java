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
package io.leitstand.event.webhook.rs;

import static io.leitstand.commons.rs.ReasonCode.VAL0003E_IMMUTABLE_ATTRIBUTE;
import static io.leitstand.event.webhook.service.WebhookId.randomWebhookId;
import static io.leitstand.event.webhook.service.WebhookName.webhookName;
import static io.leitstand.event.webhook.service.WebhookSettings.newWebhookSettings;
import static io.leitstand.event.webhook.service.WebhookTemplate.newWebhookTemplate;
import static io.leitstand.testing.ut.LeitstandCoreMatchers.reason;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.leitstand.commons.UnprocessableEntityException;
import io.leitstand.commons.messages.Messages;
import io.leitstand.event.webhook.service.WebhookId;
import io.leitstand.event.webhook.service.WebhookName;
import io.leitstand.event.webhook.service.WebhookService;
import io.leitstand.event.webhook.service.WebhookSettings;
import io.leitstand.event.webhook.service.WebhookTemplate;

@RunWith(MockitoJUnitRunner.class)
public class WebhookResourceTest {
	
	private static final WebhookId WEBHOOK_ID = randomWebhookId();
	private static final WebhookName WEBHOOK_NAME = webhookName("hook");
	
	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Mock
	private Messages messages;
	
	@Mock
	private WebhookService service;
	
	@InjectMocks
	private WebhookResource resource = new WebhookResource();
	
	
	@Test
	public void send_created_response_when_putting_a_new_webhook() {
		WebhookSettings settings = newWebhookSettings()
								   .withWebhookId(WEBHOOK_ID)
								   .withWebhookName(WEBHOOK_NAME)
								   .build();
		
		when(service.storeWebhook(settings)).thenReturn(true);

		Response response = resource.storeWebhookSettings(WEBHOOK_ID, settings);
		assertEquals(201,response.getStatus());
	}
	
	@Test
	public void send_success_response_when_putting_an_existing_webhook() {
		WebhookSettings settings = newWebhookSettings()
				   				   .withWebhookId(WEBHOOK_ID)
				   				   .withWebhookName(WEBHOOK_NAME)
				   				   .build();

		when(service.storeWebhook(settings)).thenReturn(false);

		Response response = resource.storeWebhookSettings(WEBHOOK_ID, settings);
		assertEquals(200,response.getStatus());
	}
	
	@Test
	public void send_created_response_when_posting_a_new_webhook() {
		WebhookSettings settings = newWebhookSettings()
								   .withWebhookId(WEBHOOK_ID)
								   .withWebhookName(WEBHOOK_NAME)
								   .build();
		
		when(service.storeWebhook(settings)).thenReturn(true);

		Response response = resource.storeWebhookSettings(settings);
		assertEquals(201,response.getStatus());
	}
	
	@Test
	public void send_success_response_when_posting_an_existing_webhook() {
		WebhookSettings settings = newWebhookSettings()
				   				   .withWebhookId(WEBHOOK_ID)
				   				   .withWebhookName(WEBHOOK_NAME)
				   				   .build();

		when(service.storeWebhook(settings)).thenReturn(false);

		Response response = resource.storeWebhookSettings(settings);
		assertEquals(200,response.getStatus());
	}
	
	@Test
	public void throw_UnprocessableEntityException_when_webhook_id_conflicts_when_putting_a_webhook() {
		exception.expect(UnprocessableEntityException.class);
		exception.expect(reason(VAL0003E_IMMUTABLE_ATTRIBUTE));
		
		WebhookSettings settings = newWebhookSettings()
								   .withWebhookId(WEBHOOK_ID)
								   .withWebhookName(WEBHOOK_NAME)
								   .build();

		resource.storeWebhookSettings(randomWebhookId(), settings);
	}

	@Test
	public void throw_UnprocessableEntityException_when_webhook_id_conflicts_when_putting_a_webhook_template() {
		exception.expect(UnprocessableEntityException.class);
		exception.expect(reason(VAL0003E_IMMUTABLE_ATTRIBUTE));
		
		WebhookTemplate settings = newWebhookTemplate()
								   .withWebhookId(WEBHOOK_ID)
								   .withWebhookName(WEBHOOK_NAME)
								   .build();

		resource.storeWebhookTemplate(randomWebhookId(), settings);
		
	}
	
	
	@Test
	public void send_success_response_when_putting_a_webhook_template() {
		
		WebhookTemplate settings = newWebhookTemplate()
								   .withWebhookId(WEBHOOK_ID)
								   .withWebhookName(WEBHOOK_NAME)
								   .build();

		Response response = resource.storeWebhookTemplate(WEBHOOK_ID, settings);
		assertEquals(200,response.getStatus());
	}
	
}
