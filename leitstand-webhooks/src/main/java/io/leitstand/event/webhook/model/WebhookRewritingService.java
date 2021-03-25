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
	
	public Endpoint rewriteEndpoint(Webhook webhook, JsonObject payload) {
		Endpoint endpoint = webhook.getEndpoint();
		Template<String> template = templates.compileTemplate(endpoint.getValue(), plain());
		return Endpoint.valueOf(template.apply(unmarshal(payload).toMap()));
	}
	
}
