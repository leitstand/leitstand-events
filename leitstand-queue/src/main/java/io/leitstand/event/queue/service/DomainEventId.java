/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.queue.service;

import java.util.UUID;

import javax.json.bind.annotation.JsonbTypeAdapter;

import io.leitstand.commons.model.Scalar;
import io.leitstand.event.queue.jsonb.DomainEventIdAdapter;

@JsonbTypeAdapter(DomainEventIdAdapter.class)
public class DomainEventId extends Scalar<String>{

	private static final long serialVersionUID = 1L;

	public static DomainEventId randomDomainEventId() {
		return new DomainEventId(UUID.randomUUID().toString());
	}
	
	public static DomainEventId domainEventId(String id) {
		return valueOf(id);
	}
	
	public static DomainEventId valueOf(String id) {
		return fromString(id,DomainEventId::new);
	}
	
	private String value;
	
	public DomainEventId(String value) {
		this.value = value;
	}
	
	@Override
	public String getValue() {
		return value;
	}


	
}

