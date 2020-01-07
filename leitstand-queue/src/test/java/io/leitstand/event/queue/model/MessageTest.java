/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.queue.model;

import static io.leitstand.event.queue.service.DomainEvent.newDomainEvent;
import static io.leitstand.event.queue.service.DomainEventId.randomDomainEventId;
import static io.leitstand.event.queue.service.DomainEventName.domainEventName;
import static io.leitstand.event.queue.service.TopicName.topicName;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import io.leitstand.event.queue.service.DomainEvent;

public class MessageTest {

	
	@Test
	public void determine_payload_type_from_payload() {
		DomainEvent event = newDomainEvent()
							.withTopicName(topicName("unittest"))
							.withDomainEventId(randomDomainEventId())
							.withDomainEventName(domainEventName("foo"))
							.withPayload("bar")
							.build();
		Message message = new Message(mock(Topic.class),event);
		assertEquals(String.class.getName(),message.getJavaType());
		
	}
	
	@Test
	public void payload_type_is_null_when_payload_is_null() {
		DomainEvent event = newDomainEvent()
							.withTopicName(topicName("unittest"))
							.withDomainEventId(randomDomainEventId())
							.withDomainEventName(domainEventName("foo"))
							.build();
		Message message = new Message(mock(Topic.class),event);
		assertNull(message.getJavaType());
		
	}
	
}
