package io.leitstand.event.queue.model;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.sql.DataSource;

import io.leitstand.commons.db.DatabaseService;

@Dependent
public class EventQueueDatabaseServiceProducer {

	@Resource(lookup="java:/jdbc/rbms")
	private DataSource ds;
	
	@Produces
	@ApplicationScoped
	@EventQueue
	public DatabaseService createDatabaseService() {
		return new DatabaseService(ds);
	}
	
}
