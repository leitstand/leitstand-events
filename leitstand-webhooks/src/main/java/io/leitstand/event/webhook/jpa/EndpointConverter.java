/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.webhook.jpa;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import io.leitstand.event.webhook.service.Endpoint;

@Converter
public class EndpointConverter implements AttributeConverter<Endpoint, String> {

	@Override
	public String convertToDatabaseColumn(Endpoint attribute) {
		return Endpoint.toString(attribute);
	}

	@Override
	public Endpoint convertToEntityAttribute(String dbData) {
		return Endpoint.valueOf(dbData);
	}

}
