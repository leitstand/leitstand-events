/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.webhook.jsonb;

import javax.json.bind.adapter.JsonbAdapter;

import io.leitstand.event.webhook.service.Endpoint;

public class EndpointAdapter implements JsonbAdapter<Endpoint, String>{

	@Override
	public String adaptToJson(Endpoint obj) {
		return Endpoint.toString(obj);
	}

	@Override
	public Endpoint adaptFromJson(String obj) {
		return Endpoint.valueOf(obj);
	}

}
