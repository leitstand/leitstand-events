/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.webhook.model;

import static io.leitstand.commons.jpa.SerializableJsonObjectConverter.parseJson;
import static io.leitstand.commons.json.NullSafeJsonObjectBuilder.createNullSafeJsonObjectBuilder;
import static io.leitstand.commons.jsonb.IsoDateAdapter.isoDateFormat;
import static io.leitstand.commons.messages.MessageFactory.createMessage;
import static io.leitstand.commons.model.ByteArrayUtil.decodeBase64String;
import static io.leitstand.commons.model.ByteArrayUtil.encodeBase64String;
import static io.leitstand.commons.model.ObjectUtil.isDifferent;
import static io.leitstand.commons.model.StringUtil.fromUtf8Bytes;
import static io.leitstand.commons.model.StringUtil.isEmptyString;
import static io.leitstand.event.queue.model.Message.findMessageByDomainEventId;
import static io.leitstand.event.queue.service.DomainEvent.newDomainEvent;
import static io.leitstand.event.queue.service.ReasonCode.BUS0002E_MESSAGE_NOT_FOUND;
import static io.leitstand.event.webhook.model.Webhook.findWebhooksByName;
import static io.leitstand.event.webhook.model.Webhook_Message.findMessage;
import static io.leitstand.event.webhook.model.Webhook_Message.findWebhookMessages;
import static io.leitstand.event.webhook.model.Webhook_Message.resetWebhookCalls;
import static io.leitstand.event.webhook.model.Webhook_Message.retryFailedCalls;
import static io.leitstand.event.webhook.service.MessageReference.newMessageReference;
import static io.leitstand.event.webhook.service.ReasonCode.WHK0001I_WEBHOOK_STORED;
import static io.leitstand.event.webhook.service.ReasonCode.WHK0003I_WEBHOOK_REMOVED;
import static io.leitstand.event.webhook.service.ReasonCode.WHK0004I_WEBHOOK_RESET;
import static io.leitstand.event.webhook.service.ReasonCode.WHK0007I_WEBHOOK_DISABLED;
import static io.leitstand.event.webhook.service.ReasonCode.WHK0008I_WEBHOOK_ENABLED;
import static io.leitstand.event.webhook.service.ReasonCode.WHK0010E_WEBHOOK_BASIC_AUTH_PASSWORD_MISMATCH;
import static io.leitstand.event.webhook.service.WebhookMessage.newWebhookMessage;
import static io.leitstand.event.webhook.service.WebhookMessages.newWebhookMessages;
import static io.leitstand.event.webhook.service.WebhookReference.newWebhookRef;
import static io.leitstand.event.webhook.service.WebhookSettings.newWebhookSettings;
import static io.leitstand.event.webhook.service.WebhookTemplate.newWebhookTemplate;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.json.JsonObject;

import io.leitstand.commons.EntityNotFoundException;
import io.leitstand.commons.messages.Messages;
import io.leitstand.commons.model.Repository;
import io.leitstand.commons.model.Service;
import io.leitstand.event.queue.model.Message;
import io.leitstand.event.queue.model.Topic;
import io.leitstand.event.queue.model.TopicProvider;
import io.leitstand.event.queue.service.DomainEventId;
import io.leitstand.event.webhook.service.MessageFilter;
import io.leitstand.event.webhook.service.MessageReference;
import io.leitstand.event.webhook.service.MessageState;
import io.leitstand.event.webhook.service.WebhookId;
import io.leitstand.event.webhook.service.WebhookMessage;
import io.leitstand.event.webhook.service.WebhookMessages;
import io.leitstand.event.webhook.service.WebhookName;
import io.leitstand.event.webhook.service.WebhookReference;
import io.leitstand.event.webhook.service.WebhookService;
import io.leitstand.event.webhook.service.WebhookSettings;
import io.leitstand.event.webhook.service.WebhookStatistics;
import io.leitstand.event.webhook.service.WebhookTemplate;
import io.leitstand.security.crypto.MasterSecret;

@Service
public class DefaultWebhookService implements WebhookService{
	
	private static final Logger LOG = Logger.getLogger(DefaultWebhookService.class.getName());
	
	@Inject
	@Webhooks
	private Repository repository;
	
	@Inject
	private WebhookProvider webhooks;
	
	@Inject
	private Messages messages;
	
	@Inject
	private MasterSecret secret;
	
	@Inject
	private TopicProvider topics;
	
	@Inject
	private WebhookRewritingService rewriter;
	
	@Inject
	private WebhookStatisticsService statistics;
	
	public DefaultWebhookService() {
		//CDI
	}
	
	public DefaultWebhookService(Repository repository,
								 TopicProvider topics,
								 Messages messages,
								 MasterSecret secret,
								 WebhookProvider webhooks,
								 WebhookRewritingService rewriter,
								 WebhookStatisticsService statistics) {
		this.repository = repository;
		this.topics	    = topics;
		this.messages	= messages;
		this.secret		= secret;
		this.webhooks   = webhooks;
		this.rewriter 	= rewriter;
		this.statistics = statistics;
	}
	
	private String decrypt64(String encrypted64) {
		if(isEmptyString(encrypted64)) {
			return null;
		}
		return fromUtf8Bytes(secret.decrypt(decodeBase64String(encrypted64)));
	}
	
	private String encrypt64(String plainText) {
		if(isEmptyString(plainText)) {
			return null;
		}
		return encodeBase64String(secret.encrypt(plainText));
	}
	
	/** {@inheritDoc */
	@Override
	public List<WebhookReference> findWebhooks(String filter) {
		return repository.execute(findWebhooksByName(filter))
						 .stream()
						 .map(hook -> newWebhookRef()
								 	  .withWebhookId(hook.getWebhookId())
								 	  .withWebhookName(hook.getWebhookName())
								 	  .withTopicName(hook.getTopicName())
								 	  .withSelector(hook.getSelector())
								 	  .withDescription(hook.getDescription())
								 	  .withEnabled(hook.isEnabled())
								 	  .build())
						 			  .collect(toList());
	}

	/** {@inheritDoc */
	@Override
	public boolean storeWebhook(WebhookSettings settings) {
		if(isDifferent(settings.getPassword(), 
					   settings.getConfirmPassword())) {
			LOG.fine(() -> format("%s: Confirmed password mismatches the given password. User: %s, Webhook: %s",
								  WHK0010E_WEBHOOK_BASIC_AUTH_PASSWORD_MISMATCH.getReasonCode(),
								  settings.getUserId(),
								  settings.getWebhookName()));
			messages.add(createMessage(WHK0010E_WEBHOOK_BASIC_AUTH_PASSWORD_MISMATCH,
									   settings.getWebhookId(),
									   settings.getWebhookName()));
			return false;
		}
		
		Topic topic = topics.getOrCreateTopic(settings.getTopicName());
		Webhook hook = webhooks.tryFetchWebhook(settings.getWebhookId());
		if(hook == null) {
			hook = new Webhook(topic,
							   settings.getWebhookId(),
							   settings.getWebhookName());
			repository.add(hook);
			LOG.fine(() -> format("%s: New webhook %s created",
								  WHK0001I_WEBHOOK_STORED.getReasonCode(),
								  settings.getWebhookName()));
		}
		
		hook.setWebhookName(settings.getWebhookName());
		hook.setDescription(settings.getDescription());
		hook.setTopic(topic);
		hook.setSelector(settings.getSelector());
		hook.setEndpoint(settings.getEndpoint());
		hook.setHttpMethod(settings.getMethod());
		hook.setUserId(settings.getUserId());
		hook.setPassword64(encrypt64(settings.getPassword()));
		hook.setAccessKey64(encrypt64(settings.getAccesskey()));
		hook.setBatchSize(settings.getBatchSize());
		if(settings.isEnabled()) {
			hook.enable();
		} else {
			hook.disable();
		}
		
		
		LOG.fine(() -> format("%s: Webhook %s configured",
				  			  WHK0001I_WEBHOOK_STORED.getReasonCode(),
				  			  settings.getWebhookName()));

		return hook.getId() == null;
	}

	/** {@inheritDoc */
	@Override
	public WebhookSettings getWebhook(WebhookId id) {
		Webhook hook = webhooks.fetchWebhook(id);
		return webhookSettings(hook);

	}

	/** {@inheritDoc */
	@Override
	public WebhookSettings getWebhook(WebhookName name) {
		Webhook hook = webhooks.fetchWebhook(name);
		return webhookSettings(hook);
	}


	private WebhookSettings webhookSettings(Webhook hook) {
		return newWebhookSettings()
			   .withWebhookId(hook.getWebhookId())
			   .withWebhookName(hook.getWebhookName())
			   .withEnabled(hook.isEnabled())
			   .withTopicName(hook.getTopicName())
			   .withSelector(hook.getSelector())
			   .withBatchSize(hook.getBatchSize())
			   .withDescription(hook.getDescription())
			   .withEndpoint(hook.getEndpoint())
			   .withMethod(hook.getHttpMethod())
			   .withAccesskey(decrypt64(hook.getAccessKey64()))
			   .withUserId(hook.getUser())
			   .withPassword(decrypt64(hook.getPassword64()))
			   .build();		
	}
	

	/** {@inheritDoc */
	@Override
	public void removeWebhook(WebhookId id) {
		Webhook hook = webhooks.tryFetchWebhook(id);
		if(hook != null) {
			repository.remove(hook);
			LOG.fine(() -> format("%s: Removed webhook %s", 
						   		  WHK0003I_WEBHOOK_REMOVED.getReasonCode(),
						   		  id));
			messages.add(createMessage(WHK0003I_WEBHOOK_REMOVED, 
									   hook.getWebhookName()));
		}
	}

	/** {@inheritDoc */
	@Override
	public void removeWebhook(WebhookName name) {
		Webhook hook = webhooks.tryFetchWebhook(name);
		if(hook != null) {
			repository.remove(hook);
			LOG.fine(() -> format("%s: Removed webhook %s", 
						   		  WHK0003I_WEBHOOK_REMOVED.getReasonCode(),
						   		  name));
			messages.add(createMessage(WHK0003I_WEBHOOK_REMOVED, 
									   name));
		}		
	}

	/** {@inheritDoc */
	@Override
	public WebhookMessage getMessage(WebhookId hookId, 
									 DomainEventId eventId) {
		Webhook webhook = webhooks.fetchWebhook(hookId);
		Webhook_Message message = repository.execute(findMessage(webhook, eventId));
		if(message == null) {
			throw new EntityNotFoundException(BUS0002E_MESSAGE_NOT_FOUND, 
											  webhook.getWebhookName(),
											  eventId);
		}
		return webhookMessage(message);
		
	}
	
	/** {@inheritDoc */
	@Override
	public WebhookMessage getMessage(WebhookName hookName, 
									 DomainEventId eventId) {
		Webhook webhook = webhooks.fetchWebhook(hookName);
		Webhook_Message message = repository.execute(findMessage(webhook, eventId));
		if(message == null) {
			throw new EntityNotFoundException(BUS0002E_MESSAGE_NOT_FOUND, 
											  webhook.getWebhookName(),
											  eventId);
		}
		return webhookMessage(message);
		
	}

	private WebhookMessage webhookMessage(Webhook_Message webhookMessage) {
		Webhook webhook = webhookMessage.getWebhook();
		Message message = webhookMessage.getMessage();
		JsonObject jsonPayload =  parseJson(message.getPayload());
		JsonObject requestEntity = createNullSafeJsonObjectBuilder()
						   		  .add("event_id",message.getDomainEventId().toString())
						   		  .add("event_name",message.getDomainEventName().toString())
						   		  .add("correlation_id",message.getCorrelationId())
						   		  .add("message",jsonPayload)
						   		  .add("topic_name", message.getTopicName().toString())
						   		  .add("date_created",isoDateFormat(message.getDateCreated()))
						   		  .build();		
		
		return newWebhookMessage()
			   .withWebhookId(webhook.getWebhookId())
			   .withWebhookName(webhook.getWebhookName())
			   .withTopicName(webhook.getTopicName())
			   .withSelector(webhook.getSelector())
			   .withDescription(webhook.getDescription())
			   .withEnabled(webhook.isEnabled())
			   .withEvent(newDomainEvent(JsonObject.class)
					      .withDomainEventId(message.getDomainEventId())
					      .withDomainEventName(message.getDomainEventName())
					      .withCorrelationId(message.getCorrelationId())
					      .withTopicName(message.getTopicName())
					      .withPayload(jsonPayload)
					      .withDateCreated(message.getDateCreated())
					      .build())
			   .withContentType(webhook.getContentType())
			   .withMessage(rewriter.rewritePayload(webhook, requestEntity))
			   .withMessageState(webhookMessage.getMessageState())
			   .withExecutionTime(webhookMessage.getExecutionTime())
			   .withHttpStatus(webhookMessage.getHttpStatus())
			   .build();
	}


	/** {@inheritDoc */
	@Override
	public void resetWebhook(WebhookId hookId, 
							 DomainEventId eventId) {
		Webhook webhook = webhooks.fetchWebhook(hookId);
		Message message = fetchMessage(eventId);
		resetWebhook(webhook,
					 message);
	}

	/** {@inheritDoc */
	@Override
	public void resetWebhook(WebhookName hookName, 
							 DomainEventId eventId) {
		Webhook webhook = webhooks.fetchWebhook(hookName);
		Message message = fetchMessage(eventId);
		resetWebhook(webhook,
					 message);
	}

	private void resetWebhook(Webhook webhook, Message message) {
		
		int calls = repository.execute(resetWebhookCalls(webhook,message));
		
		LOG.info(() -> format("%s: Webhook %s (%s %s) reset to message %s of type %s in topic %s. %d messages will be processed again.",
							  null,
							  webhook.getWebhookName(),
							  webhook.getHttpMethod(),
							  webhook.getEndpoint(),
							  message.getDomainEventId(),
							  message.getDomainEventName(),
							  message.getTopicName(),
							  calls));
		messages.add(createMessage(WHK0004I_WEBHOOK_RESET, 
								   webhook.getWebhookName(),
								   message.getDomainEventId(),
								   message.getTopicName()));		
	}

	private Message fetchMessage(DomainEventId eventId) {
		Message message = repository.execute(findMessageByDomainEventId(eventId));
		if(message == null) {
			LOG.fine(() -> format("%s: Message %s not found", 
								  BUS0002E_MESSAGE_NOT_FOUND.getReasonCode(),
								  eventId));
			throw new EntityNotFoundException(BUS0002E_MESSAGE_NOT_FOUND, 
											  eventId);
		}
		return message;
	}

	/** {@inheritDoc */
	@Override
	public void storeWebhookTemplate(WebhookTemplate template) {
		Webhook webhook = webhooks.fetchWebhook(template.getWebhookId());
		webhook.setContentType(template.getContentType());
		webhook.setTemplate(template.getTemplate());
	}
	
	/** {@inheritDoc */
	@Override
	public void removeWebhookTemplate(WebhookId hookId) {
		Webhook webhook = webhooks.fetchWebhook(hookId);
		removeWebhookTemplate(webhook);
	}

	/** {@inheritDoc */
	@Override
	public void removeWebhookTemplate(WebhookName hookName) {
		Webhook webhook = webhooks.fetchWebhook(hookName);
		removeWebhookTemplate(webhook);
	}
	
	private void removeWebhookTemplate(Webhook webhook) {
		if(isEmptyString(webhook.getTemplate())) {
			return;
		}
		webhook.setContentType("application/json");
		webhook.setTemplate(null);
		LOG.info(()->format("%s: Removed template for webhook %s (%s)",
						    WHK0001I_WEBHOOK_STORED,
						    webhook.getWebhookName(),
						    webhook.getWebhookId()));
		messages.add(createMessage(WHK0001I_WEBHOOK_STORED,
								   webhook.getWebhookId(),
								   webhook.getWebhookName()));
	}
	
	/** {@inheritDoc */
	@Override
	public void enableWebhook(WebhookId hookId) {
		Webhook webhook = webhooks.fetchWebhook(hookId);
		enableWebhook(webhook);
	}

	/** {@inheritDoc */
	@Override
	public void enableWebhook(WebhookName hookName) {
		Webhook webhook = webhooks.fetchWebhook(hookName);
		enableWebhook(webhook);
	}
	
	private void enableWebhook(Webhook webhook) {
		webhook.enable();
		LOG.info(() -> format("%s: Webhook %s (%s) enabled.",
							  WHK0008I_WEBHOOK_ENABLED.getReasonCode(),
							  webhook.getWebhookName(),
							  webhook.getWebhookId()));
		messages.add(createMessage(WHK0008I_WEBHOOK_ENABLED, 
								   webhook.getWebhookId(),
								   webhook.getWebhookName()));
	}
	

	/** {@inheritDoc */
	@Override
	public void disableWebhook(WebhookId hookId) {
		Webhook webhook = webhooks.fetchWebhook(hookId);
		disableWebhook(webhook);
	}

	/** {@inheritDoc */
	@Override
	public void disableWebhook(WebhookName hookName) {
		Webhook webhook = webhooks.fetchWebhook(hookName);
		disableWebhook(webhook);
	}

	private void disableWebhook(Webhook webhook) {
		webhook.disable();
		LOG.info(() -> format("%s: Webhook %s (%s) disabled.",
							  WHK0007I_WEBHOOK_DISABLED.getReasonCode(),
							  webhook.getWebhookName(),
							  webhook.getWebhookId()));
		messages.add(createMessage(WHK0007I_WEBHOOK_DISABLED, 
								   webhook.getWebhookId(),
								   webhook.getWebhookName()));
	}

	/** {@inheritDoc */
	@Override
	public WebhookTemplate getWebhookTemplate(WebhookId hookId) {
		Webhook webhook = webhooks.fetchWebhook(hookId);
		return webhookTemplate(webhook);
	}

	/** {@inheritDoc */
	@Override
	public WebhookTemplate getWebhookTemplate(WebhookName hookName) {
		Webhook webhook = webhooks.fetchWebhook(hookName);
		return webhookTemplate(webhook);
	}
	
	private WebhookTemplate webhookTemplate(Webhook webhook) {
		return newWebhookTemplate()
			   .withWebhookId(webhook.getWebhookId())
			   .withWebhookName(webhook.getWebhookName())
			   .withDescription(webhook.getDescription())
			   .withTopicName(webhook.getTopicName())
			   .withSelector(webhook.getSelector())
			   .withContentType(webhook.getContentType())
			   .withTemplate(webhook.getTemplate())
			   .build();
	}

	@Override
	public WebhookStatistics getWebhookStatistics(WebhookId hookId) {
		Webhook webhook = webhooks.fetchWebhook(hookId);
		return statistics.getWebhookStatistics(webhook);
	}

	@Override
	public WebhookStatistics getWebhookStatistics(WebhookName hookName) {
		Webhook webhook = webhooks.fetchWebhook(hookName);
		return statistics.getWebhookStatistics(webhook);
	}

	@Override
	public void retryWebhook(WebhookId hookId) {
		Webhook webhook = webhooks.fetchWebhook(hookId);
		int calls = repository.execute(retryFailedCalls(webhook));
		LOG.info(() -> format("%d failed calls reset for webhook %s",
							  calls,
							  webhook.getWebhookName()));
	}

	@Override
	public void retryWebhook(WebhookName hookName) {
		Webhook webhook = webhooks.fetchWebhook(hookName);
		int calls = repository.execute(retryFailedCalls(webhook));
		LOG.info(() -> format("%d failed calls reset for webhook %s",
							  calls,
							  webhook.getWebhookName()));		
	}

	@Override
	public WebhookMessages findMessages(WebhookId hookId, 
									    MessageFilter filter) {
		Webhook webhook = webhooks.fetchWebhook(hookId);
		return findMessages(webhook,
							filter);
	}

	@Override
	public WebhookMessages findMessages(WebhookName hookName, 
										MessageFilter filter) {
		Webhook webhook = webhooks.fetchWebhook(hookName);
		return findMessages(webhook,
							filter);
	}

	
	private WebhookMessages findMessages(Webhook webhook,
										 MessageFilter filter) {
		List<MessageReference> messages = repository.execute(findWebhookMessages(webhook, filter))
													.stream()
													.map(message -> newMessageReference()
																	.withTopicName(message.getTopicName())
																	.withDomainEventId(message.getDomainEventId())
																	.withDomainEventName(message.getDomainEventName())
																	.withCorrelationId(message.getCorrelationId())
																	.withMessageState(message.getMessageState())
																	.withHttpStatus(message.getHttpStatus())
																	.withExecutionTime(message.getExecutionTime())
																	.withDateModified(message.getDateModified())
																	.build())
													.collect(toList());
		return  newWebhookMessages()
				.withWebhookId(webhook.getWebhookId())
				.withWebhookName(webhook.getWebhookName())
				.withTopicName(webhook.getTopicName())
				.withDescription(webhook.getDescription())
				.withMessages(messages)
				.build();
		}




}
