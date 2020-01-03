/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
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
