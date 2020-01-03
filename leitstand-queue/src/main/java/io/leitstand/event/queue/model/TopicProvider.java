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
