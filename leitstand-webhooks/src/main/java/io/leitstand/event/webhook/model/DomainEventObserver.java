/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.webhook.model;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.leitstand.event.queue.service.DomainEvent;
import io.leitstand.event.queue.service.EventQueueService;

@ApplicationScoped
public class DomainEventObserver {

	@Inject
	private EventQueueService bus;
	
	public void publish(@Observes DomainEvent<?> event) {
		bus.send(event);
	}
	
}
