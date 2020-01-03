/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.queue.service;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.ResourceBundle;

import io.leitstand.commons.Reason;

public enum ReasonCode implements Reason{

	BUS0001I_DOMAIN_EVENT_SENT,
	BUS0002E_MESSAGE_NOT_FOUND,
	BUS0003E_INCOMPATIBLE_PAYLOAD_TYPE;
	
	private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("QueueMessages");
	
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
