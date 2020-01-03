/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
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
