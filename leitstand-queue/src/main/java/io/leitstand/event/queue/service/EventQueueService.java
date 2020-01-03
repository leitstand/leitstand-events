/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
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
