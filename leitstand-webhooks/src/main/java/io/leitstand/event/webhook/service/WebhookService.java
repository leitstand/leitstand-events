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
package io.leitstand.event.webhook.service;

import java.util.List;

import io.leitstand.event.queue.service.DomainEventId;

/**
 * A transactional service to maintain Webhook settings.
 * <p>
 * A webhook consumes domain events and forwards them via HTTP to configured endpoints.
 * <p>
 * Every webhook has an immutable UUID an a unique but editable name.
 * All functions are avialbe using both, the UUID or the name. Using the UUID is recommended for persistent links.
 * </p>
 * The following authentication means are available:
 * <ul>
 * 	<li>Unauthenticated endpoint invocation</li>
 * 	<li>Basic Authentication, i.e. endpoint authentication with user credentials</li>
 *  <li>Bearer Token, i.e. authentication with an API access key </li>
 * </ul>
 * All credentials are AES encrypted and protected with the Leitstand master secret.
 * <p>
 * Every webhook can have an optional template to rewrite domain event messages.
 * <p>
 * Webhooks can be disabled to suspend message processing.
 * 
 * @see WebhookId
 * @see WebhookName
 * @see WebhookSettings
 */
public interface WebhookService {

	/**
	 * Disable a webhook to stop domain event processing.
	 * @param hookId the ID of the webhook to be disabled
	 */
	void disableWebhook(WebhookId hookId);
	
	/**
	 * Disable a webhookd to stop domain event processing.
	 * @param hookName the name of the webhook to be disabled
	 */
	void disableWebhook(WebhookName hookName);
	
	/**
	 * Enables a webhook to resume domain event processing.
	 * The webhook will consume all events that occured while being disabled.
	 * @param hookId the name of the webhook to be enabled
	 */
	void enableWebhook(WebhookId hookId);
	
	/**
	 * Enables a webhook to resume domain event processing.
	 * The webhook will consume all events that occured while being disabled.	 
	 * @param hookName the name of the webhook to be enabled
	 */
	void enableWebhook(WebhookName hookName);
	
	/**
	 * Returns a list of existing webhooks.
	 * @param filter an optional regula expression to filter for webhook names
	 * @return a list of matching webhooks or an empty list if no matches exist.
	 */
	List<WebhookReference> findWebhooks(String filter);
	
	/**
	 * Returns the message created by this webhook for a certain domain event.
	 * If a template is configured, the message returns the rewritten domain event.
	 * @param hookId the webhook ID
	 * @param eventId the event ID
	 * @return the message sent by this webhook.
	 */
	WebhookMessage getMessage(WebhookId hookId, DomainEventId eventId);

	/**
	 * Returns the message created by this webhook for a certain domain event.
	 * If a template is configured, the message returns the rewritten domain event.
	 * @param hookId the webhook name
	 * @param eventId the event ID
	 * @return the message sent by this webhook.
	 */
	WebhookMessage getMessage(WebhookName hookName, DomainEventId eventId);	

	/**
	 * Returns the general webhook settings.
	 * @param hookId the webhook ID
	 * @return the general webhook settings
	 * @throws EntitNotFoundException if the webhook does not exist.
	 */
	WebhookSettings getWebhook(WebhookId hookId);

	/**
	 * Returns the general webhook settings.
	 * @param hookId the webhook ID
	 * @return the general webhook settings
	 * @throws EntitNotFoundException if the webhook does not exist.
	 */
	WebhookSettings getWebhook(WebhookName hookName);
	
	/**
	 * Removes a webhook. 
	 * Fails silently if the webhook does not exist.
	 * @param hookId the webhook ID.
	 */
	void removeWebhook(WebhookId id);

	/**
	 * Removes a webhook. 
	 * Fails silently if the webhook does not exist.
	 * @param hookName the webhook name.
	 */
	void removeWebhook(WebhookName hookName);
	
	/**
	 * Resets a webhook to process a domain event and all events occurred after that event again.
	 * @param hookId the webhook ID
	 * @param eventId the domain event ID
	 */
	void resetWebhook(WebhookId hookId, 
					  DomainEventId eventId);

	/**
	 * Resets a webhook to process a domain event and all events occurred after that event again.
	 * @param hookName the webhook name
	 * @param eventId the domain event ID
	 */
	void resetWebhook(WebhookName hookName, 
					  DomainEventId eventId);
	
	
	/**
	 * Resets a webhook to process a domain event and all events occurred after that event again.
	 * @param hookId the webhook ID
	 */
	void retryWebhook(WebhookId hookId);

	/**
	 * Resets a webhook to process a domain event and all events occurred after that event again.
	 * @param hookName the webhook name
	 */
	void retryWebhook(WebhookName hookName);
	
	/**
	 * Stores a webhook. 
	 * @param settings the webhook settings
	 * @return <code>true</code> if a new webhook was added to the system and <code>false</code> if an existing webhook was updated.
	 */
	boolean storeWebhook(WebhookSettings settings);
	
	WebhookStatistics getWebhookStatistics(WebhookId hookId);
	WebhookStatistics getWebhookStatistics(WebhookName hookName);
	WebhookMessages findMessages(WebhookId hookId, MessageFilter filter);
	WebhookMessages findMessages(WebhookName hookName, MessageFilter filter);

}
