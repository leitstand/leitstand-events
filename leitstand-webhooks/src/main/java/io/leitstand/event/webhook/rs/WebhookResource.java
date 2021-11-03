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

import static io.leitstand.commons.model.ObjectUtil.isDifferent;
import static io.leitstand.commons.model.Patterns.UUID_PATTERN;
import static io.leitstand.commons.rs.ReasonCode.VAL0003E_IMMUTABLE_ATTRIBUTE;
import static io.leitstand.commons.rs.Responses.created;
import static io.leitstand.commons.rs.Responses.success;
import static io.leitstand.event.webhook.rs.Scopes.ADM;
import static io.leitstand.event.webhook.rs.Scopes.ADM_READ;
import static io.leitstand.event.webhook.rs.Scopes.ADM_WEBHOOKS;
import static io.leitstand.event.webhook.rs.Scopes.ADM_WEBHOOKS_READ;
import static io.leitstand.event.webhook.service.MessageFilter.newMessageFilter;
import static io.leitstand.event.webhook.service.MessageState.messageState;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import io.leitstand.commons.UnprocessableEntityException;
import io.leitstand.commons.messages.Messages;
import io.leitstand.commons.rs.Resource;
import io.leitstand.event.queue.service.DomainEventId;
import io.leitstand.event.webhook.service.MessageFilter;
import io.leitstand.event.webhook.service.WebhookId;
import io.leitstand.event.webhook.service.WebhookMessage;
import io.leitstand.event.webhook.service.WebhookMessages;
import io.leitstand.event.webhook.service.WebhookName;
import io.leitstand.event.webhook.service.WebhookService;
import io.leitstand.event.webhook.service.WebhookSettings;
import io.leitstand.event.webhook.service.WebhookStatistics;
import io.leitstand.security.auth.Scopes;

@Resource
@Path("/webhooks")
@Scopes({ADM, ADM_WEBHOOKS})
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class WebhookResource {

	@Inject
	private WebhookService service;
	
	@Inject
	private Messages messages;
	
	@GET
	@Path("/{hook_id:"+UUID_PATTERN+"}/settings")
	@Scopes({ADM,ADM_READ, ADM_WEBHOOKS_READ, ADM_WEBHOOKS})
	public WebhookSettings getWebhookSettings(@PathParam("hook_id") WebhookId hookId) {
		return service.getWebhook(hookId);
	}
	
	@GET
	@Path("/{hook_name}/settings")
	@Scopes({ADM,ADM_READ, ADM_WEBHOOKS_READ, ADM_WEBHOOKS})
	public WebhookSettings getWebhookSettings(@Valid @PathParam("hook_name") WebhookName hookName) {
		return service.getWebhook(hookName);
	}
	
	@PUT
	@Path("/{hook_id:"+UUID_PATTERN+"}/settings")
	public Response storeWebhookSettings(@Valid @PathParam("hook_id") WebhookId hookId,
										 @Valid WebhookSettings settings) {
		if(isDifferent(hookId, settings.getWebhookId())) {
			throw new UnprocessableEntityException(VAL0003E_IMMUTABLE_ATTRIBUTE, 
					   								"hook_id", 
					   								hookId, 
					   								settings.getWebhookId());
		}
		
		if(service.storeWebhook(settings)) {
			return created(messages, hookId);
		}
		return success(messages);
	}
	
	@PUT
    @Path("/{hook_name}/settings")
    public Response storeWebhookSettings(@Valid @PathParam("hook_name") WebhookName hookName,
                                         @Valid WebhookSettings settings) {
        if(service.storeWebhook(settings)) {
            return created(messages, settings.getWebhookId());
        }
        return success(messages);
    }
	
	@POST
	public Response storeWebhookSettings(@Valid WebhookSettings settings) {
		
		if(service.storeWebhook(settings)) {
			return created(messages,settings.getWebhookId());
		}
		return success(messages);	
	}
	
	@GET
	@Path("/{hook_name}/messages/{event:"+UUID_PATTERN+"}")
	@Scopes({ADM,ADM_READ, ADM_WEBHOOKS_READ, ADM_WEBHOOKS})
	public WebhookMessage getMessage(@Valid @PathParam("hook_name") WebhookName hookName, 
									 @Valid @PathParam("event") DomainEventId eventId) {
		return service.getMessage(hookName, eventId);
	}
	
	@GET
	@Path("/{hook_id:"+UUID_PATTERN+"}/messages/{event:"+UUID_PATTERN+"}")
	@Scopes({ADM,ADM_READ, ADM_WEBHOOKS_READ, ADM_WEBHOOKS})
	public WebhookMessage getMessage(@Valid @PathParam("hook_id") WebhookId hookId, 
									 @Valid @PathParam("event") DomainEventId eventId) {
		return service.getMessage(hookId,
								  eventId);
	}
	
	@GET
	@Path("/{hook_name}/messages")
	@Scopes({ADM,ADM_READ, ADM_WEBHOOKS_READ, ADM_WEBHOOKS})
	public WebhookMessages findMessages(@Valid @PathParam("hook_name") WebhookName hookName,
										@QueryParam("state") String state,
										@QueryParam("correlationId") String correlationId,
										@QueryParam("offset") int offset,
										@QueryParam("size") int size){

		MessageFilter filter = newMessageFilter()
							   .withMessageState(messageState(state))
							   .withCorrelationId(correlationId)
							   .withOffset(offset)
							   .withLimit(size)
							   .build();
		
		return service.findMessages(hookName,filter);
	}
	
	@GET
	@Path("/{hook_id:"+UUID_PATTERN+"}/messages")
	@Scopes({ADM,ADM_READ, ADM_WEBHOOKS_READ, ADM_WEBHOOKS})
	public WebhookMessages findMessages(@Valid @PathParam("hook_id") WebhookId hookId,
										@QueryParam("correlationId") String correlationId,
										@QueryParam("state") String state,
									   	@QueryParam("offset") int offset,
									   	@QueryParam("size") int size){
		
		MessageFilter filter = newMessageFilter()
							   .withMessageState(messageState(state))
							   .withCorrelationId(correlationId)
							   .withOffset(offset)
							   .withLimit(size)
							   .build();

		return service.findMessages(hookId,filter);
	}
	
	@POST
	@Path("/{hook_name}/messages/{event:"+UUID_PATTERN+"}/_retry")
	public Response retryMessage(@Valid @PathParam("hook_name") WebhookName hookName, 
								 @Valid @PathParam("event") DomainEventId eventId) {
		service.getMessage(hookName, eventId);
		return success(messages);
	}
	
	@POST
	@Path("/{hook_id:"+UUID_PATTERN+"}/messages/{event:"+UUID_PATTERN+"}/_retry")
	public Response retryMessage(@Valid @PathParam("hook_id") WebhookId hookId, 
								 @Valid @PathParam("event") DomainEventId eventId) {
		service.getMessage(hookId, eventId);
		return success(messages);
	}
	
	@POST
	@Path("/{hook_name}/_reset")
	public Response resetWebhook(@Valid @PathParam("hook_name") WebhookName hookName, 
								 @Valid @QueryParam("event_id") DomainEventId eventId) {
		service.resetWebhook(hookName,
							 eventId);
		return success(messages);
	}
	
	@POST
	@Path("/{hook_id:"+UUID_PATTERN+"}/_reset")
	public Response resetWebhook(@Valid @PathParam("hook_id") WebhookId hookId, 
		    					 @Valid @QueryParam("event_id") DomainEventId eventId) {
		service.resetWebhook(hookId,
						   	 eventId);
		return success(messages);
	}
	
	@POST
	@Path("/{hook_name}/_retry")
	public Response retryFailedCalls(@Valid @PathParam("hook_name") WebhookName hookName) {
		service.retryWebhook(hookName);
		return success(messages);
	}
	
	@POST
	@Path("/{hook_id:"+UUID_PATTERN+"}/_retry")
	public Response retryFailedCalls(@Valid @PathParam("hook_id") WebhookId hookId) {
		service.retryWebhook(hookId);
		return success(messages);
	}
	
	
	@POST
	@Path("/{hook_name}/_disable")
	public Response disableWebhook(@Valid @PathParam("hook_name") WebhookName hookName) {
		service.disableWebhook(hookName);
		return success(messages);
	}
	
	@POST
	@Path("/{hook_id:"+UUID_PATTERN+"}/_disable")
	public Response disableWebhook(@Valid @PathParam("hook_id") WebhookId hookId) {
		service.disableWebhook(hookId);
		return success(messages);
	}
	
	@POST
	@Path("/{hook_name}/_enable")
	public Response enableWebhook(@Valid @PathParam("hook_name") WebhookName hookName) {
		service.enableWebhook(hookName);
		return success(messages);
	}
	
	@POST
	@Path("/{hook_id:"+UUID_PATTERN+"}/_enable")
	public Response enableWebhook(@Valid @PathParam("hook_id") WebhookId hookId) {
		service.enableWebhook(hookId);
		return success(messages);
	}
	
	@DELETE
	@Path("/{hook_id:"+UUID_PATTERN+"}")
	public Response removeWebhook(@Valid @PathParam("hook_id") WebhookId hookId) {
		service.removeWebhook(hookId);
		return success(messages);
	}
	
	@DELETE
	@Path("/{hook_name}")
	public Response removeWebhook(@Valid @PathParam("hook_name") WebhookName hookName) {
		service.removeWebhook(hookName);
		return success(messages);
	}
		
	@GET
	@Path("/{hook_id:"+UUID_PATTERN+"}/statistics")
	@Scopes({ADM,ADM_READ, ADM_WEBHOOKS_READ, ADM_WEBHOOKS})
	public WebhookStatistics getWebhookStatistics(@PathParam("hook_id") WebhookId hookId) {
		return service.getWebhookStatistics(hookId);
	}
	
	@GET
	@Path("/{hook_name}/statistics")
	@Scopes({ADM,ADM_READ, ADM_WEBHOOKS_READ, ADM_WEBHOOKS})
	public WebhookStatistics getWebhookStatistics(@Valid @PathParam("hook_name") WebhookName hookName) {
		return service.getWebhookStatistics(hookName);
	}
}
