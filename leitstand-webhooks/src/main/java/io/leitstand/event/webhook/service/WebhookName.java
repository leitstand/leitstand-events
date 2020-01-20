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

import javax.json.bind.annotation.JsonbTypeAdapter;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import io.leitstand.commons.model.Scalar;
import io.leitstand.event.webhook.jsonb.WebhookNameAdapter;

/**
 * A unique webhook name.
 */
@JsonbTypeAdapter(WebhookNameAdapter.class)
public class WebhookName extends Scalar<String> {
	
	private static final long serialVersionUID = 1L;
	
	
	/**
	 * Alias of {@link #valueOf(String)} to improve readability.
	 * <p>
	 * Creates a <ccode>WebhookName</code> from the specified string.
	 * @param name the webhook name
	 * @return the webhook name
	 */
	public static WebhookName webhookName(String name) {
		return valueOf(name);
	}

	/**
	 * Creates a <ccode>WebhookName</code> from the specified string.
	 * @param name the webhook name
	 * @return the webhook name
	 */
	public static WebhookName valueOf(String name) {
		return fromString(name,WebhookName::new);
	}
	
	@NotNull(message="{hook_name.required}")
	@Pattern(regexp="[A-Za-z0-9_]{1,64}", message="{hook_name.invalid}")
	private String value;
	
	/**
	 * Creates a <code>WebhookName</code>.
	 * @param name the webhook name
	 */
	public WebhookName(String name) {
		this.value = name;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getValue() {
		return value;
	}


	
}
