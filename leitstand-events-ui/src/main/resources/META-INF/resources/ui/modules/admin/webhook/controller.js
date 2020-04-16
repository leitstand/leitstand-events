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
import {Select} from '/ui/js/ui-components.js';

class TopicSelector extends Select {
	
	options(){
		return Promise.resolve([
			{'value':'','label':''},
			{'value':'element','label':'Element'},
			{'value':'image','label':'Image'},
			{'value':'metric', 'label':'Metric'}
		]);
	}
	
}
customElements.define('webhook-topic',TopicSelector);

class WebhookHttpMethodSelector extends Select {
	
	options(){
		return Promise.resolve([
					{'value':'PUT'},
					{'value':'POST', 'default':true}
				]);
	}
}
customElements.define('webhook-method',WebhookHttpMethodSelector);

class WebhookBatchSizeSelector extends Select {
	
	options(){
		return Promise.resolve([
				 {'value':1, 'label': '1 message'},
				 {'value':2, 'label': '2 messages'},
   				 {'value':3, 'label': '3 messages'},
   				 {'value':4, 'label': '4 messages'},
   				 {'value':5, 'label': '5 messages'},
   				 {'value':6, 'label': '6 messages'},
   				 {'value':7, 'label': '7 messages'},
   				 {'value':8, 'label': '8 messages'},
   				 {'value':9, 'label': '9 messages'},
   				 {'value':10, 'label': '10 messages','default':true}
   		])
	}
}

customElements.define('webhook-batchsize',WebhookBatchSizeSelector);


const webhooksController = function() {
	const hooks = new Webhooks();
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

const newWebhookController = function(){
	const hooks = new Webhooks();
	return new Controller({
		resource:hooks,
		viewModel:function(){
			const viewModel = {};
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
				const settings = this.getViewModel('hook');
				const authmode = this.getViewModel('auth_mode');
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

const webhookController = function(){
	const hook = new Webhook();
	return new Controller({
		resource:hook,
		viewModel:function(settings){
			
			const viewModel = {'hook_id':settings.hook_id,
							   'hook_name':settings.hook_name,
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
	
const webhookTemplateController = function(){
	const hook = new Webhook({'scope':'template'});
		return new Controller({
			resource:hook,
			buttons:{
				'save-template':function(){
					const settings = this.updateViewModel({'content_type':this.input('content_type').value(),
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

const webhookMessageQueueController = function(){
	const webhook = new Webhook({'scope':'messages'});
	return new Controller({ 
		resource : webhook,
		viewModel: function(messages){
			messages.correlationId = this.location.param('correlationId');
			return messages;
		},
		buttons: {
			'filter' : function(){
				const params = this.location.params;
				const correlationId = this.getViewModel('correlationId');
				if(correlationId){
					params.correlationId = correlationId;
					this.reload(params);
				} else {
					delete params.correlationId;
					this.reload(params);
				}
			}
		}
	});
};

const webhookStatisticsController = function(){
	const webhook = new Webhook({scope:'statistics'});
	return new Controller({
		resource:webhook,
		buttons:{
			'reset':function(){
				webhook.retryFailed(this.location.params);
			}
		},
		onSuccess:function(){
			this.reload();
		}
	});
};


const webhookMessageController = function(){
	const message = new Message();
	return new Controller({ 
		resource : message,
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
				const hook = new Webhook();
				this.attach(hook);
				hook.resetHook({'hook':this.location.param('hook'),
				  			    'event':this.getViewModel('event').event_id});
			},
			'reset': function(){
				const hook = new Webhook();
				this.attach(hook);
				hook.resetHook({'hook':this.location.param('hook'),
							    'event':this.getViewModel('event').event_id});
			}
		},
		onNotFound:function(settings){
			this.renderView({});
		},
		onSuccess:function(settings){
			const hook   = this.location.param('hook');
			this.navigate({'view':'message-queue.html',
				  		   '?':{'hook':hook}});
		}
	});
};

const messageQueueView = {
	'master':webhookMessageQueueController(),
	'details':{'message.html':webhookMessageController()}
};
	
const webhooksView = {
	'master':webhooksController(),
	'details':{'new-webhook.html':newWebhookController()}
};

const webhookView = {
	'master':webhookController(),
	'details':{'confirm-remove.html':webhookController()}
};

export const menu = new Menu({'webhooks.html':webhooksView,
							  'webhook.html':webhookView,
							  'webhook-statistics.html':webhookStatisticsController(),
							  'webhook-template.html':webhookTemplateController(),
							  'message-queue.html':messageQueueView});
