/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.webhook.model;

import static io.leitstand.commons.model.StringUtil.isNonEmptyString;
import static javax.persistence.EnumType.STRING;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import io.leitstand.commons.jpa.BooleanConverter;
import io.leitstand.commons.model.AbstractEntity;
import io.leitstand.commons.model.Query;
import io.leitstand.event.queue.model.Topic;
import io.leitstand.event.queue.service.TopicName;
import io.leitstand.event.webhook.jpa.EndpointConverter;
import io.leitstand.event.webhook.jpa.WebhookIdConverter;
import io.leitstand.event.webhook.jpa.WebhookNameConverter;
import io.leitstand.event.webhook.service.Endpoint;
import io.leitstand.event.webhook.service.WebhookId;
import io.leitstand.event.webhook.service.WebhookName;
import io.leitstand.event.webhook.service.WebhookSettings.HttpMethod;

@Entity
@Table(schema="bus", name="webhook")
@NamedQuery(name="Webhook.findByName",
			query="SELECT w FROM Webhook w WHERE w.name=:name")
@NamedQuery(name="Webhook.findById",
			query="SELECT w FROM Webhook w WHERE w.uuid=:id")
@NamedQuery(name="Webhook.findByNameFilter",
			query="SELECT w FROM Webhook w WHERE CAST(w.name AS TEXT) REGEXP :name ORDER by w.name ASC ")
@NamedQuery(name="Webhook.findAllEnabled",
			query="SELECT w FROM Webhook w WHERE w.enabled=true")
public class Webhook extends AbstractEntity{
	
	private static final int DEFAULT_BATCH_SIZE = 10;

	private static final long serialVersionUID = 1L;

	public static Query<Webhook> findWebhookByName(WebhookName name){
		return em -> em.createNamedQuery("Webhook.findByName",Webhook.class)
					   .setParameter("name",name)
					   .getSingleResult();
	}
	
	public static Query<Webhook> findWebhookById(WebhookId id){
		return em -> em.createNamedQuery("Webhook.findById",Webhook.class)
					   .setParameter("id",id)
					   .getSingleResult();
	}
	
	public static Query<List<Webhook>> findWebhooksByName(String filter){
		return em -> em.createNamedQuery("Webhook.findByNameFilter",Webhook.class)
					   .setParameter("name", filter)
					   .getResultList();
	}
	
	public static Query<List<Webhook>> findAllEnabledWebhooks() {
		return em -> em.createNamedQuery("Webhook.findAllEnabled",Webhook.class)
					   .getResultList();
	}
	
	@Convert(converter=WebhookIdConverter.class)
	private WebhookId uuid;
	
	@Column(unique=true)
	@Convert(converter=WebhookNameConverter.class)
	private WebhookName name;

	private String userName;
	private String pass64;
	private String akey64;
	@Enumerated(STRING)
	private HttpMethod method;
	@Convert(converter=EndpointConverter.class)
	private Endpoint endpoint;
	private String description;
	private String selector;
	@Convert(converter=BooleanConverter.class)
	private boolean enabled;
	
	@Min(value=1, message="{batch_size.out_of_range}")
	@Max(value=10, message="{batch_size.out_of_range}")
	private int batchSize;
	
	private String contentType;
	private String template;

	@ManyToOne
	@JoinColumn(name="topic_id", referencedColumnName="id")
	private Topic topic;
	
	public Webhook() {
		// JPA
	}
	
	public Webhook(Topic topic,
				   WebhookId webhookId,
				   WebhookName webhookName) {
		this.uuid = webhookId;
		this.name = webhookName;
		this.topic = topic;	
		this.enabled = true;
		this.batchSize = DEFAULT_BATCH_SIZE;
		// Default content type is application/json unless specified otherwise by a template definition.
		this.contentType = "application/json";
	}
	
	
	public WebhookId getWebhookId() {
		return uuid;
	}
	
	public WebhookName getWebhookName() {
		return name;
	}
	
	public void setWebhookName(WebhookName name) {
		this.name = name;
	}
	
	public String getUser() {
		return userName;
	}
	
	public String getPassword64() {
		return pass64;
	}
	
	public String getAccessKey64() {
		return akey64;
	}
	
	public boolean isBasicAuthentication() {
		return isNonEmptyString(userName);
	}

	public void setEndpoint(Endpoint endpoint) {
		this.endpoint = endpoint;
	}
	
	public Endpoint getEndpoint() {
		return endpoint;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}

	public HttpMethod getHttpMethod() {
		return method;
	}

	public void setHttpMethod(HttpMethod method) {
		this.method = method;
	}
	
	public void setPassword64(String pass64) {
		this.pass64 = pass64;
	}
	
	public void setUserName(String user) {
		this.userName = user;
	}
	
	public void setAccessKey64(String akey64) {
		this.akey64 = akey64;
	}

	public void setTopic(Topic topic) {
		this.topic = topic;
	}
	
	public Topic getTopic() {
		return topic;
	}

	public TopicName getTopicName() {
		return getTopic().getName();
	}
	
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public String getContentType() {
		return contentType;
	}
	
	public String getTemplate() {
		return template;
	}
	
	public void setTemplate(String template) {
		this.template = template;
	}
	
	public String getSelector() {
		return selector;
	}

	public void setSelector(String selector) {
		if(isNonEmptyString(selector)) {
			this.selector = selector;
		} else {
			this.selector = null;
		}
	}
	
	public int getBatchSize() {
		return batchSize;
	}
	
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public boolean isEnabled() {
		return enabled;
	}
	
	public void disable() {
		this.enabled = false;
	}

	public void enable() {
		this.enabled = true;
	}

	public boolean hasTemplate() {
		return isNonEmptyString(getTemplate());
	}

}