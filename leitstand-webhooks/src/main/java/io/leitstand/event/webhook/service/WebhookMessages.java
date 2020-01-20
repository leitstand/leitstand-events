/*
 * Copyright 2020 RtBrick Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
