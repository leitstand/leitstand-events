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

import static io.leitstand.commons.model.Patterns.UUID_PATTERN;
import static java.util.UUID.randomUUID;

import javax.json.bind.annotation.JsonbTypeAdapter;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import io.leitstand.commons.model.Scalar;
import io.leitstand.event.webhook.jsonb.WebhookIdAdapter;

/**
 * Unique immutable webhook ID in UUIDv4 format.
 * The <code>WebhookId</code> is assigned whenever a new webhook is created.
 */
@JsonbTypeAdapter(WebhookIdAdapter.class)
public class WebhookId extends Scalar<String> {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a random webhook ID.
	 * @return a random webhook ID.
	 */
	public static WebhookId randomWebhookId() {
		return new WebhookId(randomUUID().toString());
	}
	
	/**
     * Creates a <code>WebhookId</code> from the specified string.
     * @param id the webhook ID
     * @return the <code>WebhookId</code> or <code>null</code> if the specified string is <code>null</code> or empty.
	 */
	public static WebhookId valueOf(String id) {
		return fromString(id,WebhookId::new);
	}

	@NotNull(message="{hook_id.required}")
	@Pattern(regexp=UUID_PATTERN, message="{hook_id.invalid}")
	private String value;
	
	/**
	 * Creates a <ccode>WebhookId</code>.
	 * @param id the webhook ID value
	 */
	public WebhookId(String id) {
		this.value = id;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getValue() {
		return value;
	}
	
}
