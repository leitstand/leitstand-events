/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.webhook.service;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.ResourceBundle;

import io.leitstand.commons.Reason;

/**
 * Enumeration of webhook API reason codes.
 */
public enum ReasonCode implements Reason{

	/** Webhook stored sucessfully.*/
	WHK0001I_WEBHOOK_STORED,
	
	/** Webhook does not exist.*/
	WHK0002E_WEBHOOK_NOT_FOUND,
	
	/** Webhook successfully removed.*/
	WHK0003I_WEBHOOK_REMOVED, 
	
	/** Webhook successfully reset.*/
	WHK0004I_WEBHOOK_RESET, 
	
	/**
	 * Failed to reset a webhook.
	 * The webhookd does not subscribe the topic of the specified event. 
	 */
	WHK0005E_WEBHOOK_RESET_WRONG_TOPIC,
	
	/** 
	 * Failed to reset a webhook.
	 * The webhook does not subscribe the specified event.
	 */
	WHK0006E_WEBHOOK_RESET_WRONG_EVENT,

	/** Webhook disabled.*/
	WHK0007I_WEBHOOK_DISABLED,
	
	/** Webhook enabled.*/
	WHK0008I_WEBHOOK_ENABLED,
	
	/**
	 * Basic authentication configuration failed because password and confirmed password mismatch.
	 */
	WHK0010E_WEBHOOK_BASIC_AUTH_PASSWORD_MISMATCH,
	
	/**
	 * The webhook invocation failed because the HTTP endpoint returns an HTTP error code.
	 */
	WHK0020E_WEBHOOK_INVOCATION_FAILED,
	
	/** The webhook event loop has been started.*/
	WHK0100I_WEBHOOK_EVENT_LOOP_STARTED,
	
	/** The webhook event loop has been stopped.*/
	WHK0101I_WEBHOOK_EVENT_LOOP_STOPPED;
	
	private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("WebhookMessages");
	
	/**
	 * {@inheritDoc}
	 */
	public String getMessage(Object... args){
		try{
			String pattern = MESSAGES.getString(name());
			return MessageFormat.format(pattern, args);
		} catch(Exception e){
			return name() + Arrays.asList(args);
		}
	}
	
}
