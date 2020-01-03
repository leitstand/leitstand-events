/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.queue.jpa;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import io.leitstand.event.queue.service.DomainEventId;

@Converter
public class DomainEventIdConverter implements AttributeConverter<DomainEventId,String> {

	@Override
	public String convertToDatabaseColumn(DomainEventId attribute) {
		return DomainEventId.toString(attribute);
	}

	@Override
	public DomainEventId convertToEntityAttribute(String dbData) {
		return DomainEventId.valueOf(dbData);
	}

}
