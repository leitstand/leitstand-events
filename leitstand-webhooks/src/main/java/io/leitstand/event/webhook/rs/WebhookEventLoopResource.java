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

import static io.leitstand.commons.messages.MessageFactory.createMessage;
import static io.leitstand.event.webhook.service.ReasonCode.WHK0100I_WEBHOOK_EVENT_LOOP_STARTED;
import static io.leitstand.event.webhook.service.ReasonCode.WHK0101I_WEBHOOK_EVENT_LOOP_STOPPED;
import static io.leitstand.security.auth.Role.ADMINISTRATOR;
import static io.leitstand.security.auth.Role.SYSTEM;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import io.leitstand.commons.messages.Messages;
import io.leitstand.event.webhook.service.WebhookEventLoopService;
import io.leitstand.event.webhook.service.WebhookEventLoopStatus;

@RequestScoped
@Path("/webhooks")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class WebhookEventLoopResource {

	@Inject
	private WebhookEventLoopService service;
	
	@Inject
	private Messages messages;
	
	@POST
	@Path("/_start")
	@RolesAllowed({SYSTEM,ADMINISTRATOR})
	public Messages startEventLoop() {
		service.startEventLoop();
		messages.add(createMessage(WHK0100I_WEBHOOK_EVENT_LOOP_STARTED));
		return messages;
	}
	
	@POST
	@Path("/_stop")
	@RolesAllowed({SYSTEM,ADMINISTRATOR})
	public Messages stopEventLoop() {
		service.stopEventLoop();
		messages.add(createMessage(WHK0101I_WEBHOOK_EVENT_LOOP_STOPPED));
		return messages;
	}
	
	@GET
	@Path("/_status")
	@RolesAllowed({SYSTEM,ADMINISTRATOR})
	public WebhookEventLoopStatus getStatus() {
		return service.getWebhookEventLoopStatus();
	}
	
}
