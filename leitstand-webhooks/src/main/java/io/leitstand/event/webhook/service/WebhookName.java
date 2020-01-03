/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
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
