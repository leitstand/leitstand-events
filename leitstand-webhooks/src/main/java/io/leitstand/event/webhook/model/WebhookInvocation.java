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
package io.leitstand.event.webhook.model;

import io.leitstand.event.queue.service.DomainEventId;
import io.leitstand.event.queue.service.DomainEventName;
import io.leitstand.event.webhook.service.Endpoint;

class WebhookInvocation {
	
	static Builder newWebhookInvocation() {
		return new Builder();
	}
	
	static class Builder {
		private WebhookInvocation invocation = new WebhookInvocation();
		
		public Builder withDomainEventId(DomainEventId id) {
			invocation.eventId = id;
			return this;
		}
		
		public Builder withDomainEventName(DomainEventName name) {
			invocation.eventName = name;
			return this;
		}

		public Builder withMessagePK(Long id) {
			invocation.messagePK = id;
			return this;
		}

		public Builder withContentType(String contentType) {
			invocation.contentType = contentType;
			return this;
		}
		
		public Builder withMessage(String message) {
			invocation.message = message;
			return this;
		}
	
		public Builder withEndpoint(Endpoint endpoint) {
			invocation.endpoint = endpoint;
			return this;
		}
		
		public WebhookInvocation build() {
			try {
				return invocation;
			} finally {
				this.invocation = null;
			}
		}

	}
	
	private Long messagePK;
	private DomainEventId eventId;
	private DomainEventName eventName;
	private Endpoint endpoint;
	private String contentType;
	private String message;
	
	
	public Endpoint getEndpoint() {
		return endpoint;
	}

	public String getContentType() {
		return contentType;
	}
	
	public DomainEventId getEventId() {
		return eventId;
	}
	
	public DomainEventName getEventName() {
		return eventName;
	}
	
	public String getMessage() {
		return message;
	}
	
	public Long getMessagePK() {
		return messagePK;
	}
	
}
