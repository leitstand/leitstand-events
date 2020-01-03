/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.queue.jsonb;

import javax.json.bind.adapter.JsonbAdapter;

import io.leitstand.event.queue.service.TopicName;

public class TopicNameAdapter implements JsonbAdapter<TopicName, String> {

	@Override
	public String adaptToJson(TopicName obj) {
		return TopicName.toString(obj);
	}

	@Override
	public TopicName adaptFromJson(String obj) {
		return TopicName.valueOf(obj);
	}

}
