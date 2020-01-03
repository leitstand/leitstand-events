/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.webhook.jpa;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import io.leitstand.event.webhook.service.WebhookId;

@Converter
public class WebhookIdConverter implements AttributeConverter<WebhookId, String> {

	@Override
	public String convertToDatabaseColumn(WebhookId attribute) {
		return WebhookId.toString(attribute);
	}

	@Override
	public WebhookId convertToEntityAttribute(String dbData) {
		return WebhookId.valueOf(dbData);
	}

}
