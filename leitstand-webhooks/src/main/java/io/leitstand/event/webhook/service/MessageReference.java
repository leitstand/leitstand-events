/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.webhook.service;

import static io.leitstand.commons.model.BuilderUtil.assertNotInvalidated;

import java.util.Date;

import io.leitstand.commons.model.ValueObject;
import io.leitstand.event.queue.service.DomainEventId;
import io.leitstand.event.queue.service.DomainEventName;
import io.leitstand.event.queue.service.TopicName;

public class MessageReference extends ValueObject{

	public static Builder newMessageReference() {
		return new Builder();
	}
	
	public static class Builder {
		private MessageReference message = new MessageReference();
		
		public Builder withDomainEventId(DomainEventId eventId) {
			assertNotInvalidated(getClass(),message);
			message.eventId = eventId;
			return this;
		}
		
		public Builder withDomainEventName(DomainEventName eventName) {
			assertNotInvalidated(getClass(),message);
			message.eventName = eventName;
			return this;
		}

		public Builder withTopicName(TopicName topicName) {
			assertNotInvalidated(getClass(),message);
			message.topicName = topicName;
			return this;
		}

		
		public Builder withCorrelationId(String correlationId) {
			assertNotInvalidated(getClass(),message);
			message.correlationId = correlationId;
			return this;
		}
		
		public Builder withMessageState(MessageState messageState) {
			assertNotInvalidated(getClass(),message);
			message.messageState = messageState;
			return this;
		}
		
		public Builder withHttpStatus(Integer httpStatus) {
			assertNotInvalidated(getClass(),message);
			message.httpStatus = httpStatus;
			return this;
		}
		
		public Builder withExecutionTime(Long duration) {
			assertNotInvalidated(getClass(),message);
			message.execTime = duration;
			return this;
		}
		
		public Builder withDateModified(Date dateModified) {
			assertNotInvalidated(getClass(), message);
			message.dateModified = new Date(dateModified.getTime());
			return this;
		}
		
		public MessageReference build() {
			try {
				assertNotInvalidated(getClass(), message);
				return message;
			} finally {
				this.message = null;
			}
		}
	}
	
	
	private DomainEventId eventId;
	private DomainEventName eventName;
	private String correlationId;
	private TopicName topicName;
	private MessageState messageState;
	private Integer httpStatus;
	private Long execTime;
	private Date dateModified;
	
	public DomainEventId getEventId() {
		return eventId;
	}
	
	public DomainEventName getEventName() {
		return eventName;
	}
	
	public Long getCallDuration() {
		return execTime;
	}
	
	public Integer getHttpStatus() {
		return httpStatus;
	}
	
	public MessageState getMessageState() {
		return messageState;
	}
	
	public String getCorrelationId() {
		return correlationId;
	}
	
	public Date getDateModified() {
		return new Date(dateModified.getTime());
	}
	
	public TopicName getTopicName() {
		return topicName;
	}
}
