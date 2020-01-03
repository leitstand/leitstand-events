/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.webhook.service;

import static io.leitstand.commons.model.BuilderUtil.assertNotInvalidated;
import static java.lang.Math.max;

import io.leitstand.commons.model.ValueObject;

public class MessageFilter extends ValueObject {

	public static Builder newMessageFilter() {
		return new Builder();
	}
	
	public static class Builder {
		
		private MessageFilter filter = new MessageFilter();
		
		public Builder withMessageState(MessageState state) {
			assertNotInvalidated(getClass(), filter);
			filter.messageState = state;
			return this;
		}

		public Builder withCorrelationId(String correlationId) {
			assertNotInvalidated(getClass(), filter);
			filter.correlationId = correlationId;
			return this;
		}
		
		public Builder withOffset(int offset) {
			assertNotInvalidated(getClass(), filter);
			filter.offset = max(0, offset); // Ignore negative offsets.
			return this;
		}
		
		public Builder withLimit(int limit) {
			assertNotInvalidated(getClass(), filter);
			if(limit <= 0) {
				filter.limit = 100;
			} else {
				filter.limit = limit;
			}
			return this;
		}
		
		public MessageFilter build() {
			assertNotInvalidated(getClass(), filter);
			try {
				return filter;
			} finally {
				this.filter = null;
			}
			
		}
		
	}
	
	
	private MessageState messageState;
	private String correlationId;
	private int limit = 100;
	private int offset = 0;
	
	public MessageState getMessageState() {
		return messageState;
	}
	
	public String getCorrelationId() {
		return correlationId;
	}
	
	public int getLimit() {
		return limit;
	}
	
	public int getOffset() {
		return offset;
	}
	
}
