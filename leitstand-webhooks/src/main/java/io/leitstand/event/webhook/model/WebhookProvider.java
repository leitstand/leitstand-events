/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
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
