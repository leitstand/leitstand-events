/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.queue.model;

import static io.leitstand.event.queue.service.TopicName.topicName;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.leitstand.commons.model.Query;
import io.leitstand.commons.model.Repository;
import io.leitstand.commons.tx.SubtransactionService;
import io.leitstand.event.queue.service.TopicName;

@RunWith(MockitoJUnitRunner.class)
public class TopicProviderTest {
	
	private static final TopicName TOPIC_NAME = topicName("topic");

	@Mock
	private Repository repository;
	
	@Mock
	private SubtransactionService tx;
	
	@InjectMocks
	private TopicProvider provider = new TopicProvider();
	
	@Test
	public void return_existing_topic() {
		Topic topic = mock(Topic.class);
		when(repository.execute(any(Query.class))).thenReturn(topic);
		
		assertSame(topic,provider.getOrCreateTopic(TOPIC_NAME));
		verifyZeroInteractions(tx);
	}

	@Test
	public void create_new_topic_if_topic_does_not_exist() {
		Topic topic = mock(Topic.class);
		ArgumentCaptor<TopicProvider.TopicCreationFlow> flowCaptor = ArgumentCaptor.forClass(TopicProvider.TopicCreationFlow.class);
		when(tx.run(flowCaptor.capture())).thenReturn(topic);
		
		assertSame(topic,provider.getOrCreateTopic(TOPIC_NAME));
		assertEquals(TOPIC_NAME,flowCaptor.getValue().getTopicName());
	}

	
}
