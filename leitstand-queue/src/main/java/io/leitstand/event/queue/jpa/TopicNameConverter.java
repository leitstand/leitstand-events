/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.queue.jpa;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import io.leitstand.event.queue.service.TopicName;

@Converter
public class TopicNameConverter implements AttributeConverter<TopicName,String> {

	@Override
	public String convertToDatabaseColumn(TopicName attribute) {
		return TopicName.toString(attribute);
	}

	@Override
	public TopicName convertToEntityAttribute(String dbData) {
		return TopicName.valueOf(dbData);
	}

}
