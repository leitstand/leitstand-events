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

import io.leitstand.commons.model.ValueObject;

public class MessageStateStatistics extends ValueObject{

	public static Builder newMessageStatistics() {
		return new Builder();
	}
	
	public static class Builder {
		
		private MessageStateStatistics statistics = new MessageStateStatistics();
		
		public Builder withMessageState(MessageState state) {
			assertNotInvalidated(getClass(), statistics);
			statistics.messageState = state;
			return this;
		}

		public Builder withMessageCount(Integer messageCount) {
			assertNotInvalidated(getClass(), statistics);
			statistics.messageCount = messageCount;
			return this;
		}

		public Builder withMinExecTime(Integer duration) {
			assertNotInvalidated(getClass(), statistics);
			statistics.minExecTime = duration;
			return this;
		}

		public Builder withMaxExecTime(Integer duration) {
			assertNotInvalidated(getClass(), statistics);
			statistics.maxExecTime = duration;
			return this;
		}

		public Builder withAvgExecTime(Number duration) {
			assertNotInvalidated(getClass(), statistics);
			if(duration != null) {
				statistics.avgExecTime = duration.floatValue();
			}
			return this;
			
		}

		public Builder withStddevExecTime(Number duration) {
			assertNotInvalidated(getClass(), statistics);
			if(duration != null) {
				statistics.stddevExecTime = duration.floatValue();
			}
			return this;
		}
		
		
		public MessageStateStatistics build() {
			try {
				assertNotInvalidated(getClass(), statistics);
				return statistics;
			} finally {
				this.statistics = null;
			}
		}

		
	}
	
	
	private MessageState messageState;
	private int messageCount;
	private Integer minExecTime;
	private Float avgExecTime;
	private Integer maxExecTime;
	private Float stddevExecTime;

	public MessageState getMessageState() {
		return messageState;
	}
	
	public int getMessageCount() {
		return messageCount;
	}
	
	public Integer getMaxExecTime() {
		return maxExecTime;
	}
	
	public Integer getMinExecTime() {
		return minExecTime;
	}
	
	public Float getAvgExecTime() {
		return avgExecTime;
	}
	
	public Float getStddevExecTime() {
		return stddevExecTime;
	}
	
}
