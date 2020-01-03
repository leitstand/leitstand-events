package io.leitstand.event.queue.model;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.transaction.TransactionScoped;

import io.leitstand.commons.model.Repository;

@Dependent
public class EventQueueRepositoryProducer {

	@PersistenceUnit(unitName="events")
	private EntityManagerFactory emf;
	
	@Produces
	@TransactionScoped
	@EventQueue
	public Repository createRepository() {
		return new Repository(emf.createEntityManager());
	}
	
	public void closeRepository(@Disposes @EventQueue Repository repository) {
		repository.close();
	}
	
}
