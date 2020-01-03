/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
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
