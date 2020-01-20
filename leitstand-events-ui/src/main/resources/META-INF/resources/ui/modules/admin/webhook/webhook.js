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
import {Resource} from '/ui/js/client.js';

/**
 * A reference to an existing webhook.
 * @typedef WebhookReference
 * @type {Object}
 * @property {String} hook_id unique, immutable webhook ID in UUIDv4 format
 * @property {String} hook_name unique hook name
 * @property {String} topic_name the subscribed topic
 * @property {String} [selector] regular expression to subscribe certain events only  
 * @property {boolean} enabled a flag indication whether the webhook is enabled (<code>true</code>) or not (<code>false</code>).
 * @property {String} [description] a webhook description
 */

/**
 * The webhook settings.
 * @typedef WebhookSettings
 * @type {Object}
 * @property {String} hook_id unique, immutable webhook ID in UUIDv4 format
 * @property {String} hook_name unique hook name
 * @property {String} topic_name the subscribed topic
 * @property {String} [selector] regular expression to subscribe certain events only  
 * @property {boolean} enabled a flag indication whether the webhook is enabled (<code>true</code>) or not (<code>false</code>).
 * @property {String} [description] an optional description of the webhook
 * @property {String} endpoint the webhook HTTP endpoint
 * @property {String} method the webhook HTTP request method
 * @property {Number} [batch_size=10] the batch size defines how many requests can be processed in one go. In case of an error, the entire batch will be executed again.
 * @property {String} [accesskey] the accesskey to authenticate the webhook invocation. The key is sent as bearer token.
 * @property {String} [user_id] the user name for HTTP basic authentication of the webhook invocation
 * @property {String} [password] the password for HTTP basic authentication
 * @property {String} [confirm_password] the confirmed password to detect accidental typos.
 */

/**
 * Collection of configured webhooks.
 */
export class Webhooks extends Resource {

	/**
	 * Loads all configured webhooks
	 * @returns {Promise} a promise to process the webhook collection or an error response
	 * @see WebhookReference
	 */
	load(params) {
		return this.json('/api/v1/webhooks?filter={{filter}}',
						 params)
				   .GET();
	}

	/**
	 * Adds a new webhook.
	 * @param {WebhookSettings} settings the webhook settings
	 */
	addHook(settings){
		return this.json("/api/v1/webhooks/")
				   .POST(settings);
	}

}

/**
 * A configured webhook.
 * <p>
 * This class provides methods to store, remove, and manage the state of a configured webhook.
 */
export class Webhook extends Resource {
	
	/**
	 * Creates a <code>Webhook</code> resource.
	 * @param {string} [cfg.scope] the scope being read, which is either <code>settings</code> or <code>template</code>
	 */
	constructor(cfg) {
		super()
		this._cfg = cfg;
	}
	
	/**
	 * Reads the webhook configuration.
	 * @param {String} params.hook the hook ID or the hook name
	 * @returns {Promise} a promise to process the REST API response
	 */
	load(params) {
		if(this._cfg && this._cfg.scope == "messages"){
			return this.json("/api/v1/webhooks/{{hook}}/messages?state={{state}}&correlationId={{correlationId}}&offset={{&offset}}&limit={{limit}}",
							 {"offset":"0","limit":"100"},
							 this._cfg,
							 params)
							 .GET();
		}
		
		return this.json("/api/v1/webhooks/{{hook}}/{{scope}}",
						 this._cfg,
						 params)
				   .GET();
	}

	/**
	 * Stores the webhook configuration.
	 * @param {String} params.hook the hook ID or the hook name
	 * @param {WebhookSettings} settings the webhook settings.
	 * @returns {Promise} a promise to process the REST API response
	 */
	saveSettings(params,settings){
		return this.json("/api/v1/webhooks/{{hook}}/{{scope}}",
						 this._cfg,
						 settings,
						 params)
				   .PUT(settings);
	}
	
	/**
	 * Removes a webhook.
	 * @param {Object} params the query parameters
	 * @returns {Promise} a promise to process the REST API response
	 */
	removeHook(params){
		return this.json("/api/v1/webhooks/{{hook}}",
						 this._cfg,
						 params)
				   .DELETE();
	}

	/**
	 * Enables a configured webhook.
	 * This operation has no effect when executed on already enabled webhooks.
	 * @param {String} params.hook the hook ID or the hook name
	 * @returns {Promise} a promise to process the REST API response
	 */
	enableHook(params){
		this.json("/api/v1/webhooks/{{hook}}/_enable",
				  this._cfg,
				  params)
			.POST();
	}
	
	/**
	 * Disables a configured webhook.
	 * This operation has no effect when executed on already disabled webhooks.
	 * @param {String} params.hook the hook ID or the hook name
	 * @returns {Promise} a promise to process the REST API response
	 */
	disableHook(params){
		return this.json("/api/v1/webhooks/{{hook}}/_disable",
						 this._cfg,
						 params)
				   .POST();
	}
	
	/**
	 * Resets a configured webhook to process a domain event and all subsequent events again.
	 * @param {String} params.hook the hook ID or the hook name
	 * @param {String} params.event_id the domain event in the message queue to reset the webhook to
	 * @returns {Promise} a promise to process the REST API response
	 */
	resetHook(params){
		return this.json("/api/v1/webhooks/{{hook}}/_reset?event_id={{event}}",
				  		 this._cfg,
				  		 params)
				   .POST();
	}
	
	retryFailed(params){
		return this.json("/api/v1/webhooks/{{hook}}/_retry",
		  		 		 this._cfg,
		  		 		 params)
		  		   .POST();
	}
		
}

export class Message extends Resource {
	
	constructor(cfg){
		super();
		this._cfg = cfg;
	}
	
	load(params){
		return this.json("/api/v1/webhooks/{{hook}}/messages/{{event}}",
						 this._cfg,params)
				   .GET();
	}
	
	retryFailed(){
		return this.json("/api/v1/webhooks/{{hook}}/messages/{{event}}/_retry",
						 this._cfg,
						 params)
				   .POST();
	}
}	
