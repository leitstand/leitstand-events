/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
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
