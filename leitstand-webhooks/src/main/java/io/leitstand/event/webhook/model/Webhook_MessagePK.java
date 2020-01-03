/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.webhook.model;

import static io.leitstand.commons.model.ObjectUtil.isDifferent;
import static java.util.Objects.hash;

import java.io.Serializable;

public class Webhook_MessagePK implements Serializable{

	private static final long serialVersionUID = 1L;

	private Long message;
	private Long webhook;
	
	
	public Webhook_MessagePK() {
		// JPA
	}
	
	public Webhook_MessagePK(Long webhookPK, Long messagePK) {
		this.webhook = webhookPK;
		this.message = messagePK;
	}

	public Long getMessage() {
		return message;
	}
	
	public Long getWebhook() {
		return webhook;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}
		if(obj == this) {
			return true;
		}
		if(getClass() != obj.getClass()) {
			return false;
		}
		Webhook_MessagePK pk = (Webhook_MessagePK) obj;
		
		if(isDifferent(message, pk.getMessage())) {
			return false;
		}
		
		if(isDifferent(webhook,pk.getWebhook())) {
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		return hash(webhook,message);
	}
	
}
