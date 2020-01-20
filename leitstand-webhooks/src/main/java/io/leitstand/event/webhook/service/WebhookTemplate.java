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

/**
 * Webhook template settings.
 * <p>
 * A webhook templates enables to rewrite the domain event.
 * Apart from webhook reference settings to identify the webhook, 
 * the template conveys the template definition to rewrite a domain event message
 * and the content type of the rewritten message.
 */
public class WebhookTemplate extends WebhookReference{
	
	/**
	 * Creates a webhook template descriptor.
	 * @return a builder to create an immutable webhook template descriptor
	 */
	public static Builder newWebhookTemplate() {
		return new Builder();
	}
	
	/**
	 * Webhook template descriptor builder.
	 */
	public static class Builder extends WebhookRefBuilder<WebhookTemplate, Builder> {
		
		protected Builder() {
			super(new WebhookTemplate());
		}
		
		/**
		 * Sets the content type of the rewritten template.
		 * @param contentType content type of rewritten template
		 * @return a reference to this builder to continue with object creation
		 */
		public Builder withContentType(String contentType){
			assertNotInvalidated(getClass(), object);
			object.contentType = contentType;
			return this;
		}

		/**
		 * Sets the template to rewrite the domain event message.
		 * @param template the template to rewrite the domain event message
		 * @return a reference to this builder to continue with object creation
		 */
		public Builder withTemplate(String template){
			assertNotInvalidated(getClass(), object);
			object.template = template;
			return this;
		}

	}

	private String contentType = "application/json";
	private String template;
	
	/**
	 * Returns the content type of the rewritten template.
	 * @return the content type of the rewritten template.
	 */
	public String getContentType() {
		return contentType;
	}
	
	/**
	 * Returns the template to rewrite the domain event message.
	 * Returns <code>null</code> if no template to rewrite the domain event message exists.
	 * @return the template definition or <code>null</code> if no template exists.
	 */
	public String getTemplate() {
		return template;
	}
	
}
