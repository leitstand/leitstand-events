/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.queue.jpa;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import io.leitstand.event.queue.service.DomainEventName;

@Converter
public class DomainEventNameConverter implements AttributeConverter<DomainEventName,String> {

	@Override
	public String convertToDatabaseColumn(DomainEventName attribute) {
		return DomainEventName.toString(attribute);
	}

	@Override
	public DomainEventName convertToEntityAttribute(String dbData) {
		return DomainEventName.valueOf(dbData);
	}

}
