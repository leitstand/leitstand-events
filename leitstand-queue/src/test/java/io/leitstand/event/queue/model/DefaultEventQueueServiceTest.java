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

import static io.leitstand.event.queue.service.DomainEventId.randomDomainEventId;
import static io.leitstand.event.queue.service.ReasonCode.BUS0002E_MESSAGE_NOT_FOUND;
import static io.leitstand.event.queue.service.ReasonCode.BUS0003E_INCOMPATIBLE_PAYLOAD_TYPE;
import static io.leitstand.testing.ut.LeitstandCoreMatchers.reason;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.json.JsonObject;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.leitstand.commons.EntityNotFoundException;
import io.leitstand.commons.UnprocessableEntityException;
import io.leitstand.commons.model.Query;
import io.leitstand.commons.model.Repository;

@RunWith(MockitoJUnitRunner.class)
public class DefaultEventQueueServiceTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Mock
	private TopicProvider topicProvider;
	
	@Mock
	private Repository repository;
	
	@InjectMocks
	private DefaultEventQueueService service = new DefaultEventQueueService();
	
	
	@Test
	public void throws_EntityNotFoundException_for_unknown_domain_event() {
		exception.expect(EntityNotFoundException.class);
		exception.expect(reason(BUS0002E_MESSAGE_NOT_FOUND));
		
		service.getEvent(JsonObject.class, randomDomainEventId());
		
	}

	
	@Test
	public void throws_UnprocessableEntityException_if_event_types_do_not_match() {
		exception.expect(UnprocessableEntityException.class);
		exception.expect(reason(BUS0003E_INCOMPATIBLE_PAYLOAD_TYPE));
		
		Message message = mock(Message.class);
		when(repository.execute(any(Query.class))).thenReturn(message);
		when(message.getJavaType()).thenReturn(String.class.getName());
		when(message.getPayload()).thenReturn("foo");
		
		service.getEvent(JsonObject.class, randomDomainEventId());
		
	}
	
	
}
