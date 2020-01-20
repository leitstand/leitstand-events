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

import java.util.List;
import java.util.SortedSet;

/**
 * A persistent event queue for domain event delivery.
 * <p> 
 * A domain event informs about a state change in a domain.
 * The payload describes the state change in detail.
 * <p>
 * The message bus guarantees at least once delivery. 
 * @see DomainEvent - the envelope protocol to convey state change information
 */
public interface EventQueueService {

	DomainEventId send(DomainEvent<?> event);

	<E> DomainEvent<E> getEvent(Class<E> payloadType, 
								DomainEventId eventId);


	<E> List<DomainEvent<E>> findEvents(TopicName topic,
										DomainEventName event,
										Class<E> payloadType,
										int offset, 
										int limit);
	
	SortedSet<TopicName> getTopicNames();
}
