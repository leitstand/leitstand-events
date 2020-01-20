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
