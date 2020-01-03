/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.webhook.service;

import static io.leitstand.commons.model.Patterns.HTTP_URL_PATTERN;

import java.net.URI;

import javax.json.bind.annotation.JsonbTypeAdapter;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import io.leitstand.commons.model.Scalar;
import io.leitstand.event.webhook.jsonb.EndpointAdapter;

/**
 * The HTTP endpoint a webhook shall invoke.
 */
@JsonbTypeAdapter(EndpointAdapter.class)
public class Endpoint extends Scalar<String> {

	private static final long serialVersionUID = 1L;

	/**
	 * Alias of {@link #valueOf(String)} to improve readability.
	 * <p>
	 * Creates an endpoint from a URL.
	 * @param url the endpoint URL as string
	 * @return the HTTP endpoint
	 */
	public static Endpoint endpoint(String url) {
		return valueOf(url);
	}
	/**
	 * Creates the endpoint from an URI
	 * @param uri the endpoint URI
	 * @return the HTTP endpoint
	 */
	public static Endpoint valueOf(URI uri) {
		if(uri == null) {
			return null;
		}
		return valueOf(uri.toString());
	}
	
	/**
	 * Creates an endpoint from a URL.
	 * @param url the endpoint URL as string
	 * @return the HTTP endpoint
	 */
	public static Endpoint valueOf(String url) {
		return fromString(url,Endpoint::new);
	}
	
	@NotNull(message="{endpoint.required}")
	@Pattern(regexp=HTTP_URL_PATTERN, message="{endpoint.invalid}")
	private String value;
	
	/**
	 * Creates an <code>Endpoint</code>.
	 * @param url the endpoint URL
	 */
	public Endpoint(String url) {
		this.value = url;
	}
	
	public URI toUri() {
		return URI.create(value);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getValue() {
		return value;
	}


	
}
