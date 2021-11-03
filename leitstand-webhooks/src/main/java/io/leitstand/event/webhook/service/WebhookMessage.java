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
		public Builder withMessage(JsonObject message) {
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
	
	private JsonObject message;
	private DomainEvent<JsonObject> event;
	private MessageState messageState;
	private Integer httpStatus;
	private Long executionTime;
	
	/**
	 * Returns the request entity.
	 * @return the request entity.
	 */
	public JsonObject getMessage() {
		return message;
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


