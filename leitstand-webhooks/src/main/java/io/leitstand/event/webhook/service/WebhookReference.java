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
import static io.leitstand.event.webhook.service.WebhookId.randomWebhookId;

import javax.json.bind.annotation.JsonbProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.leitstand.commons.model.ValueObject;
import io.leitstand.event.queue.service.TopicName;

/**
 * A immutable reference to an existing webhook.
 * <p>
 * A webhook reference conveys the following information:
 * <ul>
 * 	<li>The unique {@link WebhookId}.</li>
 * 	<li>The unique {@link WebhookName}.</li>
 *  <li>The name of the subscribed topic.</li>
 *  <li>The domain event selector expression. The selector is a regular expression to filter for certain events in a topic.</li>
 *  <li>The webhook description.</li>
 *  <li>The webhook state, i.e. whether the webhook is enabled or disabled.</li>
 * </ul>
 */
public class WebhookReference extends ValueObject{
	
	/**
	 * Creates a <code>WebhookReference</code> builder.
	 * @return a builder to create a new immutable webhook reference.
	 */
	public static Builder newWebhookRef() {
		return new Builder();
	}

	/**
	 * Base class for all composite webhook value objects.
	 * @param <T> the value object type
	 * @param <B> the value object builder
	 */
	protected static class WebhookRefBuilder<T extends WebhookReference, B extends WebhookRefBuilder<T,B>> {
			
		protected T object;
		
		protected WebhookRefBuilder(T object) {
			this.object = object;
		}
		
		/**
		 * Sets the webhook ID
		 * @param hookId the webhook ID
		 * @return a reference to this builder to continue with object creation
		 */
		public B withWebhookId(WebhookId hookId) {
			assertNotInvalidated(getClass(),object);
			((WebhookReference)object).webhookId = hookId;
			return (B) this;
		}
	
		/**
		 * Sets the webhook name
		 * @param hookId the webhook name
		 * @return a reference to this builder to continue with object creation
		 */
		public B withWebhookName(WebhookName hookNa){
			assertNotInvalidated(getClass(), object);
			((WebhookReference)object).webhookName = hookNa;
			return (B) this;
		}
		
		/**
		 * Sets the topic this webhook has subscribed.
		 * @param topic the topic name 
		 * @return a reference to this builder to continue with object creation
		 */
		public B withTopicName(TopicName topic) {
			assertNotInvalidated(getClass(), object);
			((WebhookReference)object).topicName = topic;
			return (B) this;
		}
		
		
		/**
		 * Sets the event selector expression to filter for certain domain events.
		 * @param selector the regular expression to filter for certain domain events.
		 * @return a reference to this builder to continue with object creation
		 */
		public B withSelector(String selector) {
			assertNotInvalidated(getClass(), object);
			((WebhookReference)object).selector = selector;
			return (B) this;
		}
		
		/**
		 * Sets the webhook description
		 * @param description the description
		 * @return a reference to this builder to continue with object creation
		 */
		public B withDescription(String description){
			assertNotInvalidated(getClass(), object);
			((WebhookReference)object).description = description;
			return (B) this;
		}
		
		/**
		 * Sets whether the webhook is enabled (<code>true</code> or not (<code>false</code>).
		 * @param enabled whether the webhook is enabled
		 * @return a reference to this builder to continue with object creation
		 */
		public B withEnabled(boolean enabled) {
			assertNotInvalidated(getClass(), object);
			((WebhookReference)object).enabled = enabled;
			return (B) this;
		}
		

		/**
		 * Creates an immutable webhook reference an invalidates this builder.
		 * Subsequence builder invocation will raise an exception.
		 * @return the immuable webhook refrence.
		 */
		public T build() {
			try {
				assertNotInvalidated(getClass(), object);
				return object;
			} finally {
				this.object = null;
			}
		}
	}
	
	/**
	 * Builder to create immutable webhook reference.
	 */
	public static class Builder extends WebhookRefBuilder<WebhookReference,Builder>{
		public Builder() {
			super(new WebhookReference());
		}
	}
	
	@Valid
	@NotNull(message="{hook_id.required}")
	@JsonbProperty("hook_id")
	private WebhookId webhookId = randomWebhookId();
	
	@Valid
	@NotNull(message="{hook_name.required}")
	@JsonbProperty("hook_name")
	private WebhookName webhookName;
	
	@NotNull(message="{topic_name.required}")
	@Valid
	private TopicName topicName;
	private String selector;
	
	private String description;
	
	private boolean enabled = true;
	
	/**
	 * Returns the webhook ID.
	 * @return the webhook ID.
	 */
	public WebhookId getWebhookId() {
		return webhookId;
	}
	
	/**
	 * Returns the webhook name.
	 * @return the webhook name.
	 */
	public WebhookName getWebhookName() {
		return webhookName;
	}
	
	/**
	 * Returns the webhook description.
	 * Returns <code>null</code> if no descrption exist.
	 * @return
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Returns the name of the subscribed topic.
	 * @return the topic name.
	 */
	public TopicName getTopicName() {
		return topicName;
	}
	
	/**
	 * Returns the selector expression to filter for domain events in the subscribed topic.
	 * Returns <code>null</code> if no selector is present and the webhook subscribes all events.
	 * @return the selector expression or <code>null</code> if no expression is present.
	 */
	public String getSelector() {
		return selector;
	}
	
	/**
	 * Returns whether this webhook is enabled or not. A disabled webhook does not consume domain events.
	 * @return <code>true</code> if this webhook is enabled, <code>false</code> otherwse.
	 */
	public boolean isEnabled() {
		return enabled;
	}

}
