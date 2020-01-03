/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.webhook.rs;


import static io.leitstand.security.auth.Role.ADMINISTRATOR;
import static io.leitstand.security.auth.Role.SYSTEM;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.created;

import java.net.URI;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
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
import io.leitstand.event.webhook.service.WebhookReference;
import io.leitstand.event.webhook.service.WebhookService;
import io.leitstand.event.webhook.service.WebhookSettings;

@RequestScoped
@Path("/webhooks")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class WebhooksResource {

	@Inject
	private WebhookService service;
	
	@Inject
	private Messages messages;
	
	@GET
	@RolesAllowed({ADMINISTRATOR,SYSTEM})
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
