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
package io.leitstand.event.queue.service;

import static io.leitstand.commons.model.BuilderUtil.assertNotInvalidated;
import static io.leitstand.event.queue.service.DomainEventId.randomDomainEventId;
import static java.util.Objects.requireNonNull;

import java.util.Date;

import javax.json.bind.annotation.JsonbProperty;

import io.leitstand.commons.model.ValueObject;

/**
 * A domain event informs about an event, i.e. typically a state change, that occurred in a sub domain.
 * Other sub domains and processes can subscribe for domains to trigger subsequent actions.
 * <p>
 * This class defines the envelope protocol to convey domain events.
 * <ul>
 * 	<li>A unique event ID to identify a domain event unambiguously</li>
 *  <li>An event name to describe what event is reported (e.g. <code>ElementSettingsUpdated</code>) 
 *  <li>An optional correlation ID, such that events originating from the same process can be correlated easily</li>
 *  <li>A topic to group domain events.</li>
 *  <li>The event payload, i.e. the object that explains the state change</li>
 *  <li>The creation date of the event</li>
 * </ul>
 * The {@link EventQueueService} maintains the topics and conveys the domain events to existing subscribers.
 * @param <E>
 */
public class DomainEvent<E> extends ValueObject{

	public static <E> Builder<E> newDomainEvent() {
		return new Builder<>();
	}
	
	public static <E> Builder<E> newDomainEvent(Class<E> payloadType) {
		return DomainEvent.<E>newDomainEvent();
	}
	
	public static class Builder<E> {
		private DomainEvent<E> event = new DomainEvent<>();

		public Builder<E> withDomainEventId(DomainEventId id) {
			assertNotInvalidated(getClass(),event);
			event.domainEventId = id;
			return this;
		}
		
		public Builder<E> withDomainEventName(DomainEventName name) {
			assertNotInvalidated(getClass(),event);
			event.domainEventName = name;
			return this;
		}
		
		public Builder<E> withTopicName(TopicName topic) {
			assertNotInvalidated(getClass(),event);
			event.topicName = topic;
			return this;
		}
		
		public Builder<E> withCorrelationId(String id) {
			assertNotInvalidated(getClass(),event);
			event.correlationId = id;
			return this;
		}
		
		public Builder<E> withDateCreated(Date date) {
			assertNotInvalidated(getClass(),event);
			event.dateCreated = new Date(date.getTime());
			return this;
		}
		
		public Builder<E> withPayload(E payload) {
			assertNotInvalidated(getClass(),event);
			event.payload = payload;
			if(event.domainEventName == null && payload != null) {
				event.domainEventName = DomainEventName.valueOf(payload.getClass().getSimpleName());
			}
			return this;
		}
		
		public DomainEvent<E> build() {
			try {
				assertNotInvalidated(getClass(), event);
				requireNonNull(event.topicName,"A topic name is required");
				if(event.domainEventId == null) {
					event.domainEventId = randomDomainEventId();
				}
				if(event.dateCreated == null) {
					event.dateCreated = new Date();
				}
				return event;
			} finally {
				this.event = null;
			}
		}
	}
	
	@JsonbProperty("event_id")
	private DomainEventId domainEventId;
	
	@JsonbProperty("event_name")
	private DomainEventName domainEventName;
	
	private TopicName topicName;
	private String correlationId;
	private E payload;
	private Date dateCreated;

	
	public DomainEventId getDomainEventId() {
		return domainEventId;
	}
	
	public DomainEventName getDomainEventName() {
		return domainEventName;
	}
	
	public TopicName getTopicName() {
		return topicName;
	}
	
	public String getCorrelationId() {
		return correlationId;
	}
	
	public E getPayload() {
		return payload;
	}
	
	public Date getDateCreated() {
		return new Date(dateCreated.getTime());
	}
	
}
