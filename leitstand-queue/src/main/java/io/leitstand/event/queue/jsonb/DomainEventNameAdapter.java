/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.queue.jsonb;

import javax.json.bind.adapter.JsonbAdapter;

import io.leitstand.event.queue.service.DomainEventName;

public class DomainEventNameAdapter implements JsonbAdapter<DomainEventName, String>{

	@Override
	public String adaptToJson(DomainEventName obj) {
		return DomainEventName.toString(obj);
	}

	@Override
	public DomainEventName adaptFromJson(String obj) {
		return DomainEventName.valueOf(obj);
	}

}
