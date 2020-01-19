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
package io.leitstand.event.webhook.service;

/**
 * A transactional service to manage the global webhook event loop.
 */
public interface WebhookEventLoopService {

	
	/**
	 * Starts the global webhook event loop.
	 * Does nothing when the event loop is already started.
	 */
	void startEventLoop();
	
	/**
	 * Stops the global webhook event loop.
	 * Does nothing when the event loop is not running.
	 */
	void stopEventLoop();
	
	/** 
	 * Returns the current webhook event loop status.
	 * @return the current webhook event loop status.
	 */
	WebhookEventLoopStatus getWebhookEventLoopStatus();
	
}
