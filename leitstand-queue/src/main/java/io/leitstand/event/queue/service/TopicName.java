/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.queue.service;

import javax.json.bind.annotation.JsonbTypeAdapter;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import io.leitstand.commons.model.Scalar;
import io.leitstand.event.queue.jsonb.TopicNameAdapter;

@JsonbTypeAdapter(TopicNameAdapter.class)
public class TopicName extends Scalar<String>{

	private static final long serialVersionUID = 1L;

	public static TopicName topicName(String name) {
		return valueOf(name);
	}

	public static TopicName valueOf(String name) {
		return fromString(name,TopicName::new);
	}
	
	@NotNull(message="{topic_name.required}")
	@Pattern(regexp="[A-Za-z0-9_]{1,16}")
	private String value;
	
	public TopicName(String name) {
		this.value = name;
	}
	
	@Override
	public String getValue() {
		return value;
	}


	
}
