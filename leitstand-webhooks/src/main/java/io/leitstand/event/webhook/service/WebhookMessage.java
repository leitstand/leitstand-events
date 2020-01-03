/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.webhook.service;

import static io.leitstand.commons.model.BuilderUtil.assertNotInvalidated;

import javax.json.JsonObject;

import io.leitstand.event.queue.service.DomainEvent;

/**
 * Description of a message send by a webhook.
 */
public class WebhookMessage extends WebhookReference{

	/**
	 * Creates a <code>WebhookMessage</code>.
	 * @return a builder to create a new webhook message descriptor.
	 */
	public static Builder newWebhookMessage() {
		return new Builder();
	}
	
	/**
	 * Builder for immutable <code>WebhookMessage</code> descriptors.
	 */
	public static class Builder extends WebhookRefBuilder<WebhookMessage, Builder>{
		
		protected Builder() {
			super(new WebhookMessage());
		}
		
		/**
		 * Sets the message content type.
		 * @param contentType content type in MIME format (e.g. <code>application/json</code>)
		 * @return a reference to this builder to continue with object creation
		 */
		public Builder withContentType(String contentType) {
			assertNotInvalidated(getClass(), object);
			object.contentType = contentType;
			return this;
		}
		
		/**
		 * Sets the domain event.
		 * @param event the original domain event
		 * @return a reference to this builder to continue with object creation
		 */
		public Builder withEvent(DomainEvent<JsonObject> event) {
			assertNotInvalidated(getClass(),object);
			object.event = event;
			return this;
		}
		
		/**
		 * Set the message sent by the webhook.
		 * @param message the message text
		 * @return a reference to this builder to continue with object creation
		 */
		public Builder withMessage(String message) {
			assertNotInvalidated(getClass(), object);
			object.message = message;
			return this;
		}
		
		public Builder withMessageState(MessageState state) {
			assertNotInvalidated(getClass(),object);
			object.messageState = state;
			return this;
		}
		
		public Builder withHttpStatus(Integer httpStatus) {
			assertNotInvalidated(getClass(), object);
			object.httpStatus = httpStatus;
			return this;
		}

		public Builder withExecutionTime(Long executionTimeMillis) {
			assertNotInvalidated(getClass(), object);
			object.executionTime = executionTimeMillis;
			return this;
		}

		
		
	}
	
	private String message;
	private DomainEvent<JsonObject> event;
	private String contentType;
	private MessageState messageState;
	private Integer httpStatus;
	private Long executionTime;
	
	/**
	 * Returns the message text.
	 * @return the message text.
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * Returns the message content type in MIME format.
	 * @return the message content type in MIME format.
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Returns the original domain event.
	 * @return the original domain event.
	 */
	public DomainEvent<JsonObject> getEvent() {
		return event;
	}
	
	public MessageState getMessageState() {
		return messageState;
	}
	
	public Integer getHttpStatus() {
		return httpStatus;
	}
	
	public Long getExecTime() {
		return executionTime;
	}
	
}


