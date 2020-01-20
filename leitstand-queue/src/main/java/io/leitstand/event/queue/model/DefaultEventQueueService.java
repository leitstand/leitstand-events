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
package io.leitstand.event.queue.model;

import static io.leitstand.commons.jsonb.JsonProcessor.unmarshal;
import static io.leitstand.commons.model.ObjectUtil.isDifferent;
import static io.leitstand.event.queue.model.Message.findMessageByDomainEventId;
import static io.leitstand.event.queue.model.Message.findMessagesByTopic;
import static io.leitstand.event.queue.model.Topic.findTopicNames;
import static io.leitstand.event.queue.service.DomainEvent.newDomainEvent;
import static io.leitstand.event.queue.service.ReasonCode.BUS0002E_MESSAGE_NOT_FOUND;
import static io.leitstand.event.queue.service.ReasonCode.BUS0003E_INCOMPATIBLE_PAYLOAD_TYPE;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.SortedSet;
import java.util.logging.Logger;

import javax.inject.Inject;

import io.leitstand.commons.EntityNotFoundException;
import io.leitstand.commons.UnprocessableEntityException;
import io.leitstand.commons.model.Repository;
import io.leitstand.commons.model.Service;
import io.leitstand.event.queue.service.DomainEvent;
import io.leitstand.event.queue.service.DomainEventId;
import io.leitstand.event.queue.service.DomainEventName;
import io.leitstand.event.queue.service.EventQueueService;
import io.leitstand.event.queue.service.TopicName;

@Service
public class DefaultEventQueueService implements EventQueueService{

	private static final Logger LOG = Logger.getLogger(DefaultEventQueueService.class.getName());
	
	@Inject
	@EventQueue
	private Repository repository;
	
	@Inject
	private TopicProvider topics;
	
	
	public DefaultEventQueueService() {
		// EJB
	}
	
	DefaultEventQueueService(Repository repository,
							 TopicProvider topics){
		this.repository = repository;
		this.topics = topics;
	}
	
	@Override
	public DomainEventId send(DomainEvent<?> event) {
		Topic topic = topics.getOrCreateTopic(event.getTopicName());
		Message message = new Message(topic,
									  event);
		repository.add(message);
		
		return event.getDomainEventId();
	}

	private Message fetchMessage(DomainEventId eventId) {
		Message message = repository.execute(findMessageByDomainEventId(eventId));
		if(message == null) {
			LOG.fine(() -> format("%s: Message %s not found!", 
								  BUS0002E_MESSAGE_NOT_FOUND.getReasonCode(),
								  eventId));
			throw new EntityNotFoundException(BUS0002E_MESSAGE_NOT_FOUND,
											  eventId);
			
		}
		return message;
	}

	@Override
	public <E> DomainEvent<E> getEvent(Class<E> payloadType, DomainEventId eventId) {
		Message message = fetchMessage(eventId);

		if(message.getJavaType() != null && isDifferent(payloadType.getName(), message.getJavaType())){
			LOG.fine(() -> format("%s: Invalid type. %s is not compatible with %s. Cannot unmarshal %s message %s in topic %s",
								  BUS0003E_INCOMPATIBLE_PAYLOAD_TYPE.getReasonCode(),
								  payloadType.getName(),
								  message.getJavaType(),
								  message.getDomainEventName(),
								  message.getDomainEventId(),
								  message.getTopic()));
			throw new UnprocessableEntityException(BUS0003E_INCOMPATIBLE_PAYLOAD_TYPE, 
												   payloadType,
												   message.getJavaType());
		}
		
		return toDomainEvent(payloadType, 
							 message);
	
	}

	private <E> DomainEvent<E> toDomainEvent(Class<E> payloadType, Message message) {
		return newDomainEvent(payloadType)
			   .withTopicName(message.getTopicName())
			   .withDomainEventId(message.getDomainEventId())
			   .withDomainEventName(message.getDomainEventName())
			   .withCorrelationId(message.getCorrelationId())
			   .withDateCreated(message.getDateCreated())
			   .withPayload(unmarshal(payloadType, message.getPayload()))
			   .build();
	}

	@Override
	public <E> List<DomainEvent<E>> findEvents(TopicName topic, 
											   DomainEventName event, 
											   Class<E> payloadType, 
											   int offset,
											   int limit) {
		return repository.execute(findMessagesByTopic(topic,event,offset,limit))
 		 		 		 .stream()
 		 		 		 .map(message -> toDomainEvent(payloadType,message))
 		 		 		 .collect(toList());	}

	@Override
	public SortedSet<TopicName> getTopicNames() {
		return repository.execute(findTopicNames());
	}
	
}
