/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.queue.jsonb;

import javax.json.bind.adapter.JsonbAdapter;

import io.leitstand.event.queue.service.DomainEventId;

public class DomainEventIdAdapter implements JsonbAdapter<DomainEventId, String>{

	@Override
	public String adaptToJson(DomainEventId obj) {
		return DomainEventId.toString(obj);
	}

	@Override
	public DomainEventId adaptFromJson(String obj) {
		return DomainEventId.valueOf(obj);
	}

}
