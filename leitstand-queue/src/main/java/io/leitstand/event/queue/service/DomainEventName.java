/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.queue.service;

import javax.json.bind.annotation.JsonbTypeAdapter;

import io.leitstand.commons.model.Scalar;
import io.leitstand.event.queue.jsonb.DomainEventNameAdapter;

@JsonbTypeAdapter(DomainEventNameAdapter.class)
public class DomainEventName extends Scalar<String>{

	private static final long serialVersionUID = 1L;

	public static DomainEventName domainEventName(String name) {
		return valueOf(name);
	}
	
	public static DomainEventName valueOf(String name) {
		return fromString(name,DomainEventName::new);
	}
	
	private String value;
	
	public DomainEventName(String name) {
		this.value = name;
	}
	
	public String getValue() {
		return value;
	}

	public boolean matches(String selector) {
		return value.matches(selector);
	}


	
}
