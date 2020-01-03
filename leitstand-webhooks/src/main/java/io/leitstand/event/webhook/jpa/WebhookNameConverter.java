/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.webhook.jpa;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import io.leitstand.event.webhook.service.WebhookName;

@Converter
public class WebhookNameConverter implements AttributeConverter<WebhookName, String> {

	@Override
	public String convertToDatabaseColumn(WebhookName attribute) {
		return WebhookName.toString(attribute);
	}

	@Override
	public WebhookName convertToEntityAttribute(String dbData) {
		return WebhookName.valueOf(dbData);
	}

}
