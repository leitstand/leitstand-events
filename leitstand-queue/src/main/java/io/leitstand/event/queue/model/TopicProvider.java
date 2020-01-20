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

import static io.leitstand.event.queue.model.Topic.findTopicByName;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import io.leitstand.commons.model.Repository;
import io.leitstand.commons.tx.Flow;
import io.leitstand.commons.tx.SubtransactionService;
import io.leitstand.event.queue.service.TopicName;

@Dependent
public class TopicProvider {

	static class TopicCreationFlow implements Flow<Topic>{

		private TopicName topicName;

		public TopicCreationFlow(TopicName name) {
			this.topicName = name;
		}
		
		@Override
		public void transaction(Repository repository) {
			Topic topic = new Topic(topicName);
			repository.add(topic);
		}

		@Override
		public Topic resume(Repository repository) {
			return repository.execute(findTopicByName(topicName));
		}
		
		public TopicName getTopicName() {
			return topicName;
		}
		
	}
	
	private Repository repository;
	private SubtransactionService tx;

	protected TopicProvider() {
		// CDI
	}
	
	@Inject
	public TopicProvider(@EventQueue Repository repo,
						 @EventQueue SubtransactionService tx) {
		this.repository = repo;
		this.tx = tx;
	}

	public Topic getOrCreateTopic(TopicName topicName) {
		Topic topic = repository.execute(findTopicByName(topicName));
		if(topic == null) {
			// Try to create a new topic
			topic = tx.run(new TopicCreationFlow(topicName));
		}
		return topic;
	}
	
}
