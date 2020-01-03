/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.queue.model;

import static io.leitstand.event.queue.service.DomainEvent.newDomainEvent;
import static io.leitstand.event.queue.service.DomainEventId.randomDomainEventId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.leitstand.commons.model.Repository;
import io.leitstand.event.queue.service.DomainEvent;
import io.leitstand.event.queue.service.DomainEventId;
import io.leitstand.event.queue.service.DomainEventName;
import io.leitstand.event.queue.service.EventQueueService;
import io.leitstand.event.queue.service.TopicName;

public class EventQueueServiceIT extends EventsIT{

	private EventQueueService service;
	private EventQueueSubtransactionService tx = null;	
	
	@Before
	public void initServiceUnderTest() {
		Repository repo = new Repository(getEntityManager());
		repo.add(new Topic(TopicName.valueOf("JUNIT")));
		tx = mock(EventQueueSubtransactionService.class);
		TopicProvider topics = new TopicProvider(repo, tx);
		service = new DefaultEventQueueService(repo, 
											   topics);
	}
	
	@Test
	public void read_write_domain_event_envelope() {
		DomainEventId eventId = randomDomainEventId();
		DomainEventName eventName = DomainEventName.valueOf("IT");
		
		DomainEvent<String> event = newDomainEvent(String.class)
								  	.withTopicName(TopicName.valueOf("JUNIT"))
								  	.withDomainEventId(eventId)
								  	.withDomainEventName(eventName)
								  	.build();
		transaction(()-> {
			service.send(event);
		});
		transaction(()->{
			DomainEvent<String> restored = service.getEvent(String.class,eventId);
			assertNotSame(event, restored);
			assertEquals(event.getDomainEventId(), restored.getDomainEventId());
			assertEquals(event.getDomainEventName(), restored.getDomainEventName());
		});
		
	}
	
	
	@After
	public void verify() {
		// Do not create new topic, if topic is already present.
		verifyZeroInteractions(tx);
	}
	
}
