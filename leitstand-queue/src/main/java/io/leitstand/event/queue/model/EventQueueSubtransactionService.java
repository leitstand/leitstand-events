/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.queue.model;

import javax.inject.Inject;
import javax.inject.Provider;

import io.leitstand.commons.model.Repository;
import io.leitstand.commons.model.Service;
import io.leitstand.commons.tx.SubtransactionService;

@Service
@EventQueue
public class EventQueueSubtransactionService extends SubtransactionService{

	@Inject
	@EventQueue
	private Repository repository;
	
	@Inject
	@EventQueue
	private Provider<SubtransactionService> provider;
	
	@Override
	protected Repository getRepository() {
		return repository;
	}

	@Override
	protected Provider<SubtransactionService> getServiceProvider() {
		return provider;
	}

}
