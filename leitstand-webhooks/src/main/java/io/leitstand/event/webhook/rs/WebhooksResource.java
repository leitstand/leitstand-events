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


import static io.leitstand.event.webhook.rs.Scopes.ADM;
import static io.leitstand.event.webhook.rs.Scopes.ADM_READ;
import static io.leitstand.event.webhook.rs.Scopes.ADM_WEBHOOKS;
import static io.leitstand.event.webhook.rs.Scopes.ADM_WEBHOOKS_READ;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.created;

import java.net.URI;
import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import io.leitstand.commons.messages.Messages;
import io.leitstand.commons.rs.Resource;
import io.leitstand.event.webhook.service.WebhookReference;
import io.leitstand.event.webhook.service.WebhookService;
import io.leitstand.event.webhook.service.WebhookSettings;
import io.leitstand.security.auth.Scopes;

@Resource
@Path("/webhooks")
@Scopes({ADM, ADM_WEBHOOKS})
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class WebhooksResource {

	@Inject
	private WebhookService service;
	
	@Inject
	private Messages messages;
	
	@GET
	@Scopes({ADM,ADM_READ, ADM_WEBHOOKS_READ, ADM_WEBHOOKS})
	public List<WebhookReference> findWebhooks(@QueryParam("filter") @DefaultValue(".*") String filter){
		return service.findWebhooks(filter);
	}
	
	@POST
	public Response addWebhook(@Valid WebhookSettings settings) {
		service.storeWebhook(settings);
		return created(URI.create(settings.getWebhookId().toString()))
			   .entity(messages)
			   .build();
	}
}
