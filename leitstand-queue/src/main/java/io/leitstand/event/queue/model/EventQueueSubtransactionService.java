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
