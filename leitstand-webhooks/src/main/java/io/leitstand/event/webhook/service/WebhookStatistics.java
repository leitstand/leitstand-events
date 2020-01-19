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
import static java.util.Collections.unmodifiableMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class WebhookStatistics extends WebhookReference{

	
	public static Builder newWebhookStatistics() {
		return new Builder();
	}
	
	
	public static class Builder extends WebhookRefBuilder<WebhookStatistics, Builder> {
		
		protected Builder() {
			super(new WebhookStatistics());
		}
		
		public Builder withStatistics(MessageStateStatistics.Builder... stats) {
			return withStatistics(stream(stats)
								  .map(MessageStateStatistics.Builder::build)
								  .collect(toMap(MessageStateStatistics::getMessageState,identity())));
		}
		
		public Builder withStatistics(List<MessageStateStatistics> stats) {
			return withStatistics(stats.stream()
					  			  	   .collect(toMap(MessageStateStatistics::getMessageState,identity())));
		}
		
		public Builder withStatistics(Map<MessageState,MessageStateStatistics> stats) {
			assertNotInvalidated(getClass(), super.object);
			object.statistics = new TreeMap<>(stats);
			return this;
		}
		
		
		
		public WebhookStatistics build() {
			assertNotInvalidated(getClass(), object);
			object.totalMessageCount = object.statistics
											 .values()
											 .stream()
											 .map(MessageStateStatistics::getMessageCount)
											 .reduce((a,b) -> a+b)
											 .orElse(0);
			return super.build();
		}

	
		
	}
	
	
	private Map<MessageState,MessageStateStatistics> statistics = new TreeMap<>();
	private int totalMessageCount;
	
	public Map<MessageState, MessageStateStatistics> getStatistics() {
		return unmodifiableMap(statistics);
	}
	
	public int getMessageCount() {
		return totalMessageCount;
	}
}
