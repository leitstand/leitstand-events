/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.webhook.model;

import static io.leitstand.commons.json.MapUnmarshaller.unmarshal;
import static io.leitstand.commons.model.StringUtil.isEmptyString;
import static io.leitstand.commons.template.TemplateProcessor.plain;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.json.JsonObject;

import io.leitstand.commons.template.Template;
import io.leitstand.commons.template.TemplateService;
import io.leitstand.event.webhook.service.Endpoint;

@Dependent
public class WebhookRewritingService {

	private TemplateService templates;
	
	@Inject
	public WebhookRewritingService(TemplateService templates) {
		this.templates = templates;
	}
	
	public String rewritePayload(Webhook webhook, JsonObject payload) {
		String templateDefinition = webhook.getTemplate();
		if(isEmptyString(templateDefinition)) {
			return payload.toString();
		}
		Template<String> template = templates.compileTemplate(templateDefinition, plain());
		return template.apply(unmarshal(payload).toMap());
	}
	
	public Endpoint rewriteEndpoint(Webhook webhook, JsonObject payload) {
		Endpoint endpoint = webhook.getEndpoint();
		Template<String> template = templates.compileTemplate(endpoint.getValue(), plain());
		return Endpoint.valueOf(template.apply(unmarshal(payload).toMap()));
	}
	
}