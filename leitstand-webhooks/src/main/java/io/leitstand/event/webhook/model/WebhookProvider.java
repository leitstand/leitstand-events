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

import static io.leitstand.event.webhook.model.Webhook.findWebhookById;
import static io.leitstand.event.webhook.service.ReasonCode.WHK0002E_WEBHOOK_NOT_FOUND;
import static java.lang.String.format;

import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import io.leitstand.commons.EntityNotFoundException;
import io.leitstand.commons.model.Repository;
import io.leitstand.event.webhook.service.WebhookId;
import io.leitstand.event.webhook.service.WebhookName;

@Dependent
public class WebhookProvider {
	
	private static final Logger LOG = Logger.getLogger(WebhookProvider.class.getName());

	private Repository repository;

	protected WebhookProvider() {
		// JPA
	}
	
	@Inject
	protected WebhookProvider(@Webhooks Repository repository) {
		this.repository = repository;
	}
	
	public Webhook tryFetchWebhook(WebhookId webhookId) {
		return repository.execute(findWebhookById(webhookId));
	}
	
	public Webhook tryFetchWebhook(WebhookName webhookName) {
		return repository.execute(Webhook.findWebhookByName(webhookName));
	}
	
	public Webhook fetchWebhook(WebhookId webhookId) {
		Webhook hook = tryFetchWebhook(webhookId);
		if(hook == null) {
			LOG.fine(() -> format("%s: webhook %s not found!", 
							  	  WHK0002E_WEBHOOK_NOT_FOUND.getReasonCode(),
							  	  webhookId)); 
			throw new EntityNotFoundException(WHK0002E_WEBHOOK_NOT_FOUND, 
											  webhookId);
		}
		return hook;
	}

	public Webhook fetchWebhook(WebhookName webhookName) {
		Webhook hook = tryFetchWebhook(webhookName);
		if(hook == null) {
			LOG.fine(() -> format("%s: webhook %s not found!", 
							  	  WHK0002E_WEBHOOK_NOT_FOUND.getReasonCode(),
							  	  webhookName)); 
			throw new EntityNotFoundException(WHK0002E_WEBHOOK_NOT_FOUND, 
											  webhookName);
		}
		return hook;
	}
	
}
