/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.queue.model;

import static io.leitstand.commons.jsonb.JsonbDefaults.jsonb;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import io.leitstand.commons.model.AbstractEntity;
import io.leitstand.commons.model.Query;
import io.leitstand.event.queue.jpa.DomainEventIdConverter;
import io.leitstand.event.queue.jpa.DomainEventNameConverter;
import io.leitstand.event.queue.service.DomainEvent;
import io.leitstand.event.queue.service.DomainEventId;
import io.leitstand.event.queue.service.DomainEventName;
import io.leitstand.event.queue.service.TopicName;

@Entity
@Table(schema="bus",name="message")
@NamedQuery(name="Message.findById",
			query="SELECT m FROM Message m WHERE m.uuid=:id")
@NamedQuery(name="Message.findByTopic",
			query="SELECT m FROM Message m WHERE m.topic.topicName=:topic AND m.id >= :offset")
@NamedQuery(name="Message.findByTopicAndEventName",
			query="SELECT m FROM Message m WHERE m.topic.topicName=:topic AND m.name =:name AND m.id >= :offset")
@NamedQuery(name="Message.findByTopicAndEventSelector",
			query="SELECT m FROM Message m WHERE m.topic.topicName=:topic AND m.name REGEXP :name AND m.id >= :offset")
public class Message extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public static Query<Message> findMessageByDomainEventId(DomainEventId eventId){
		return em -> em.createNamedQuery("Message.findById",Message.class)
					   .setParameter("id",eventId)
					   .getSingleResult();
	}
	
	public static Query<List<Message>> findMessagesByTopic(TopicName topic, 
														   long offset, 
														   int limit) {
		return em -> em.createNamedQuery("Message.findByTopic",Message.class)
					   .setParameter("topic", topic)
					   .setParameter("offset", offset)
					   .setMaxResults(limit)
					   .getResultList();
	}
	
	public static Query<List<Message>> findMessagesByTopic(TopicName topic,
														   DomainEventName event, 
														   long offset, 
														   int limit) {
		return em -> em.createNamedQuery("Message.findByTopicAndEventName",Message.class)
				   		.setParameter("topic", topic)
				   		.setParameter("event",event)
				   		.setParameter("offset", offset)
				   		.setMaxResults(limit)
				   		.getResultList();
	}
	
	public static Query<List<Message>> findMessagesByTopic(TopicName topic,
			   											   String selector, 
			   											   long offset, 
			   											   int limit) {
		return em -> em.createNamedQuery("Message.findByTopicAndEventSelector", Message.class)
					   .setParameter("topic", topic)
					   .setParameter("event",selector)
					   .setParameter("offset", offset)
					   .setMaxResults(limit)
					   .getResultList();
	}
	
	@ManyToOne
	@JoinColumn(name="topic_id", referencedColumnName="id")
	private Topic topic;

	@Convert(converter=DomainEventIdConverter.class)
	private DomainEventId uuid;
	@Convert(converter=DomainEventNameConverter.class)
	private DomainEventName name;
	private String correlationId;
	@Column(name="type")
	private String javaType;
	@Column(name="message")
	private String payload;

	
	protected Message() {
		// JPA
	}
	
	public Message(Topic topic, DomainEvent<?> event) {
		this.topic = topic;
		this.uuid = event.getDomainEventId();
		this.name = event.getDomainEventName();
		this.correlationId = event.getCorrelationId();
		Object eventPayload = event.getPayload();
		if(eventPayload != null) {
			this.javaType = eventPayload.getClass().getName();
			this.payload = jsonb().toJson(eventPayload);
		}
	}
	
	public DomainEventId getDomainEventId(){
		return uuid;
	}

	public DomainEventName getDomainEventName() {
		return name;
	}
	
	public String getCorrelationId() {
		return correlationId;
	}
	
	public Topic getTopic() {
		return topic;
	}
	
	public String getJavaType() {
		return javaType;
	}
	
	public String getPayload() {
		return payload;
	}

	public TopicName getTopicName() {
		return getTopic().getName();
	}

}
