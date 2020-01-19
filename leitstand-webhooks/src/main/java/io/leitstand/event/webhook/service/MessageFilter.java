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
