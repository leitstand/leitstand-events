/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.webhook.service;

import static io.leitstand.commons.model.BuilderUtil.assertNotInvalidated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * General webhook settings.
 * The webhook settings consists of:
 * <ul>
 * 	<li>The unique {@link WebhookId}.</li>
 * 	<li>The unique {@link WebhookName}.</li>
 *  <li>The name of the subscribed topic.</li>
 *  <li>The domain event selector expression. The selector is a regular expression to filter for certain events in a topic.</li>
 *  <li>The webhook description.</li>
 *  <li>The webhook state, i.e. whether the webhook is enabled or disabled.</li>
 *  <li>The HTTP endpoint to be invoked</li>
 *  <li>The HTTP method to invoke the HTTP endpoint, either <code>PUT</code> or <code>POST</code>.</li>
 *  <li>Credentials to configure HTTP endpoint basic authentication, i.e. user ID, passwor and confirmed password for detect accidential typos.</li>
 *  <li>Access Key for HTTP endpoint bearer token authentication, i.e. an API access key.</li>
 *  <li>The batch size stating how many messages shall be invoked in within a single transaction. 
 *  All messages in the same batch are retried if one message in that batch cannot be processed.</li>
 *</ul>
 * All sensitive data is stored AES encrypted and protected with the Leitstand master secret.
 */
public class WebhookSettings extends WebhookReference{
	
	/**
	 * Creates a <code>WebhookSettings</code> object.
	 * @return a builder to create a webhook settings object.
	 */
	public static Builder newWebhookSettings() {
		return new Builder();
	}
	
	/**
	 * Builder for an immutable webhook settings value object.
	 */
	public static class Builder extends WebhookRefBuilder<WebhookSettings, Builder> {
		
		protected Builder() {
			super(new WebhookSettings());
		}
		
		/**
		 * Sets the HTTP endpoint invoked by this webhook.
		 * @param endpoint the HTTP endpoint
		 * @return a reference to this builder to continue with object creation
		 */
		public Builder withEndpoint(Endpoint endpoint){
			assertNotInvalidated(getClass(), object);
			object.endpoint = endpoint;
			return this;
		}

		/**
		 * Sets the user ID for HTTP Basic Authentication
		 * @param userId the user ID 
		 * @return a reference to this builder to continue with object creation
		 */
		public Builder withUserId(String user){
			assertNotInvalidated(getClass(), object);
			object.userId = user;
			return this;
		}

		/**
		 * Sets the password for HTTP Basic Authentication
		 * @param password password
		 * @return a reference to this builder to continue with object creation
		 */
		public Builder withPassword(String password){
			assertNotInvalidated(getClass(), object);
			object.password = password;
			object.confirmPassword=password;
			return this;
		}

		/**
		 * Sets the access key for HTTP Bearer Token authentication.
		 * @param accesskey the access key
		 * @return a reference to this builder to continue with object creation
		 */
		public Builder withAccesskey(String accesskey){
			assertNotInvalidated(getClass(), object);
			object.accesskey = accesskey;
			return this;
		}
		
		/**
		 * Sets the HTTP method to invoke the HTTP endpoint.
		 * @param method the HTTP method
		 * @return a reference to this builder to continue with object creation
		 */
		public Builder withMethod(HttpMethod method){
			assertNotInvalidated(getClass(), object);
			object.method = method;
			return this;
		}
		
		/**
		 * Sets the configurer batch size.
		 * @param batchSize the batch size
		 * @return a reference to this builder to continue with object creation
		 */
		public Builder withBatchSize(int batchSize) {
			assertNotInvalidated(getClass(), object);
			object.batchSize = batchSize;
			return this;
		}

	}

	/**
	 * Enumeration of supported HTTP methods.
	 */
	public enum HttpMethod {
		/** HTTP PUT */
		PUT,
		/** HTTP POST */
		POST
	}
	

	private String userId;
	private String password;
	private String confirmPassword;
	private String accesskey;
	private int batchSize = 1;
	
	@NotNull(message="{method.required}")
	private HttpMethod method;
	
	@Valid
	@NotNull(message="{endpoint.required}")
	private Endpoint endpoint;
	
	/**
	 * Returns the HTTP endpoint to be invoked.
	 * @return the HTTP endpoint.
	 */
	public Endpoint getEndpoint() {
		return endpoint;
	}
	
	/**
	 * Returns the HTTP endpoint invocation method.
	 * @return the HTTP endpoint invocation method.
	 */
	public HttpMethod getMethod() {
		return method;
	}
	
	/**
	 * Returns the user ID for HTTP endpoint basic authentication.
	 * Returns <code>null</code> if no user ID was set.
	 * @return the user ID for HTTP basic authentication
	 */
	public String getUserId() {
		return userId;
	}
	
	/**
	 * Returns the password for HTTP endpoint basic authentication.
	 * Returns <code>null</code> if no password was set.
	 * @return the password for HTTP basic authentication
	 */
	public String getPassword() {
		return password;
	}
	
	/**
	 * Returns access key for HTTP endpoint bearer token authentication.
	 * Returns <code>null</code> if no access key was set.
	 * @return the access key for HTTP endpoint authentication.
	 */
	public String getAccesskey() {
		return accesskey;
	}

	/**
	 * Returns the confirmed password value. 
	 * Password and confirmed password must be equal to update the password in the database
	 * in order to detect accidential typos.
	 * @return the confirmed password value
	 */
	public String getConfirmPassword() {
		return confirmPassword;
	}
	
	/**
	 * Returns the message batch size.
	 * @return the message batch size.
	 */
	public int getBatchSize() {
		return batchSize;
	}
	
}