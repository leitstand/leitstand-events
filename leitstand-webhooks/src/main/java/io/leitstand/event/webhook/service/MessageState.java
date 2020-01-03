/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.webhook.service;

import static io.leitstand.commons.model.StringUtil.isEmptyString;

public enum MessageState {
	READY,
	IN_PROGRESS,
	PROCESSED,
	FAILED;
	
	public static MessageState messageState(String name) {
		if(isEmptyString(name)){
			return null;
		}
		return valueOf(name);
	}
	
}
