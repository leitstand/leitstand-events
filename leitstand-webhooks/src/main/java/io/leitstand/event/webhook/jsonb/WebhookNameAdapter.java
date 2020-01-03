/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.webhook.jsonb;

import javax.json.bind.adapter.JsonbAdapter;

import io.leitstand.event.webhook.service.WebhookName;

public class WebhookNameAdapter implements JsonbAdapter<WebhookName, String>{

	@Override
	public String adaptToJson(WebhookName obj) {
		return WebhookName.toString(obj);
	}

	@Override
	public WebhookName adaptFromJson(String obj) {
		return WebhookName.valueOf(obj);
	}

}
