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
import {Controller,Menu} from '/ui/js/ui.js';
import {Webhooks, Webhook, Message} from '/ui/modules/admin/webhook/webhook.js';

const TOPICS = [{'value':'','label':''},
				{'value':'element','label':'Element'},
				{'value':'image','label':'Image'},
				{'value':'metric', 'label':'Metric'}];

const BATCH_SIZES = [{'value':1, 'label': '1 message'},
	   				 {'value':2, 'label': '2 messages'},
	   				 {'value':3, 'label': '3 messages'},
	   				 {'value':4, 'label': '4 messages'},
	   				 {'value':5, 'label': '5 messages'},
	   				 {'value':6, 'label': '6 messages'},
	   				 {'value':7, 'label': '7 messages'},
	   				 {'value':8, 'label': '8 messages'},
	   				 {'value':9, 'label': '9 messages'},
	   				 {'value':10, 'label': '10 messages','default':true}];

const HTTP_METHODS = [{'value':'PUT'},
					  {'value':'POST', 'default':true}];
	
let webhooksController = function() {
	let hooks = new Webhooks();
	return new Controller({
		resource:hooks,
		viewModel:function(hooks){
			return {'hooks':hooks,
					'state':function(){
								return this.enabled ? 'UP' : 'DOWN';
					 },
					 'filter':this.location.param('filter')};
		},
		buttons:{
			'filter':function(){
				this.reload({'filter':this.getViewModel('filter')});
			}
		}	
	});
};

let newWebhookController = function(){
	let hooks = new Webhooks();
	return new Controller({
		resource:hooks,
		viewModel:function(){
			let viewModel = {'http_methods':HTTP_METHODS,
							 'topics':TOPICS,
							 'batch_sizes':BATCH_SIZES};
			
			viewModel.basic_auth = function(){
				return this.auth_mode == 'basic';
			};
			viewModel.bearer_auth = function(){
				return this.auth_mode == 'bearer';
			};
			return viewModel;
		},
		buttons:{
			'save-settings':function(){
				let settings = this.getViewModel('hook');
				let authmode = this.getViewModel('auth_mode');
				if(authmode != 'basic'){
					delete settings.user_id;
					delete settings.password;
					delete settings.cofirm_password;
				} 
				if (authmode != 'bearer'){
					delete settings.accesskey;
				}
				hooks.addHook(settings);
			}
		},
		onSuccess:function(){
			this.navigate('webhooks.html');
		}
	});
}	

let webhookController = function(){
	let hook = new Webhook();
	return new Controller({
		resource:hook,
		viewModel:function(settings){
			
			let viewModel = {'hook_id':settings.hook_id,
							 'hook_name':settings.hook_name,
							 'http_methods':HTTP_METHODS,
							 'topics':TOPICS,
							 'batch_sizes':BATCH_SIZES,
							 'hook':settings};
			viewModel.basic_auth = function(){
				return this.auth_mode == 'basic';
			};
			viewModel.bearer_auth = function(){
				return this.auth_mode == 'bearer';
			};

			// Set current auth mode
			if(settings.accesskey){
				viewModel.auth_mode = 'bearer';
			} else if (settings.user_id){
				viewModel.auth_mode = 'basic';
			}
			
			// Set current state
			viewModel.enabled = function(){
				return settings.enabled;
			}
			viewModel.disabled = function(){
				return !settings.enabled;
			}
			return viewModel;
		},
		buttons:{
			'save-settings':function(){
				let settings = this.getViewModel('hook');
				let authmode = this.getViewModel('auth_mode');
				if(authmode != 'basic'){
					delete settings.user_id;
					delete settings.password;
					delete settings.cofirm_password;
				} 
				if (authmode != 'bearer'){
					delete settings.accesskey;
				}
				hook.saveSettings(this.location.params,
								  settings);
					
			},
			'remove-webhook':function(){
				hook.removeHook(this.location.params);
			},
			'disable-webhook':function(){
				hook.disableHook(this.location.params);
			},
			'enable-webhook':function(){
				hook.enableHook(this.location.params);
			},
			'confirm-remove':function(){
				this.navigate({'view':'confirm-remove.html',
							   '?':this.location.params});
			}
		},
		onSuccess:function(){
			this.navigate('webhooks.html');
		}
	});
}
	
let webhookTemplateController = function(){
	let hook = new Webhook({'scope':'template'});
		return new Controller({
			resource:hook,
			buttons:{
				'save-template':function(){
					let settings = this.updateViewModel({'content_type':this.input('content_type').value(),
													 	 'template':this.input('template').value()})
					hook.saveSettings(this.location.params,
									  settings);
					
				},
				'remove-template':function(){
					this.input('content_type').value('application/json');
					this.input('template').value('');
					this.updateViewModel({'content_type':'application/json',
										  'template':null});
					hook.saveSettings(this.location.params,
							  settings);
				},
			},
			onSuccess:function(){
				this.navigate('webhooks.html');
			}
		});
}

let webhookMessageQueueController = function(){
	let webhook = new Webhook({"scope":"messages"});
	return new Controller({ resource : webhook,
							viewModel: function(messages){
								messages.correlationId = this.location.param("correlationId");
								return messages;
							},
							buttons: {
								"filter" : function(){
									let params = this.location.params;
									let correlationId = this.getViewModel("correlationId");
									if(correlationId){
										params.correlationId = correlationId;
										this.reload(params);
									} else {
										delete params.correlationId;
										this.reload(params);
									}
								}
								
							}});
};

let webhookStatisticsController = function(){
	let webhook = new Webhook({scope:"statistics"});
	return new Controller({resource:webhook,
						   buttons:{
							   "reset":function(){
								   webhook.retryFailed(this.location.params);
							   }
						   },
						   onSuccess:function(){
							   this.reload();
						   }});
};


let webhookMessageController = function(){
	let message = new Message();
	return new Controller({ resource : message,
						viewModel:function(message){
	  					  message.json_payload = function(){
	  							 return JSON.stringify(this.payload,null,' ');
	  					  };
	  					  message.rewritten_message = function(){
	  						  if(message.content_type == 'application/json'){
	  							  return JSON.stringify(JSON.parse(message.message),null,' ');
	  						  }
	  						  return message.message;
	  					  };
	  					  return message;
					  },
					  buttons:{
						 'retry': function(){
							  let hook = new Webhook();
							  this.attach(hook);
							  hook.resetHook({'hook':this.location.param('hook'),
							  			      'event':this.getViewModel('event').event_id});
						  },
						  'reset': function(){
							  let hook = new Webhook();
							  this.attach(hook);
							  hook.resetHook({'hook':this.location.param('hook'),
							  			      'event':this.getViewModel('event').event_id});
						  }
					  },
					  onNotFound:function(settings){
						  this.renderView({});
					  },
					  onSuccess:function(settings){
						  let hook   = this.location.param('hook');
						  this.navigate({'view':'message-queue.html',
							  			 '?':{'hook':hook}});
					  }});
	};

let messageQueueView = {
	'master':webhookMessageQueueController(),
	'details':{'message.html':webhookMessageController()}
};
	
let webhooksView = {
	'master':webhooksController(),
	'details':{'new-webhook.html':newWebhookController()}
};

let webhookView = {
	'master':webhookController(),
	'details':{'confirm-remove.html':webhookController()}
};

export const menu = new Menu({'webhooks.html':webhooksView,
							  'webhook.html':webhookView,
							  'webhook-statistics.html':webhookStatisticsController(),
							  'webhook-template.html':webhookTemplateController(),
							  'message-queue.html':messageQueueView});
