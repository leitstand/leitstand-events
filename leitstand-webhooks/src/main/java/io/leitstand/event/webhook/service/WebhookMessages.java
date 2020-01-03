/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.webhook.service;

import static io.leitstand.commons.model.BuilderUtil.assertNotInvalidated;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

public class WebhookMessages extends WebhookReference{

	public static Builder newWebhookMessages() {
		return new Builder();
	}
	
	public static class Builder extends WebhookRefBuilder<WebhookMessages, Builder>{
		
		protected Builder() {
			super(new WebhookMessages());
		}
		
		public Builder withMessages(MessageReference.Builder... messages) {
			return withMessages(stream(messages)
								.map(MessageReference.Builder::build)
								.collect(toList()));
		}
		
		public Builder withMessages(List<MessageReference> messages) {
			assertNotInvalidated(getClass(), messages);
			object.messages = new ArrayList<>(messages);
			return this;
		}
	}
	
	private List<MessageReference> messages = emptyList();
	
	public List<MessageReference> getMessages() {
		return unmodifiableList(messages);
	}
}
