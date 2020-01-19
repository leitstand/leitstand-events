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

import static io.leitstand.commons.template.TemplateService.newTemplateService;
import static javax.json.Json.createObjectBuilder;
import static org.junit.Assert.assertEquals;

import javax.json.JsonObject;

import org.junit.Test;

public class WebhookRewritingServiceTest {

	private WebhookRewritingService service = new WebhookRewritingService(newTemplateService()); 
	
	
	@Test
	public void use_original_payload_if_no_template_is_specified() {
		JsonObject payload = createObjectBuilder().add("unit","test").build();
		Webhook webhook = new Webhook();
		String message = service.rewritePayload(webhook, payload);
		assertEquals(payload.toString(),message);
		
	}
	
	@Test
	public void use_original_payload_if_empty_template_is_specified() {
		JsonObject payload = createObjectBuilder().add("unit","test").build();
		Webhook webhook = new Webhook();
		webhook.setTemplate("");
		String message = service.rewritePayload(webhook, payload);
		assertEquals(payload.toString(),message);		
	}
	
	@Test
	public void rewrite_payload_if_template_is_specified() {
		JsonObject payload = createObjectBuilder().add("unit","test").build();
		Webhook webhook = new Webhook();
		webhook.setTemplate("{{unit}}");
		String message = service.rewritePayload(webhook, payload);
		assertEquals("test",message);
	}
	
}
