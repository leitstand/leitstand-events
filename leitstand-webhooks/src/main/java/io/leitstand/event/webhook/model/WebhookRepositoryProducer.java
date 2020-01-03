package io.leitstand.event.webhook.model;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.transaction.TransactionScoped;

import io.leitstand.commons.model.Repository;

@Dependent
public class WebhookRepositoryProducer {

	@PersistenceUnit(unitName="webhooks")
	private EntityManagerFactory emf;
	
	@Produces
	@TransactionScoped
	@Webhooks
	public Repository createRepository() {
		return new Repository(emf.createEntityManager());
	}
	
	public void closeRepository(@Disposes @Webhooks Repository repository) {
		repository.close();
	}
	
}
