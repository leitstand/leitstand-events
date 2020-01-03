/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.webhook.jsonb;

import javax.json.bind.adapter.JsonbAdapter;

import io.leitstand.event.webhook.service.WebhookId;

public class WebhookIdAdapter implements JsonbAdapter<WebhookId, String>{

	@Override
	public String adaptToJson(WebhookId obj) {
		return WebhookId.toString(obj);
	}

	@Override
	public WebhookId adaptFromJson(String obj) {
		return WebhookId.valueOf(obj);
	}

}
