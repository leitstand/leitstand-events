/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.webhook.model;

import static io.leitstand.commons.model.StringUtil.isEmptyString;
import static io.leitstand.event.webhook.service.MessageState.READY;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.TemporalType.TIMESTAMP;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQuery;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;

import io.leitstand.commons.model.Query;
import io.leitstand.commons.model.Update;
import io.leitstand.event.queue.model.Message;
import io.leitstand.event.queue.model.Topic;
import io.leitstand.event.queue.service.DomainEventId;
import io.leitstand.event.queue.service.DomainEventName;
import io.leitstand.event.queue.service.TopicName;
import io.leitstand.event.webhook.service.MessageFilter;
import io.leitstand.event.webhook.service.MessageState;

@Table(schema="BUS", name="webhook_message")
@Entity
@IdClass(Webhook_MessagePK.class)
@NamedQuery(name="Webhook_Message.findMessage",
			query="SELECT m FROM Webhook_Message m JOIN FETCH m.message WHERE m.webhook=:webhook AND m.message.uuid=:event")
@NamedQuery(name="Webhook_Message.findMessages",
			query="SELECT m FROM Webhook_Message m JOIN FETCH m.message WHERE m.webhook=:webhook ORDER BY m.webhook.id, m.message.id DESC")
@NamedQuery(name="Webhook_Message.findMessagesInState",
			query="SELECT m FROM Webhook_Message m JOIN FETCH m.message WHERE m.webhook=:webhook AND m.state=:state ORDER BY m.webhook.id, m.message.id DESC")
@NamedQuery(name="Webhook_Message.findMessagesByCorrelationId",
			query="SELECT m FROM Webhook_Message m JOIN FETCH m.message WHERE m.webhook=:webhook AND m.message.correlationId=:correlationId ORDER BY m.webhook.id, m.message.id DESC")
@NamedQuery(name="Webhook_Message.findMessagesInStateByCorrelationId",
			query="SELECT m FROM Webhook_Message m JOIN FETCH m.message WHERE m.webhook=:webhook AND m.message.correlationId=:correlationId  AND m.state=:state ORDER BY m.webhook.id, m.message.id DESC")
@NamedQuery(name="Webhook_Message.resetFailedCalls",
			query="UPDATE Webhook_Message m SET m.state=io.leitstand.event.webhook.service.MessageState.READY,m.execTime=NULL,m.httpStatus=NULL WHERE m.webhook=:webhook AND m.state=io.leitstand.event.webhook.service.MessageState.FAILED")
@NamedQuery(name="Webhook_Message.resetWebhook",
			query="UPDATE Webhook_Message m SET m.state=io.leitstand.event.webhook.service.MessageState.READY,m.execTime=NULL,m.httpStatus=NULL WHERE m.webhook=:webhook AND m.message.id >= :message")
public class Webhook_Message {
	
	public static Update resetWebhookCalls(Webhook webhook, Message message) {
		return em -> em.createNamedQuery("Webhook_Message.resetWebhook",int.class)
					   .setParameter("webhook", webhook)
					   .setParameter("message", message.getId())
					   .executeUpdate();
	}
	
	public static Update retryFailedCalls(Webhook webhook) {
		return em -> em.createNamedQuery("Webhook_Message.resetFailedCalls",int.class)
					   .setParameter("webhook",webhook)
					   .executeUpdate();
	}
	
	public static Query<Webhook_Message> findMessage(Webhook webhook, DomainEventId eventId) {
		return em -> em.createNamedQuery("Webhook_Message.findMessage",Webhook_Message.class)
					   .setParameter("webhook", webhook)
					   .setParameter("event", eventId)
					   .getSingleResult();
	}
	
	public static Query<List<Webhook_Message>> findWebhookMessages(Webhook webhook, 
																   MessageFilter filter){
		MessageState state = filter.getMessageState();
		String correlationId = filter.getCorrelationId();
		int offset = filter.getOffset();
		int limit = filter.getLimit();
		if(state == null) {
			if(isEmptyString(correlationId)) {
				return em -> em.createNamedQuery("Webhook_Message.findMessages",Webhook_Message.class)
							   .setParameter("webhook", webhook)
							   .setFirstResult(offset)
							   .setMaxResults(limit)
							   .getResultList();
			}
			return em -> em.createNamedQuery("Webhook_Message.findMessagesByCorrelationId",Webhook_Message.class)
						   .setParameter("webhook", webhook)
						   .setParameter("correlationId", correlationId)
						   .getResultList();		
			
		}
		
		if(isEmptyString(correlationId)) {
			return em -> em.createNamedQuery("Webhook_Message.findMessagesInState",Webhook_Message.class)
						   .setParameter("webhook", webhook)
						   .setParameter("state", state)
						   .setFirstResult(offset)
						   .setMaxResults(limit)
						   .getResultList();
		}
		
		return em -> em.createNamedQuery("Webhook_Message.findMessagesInStateByCorrelationId",Webhook_Message.class)
				   	   .setParameter("webhook", webhook)
				   	   .setParameter("state", state)
				   	   .setParameter("correlationId",correlationId)
				   	   .setFirstResult(offset)
				   	   .setMaxResults(limit)
				   	   .getResultList();
		
	}
	
	@Id
	@JoinColumn(name="webhook_id")
	private Webhook webhook;
	@Id
	@JoinColumn(name="message_id")
	private Message message;
	@Enumerated(STRING)
	private MessageState state;
	private Integer httpStatus;
	@Temporal(TIMESTAMP)
	private Date tsmodified;
	private Long execTime;
	
	protected Webhook_Message() {
		// JPA
	}
	
	public Webhook_Message(Webhook webhook, Message message) {
		this.webhook = webhook;
		this.message = message;
		this.state = READY;
	}
	
	@PreUpdate
	protected void touchDateModified() {
		tsmodified = new Date();
	}
	
	public Webhook getWebhook() {
		return webhook;
	}
	
	protected Message getMessage() {
		return message;
	}
	
	public MessageState getMessageState() {
		return state;
	}
	
	public Integer getHttpStatus() {
		return httpStatus;
	}
	
	public void setMessageState(MessageState state) {
		this.state = state;
	}
	
	public void setHttpStatus(int httpStatus) {
		this.httpStatus = httpStatus;
	}

	public void setExecTime(Long exectTime) {
		this.execTime = exectTime;
	}
	
	public Long getExecutionTime() {
		return this.execTime;
	}

	public String getCorrelationId() {
		return message.getCorrelationId();
	}

	public Date getDateCreated() {
		return message.getDateCreated();
	}

	public Date getDateModified() {
		return message.getDateModified();
	}

	public DomainEventId getDomainEventId() {
		return message.getDomainEventId();
	}

	public DomainEventName getDomainEventName() {
		return message.getDomainEventName();
	}

	public Topic getTopic() {
		return message.getTopic();
	}

	public String getJavaType() {
		return message.getJavaType();
	}

	public String getPayload() {
		return message.getPayload();
	}

	public TopicName getTopicName() {
		return message.getTopicName();
	}

}
