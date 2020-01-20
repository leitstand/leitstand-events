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
package io.leitstand.event.webhook.model;

import static java.util.Collections.unmodifiableList;

import java.util.List;

import javax.security.enterprise.credential.Password;

import io.leitstand.event.webhook.service.WebhookId;
import io.leitstand.event.webhook.service.WebhookName;
import io.leitstand.event.webhook.service.WebhookSettings.HttpMethod;
import io.leitstand.security.auth.UserName;

public class WebhookBatch {

	static Builder newWebhookBatch() {
		return new Builder();
	}
	
	static class Builder {
		private WebhookBatch batch = new WebhookBatch();
		
		public Builder withWebhookId(WebhookId webhookId) {
			batch.webhookId = webhookId;
			return this;
		}

		public Builder withWebhookPK(Long id) {
			batch.webhookPK = id;
			return this;
		}
		
		public Builder withWebhookName(WebhookName name) {
			batch.webhookName = name;
			return this;
		}

		public Builder withHttpMethod(HttpMethod method) {
			batch.method = method;
			return this;
		}
		
		public Builder withUserName(UserName userName) {
			batch.userName = userName;
			return this;
		}
		
		public Builder withPassword(Password password) {
			batch.password = password;
			return this;
		}
		
		public Builder withAccesskey(String accesskey) {
			batch.accesskey = accesskey;
			return this;
		}		
		
		public Builder withContentType(String contentType) {
			batch.contentType = contentType;
			return this;
		}
		
		public Builder withWebhookInvocations(List<WebhookInvocation> invocations) {
			batch.invocations = invocations;
			return this;
		}
		
		public WebhookBatch build() {
			try {
				return batch;
			} finally {
				this.batch = null;
			}
		}
		
	}
	

	private WebhookId webhookId;
	private Long webhookPK;
	private WebhookName webhookName;
	private HttpMethod method;
	private UserName userName;
	private Password password;
	private String accesskey;
	private String contentType;
	private List<WebhookInvocation> invocations;
 	

	public Long getWebhookPK() {
		return webhookPK;
	}
	
	public WebhookId getWebhookId() {
		return webhookId;
	}

	public String getContentType() {
		return contentType;
	}
	
	public WebhookName getWebhookName() {
		return webhookName;
	}
	
	public HttpMethod getMethod() {
		return method;
	}
	
	public UserName getUserName() {
		return userName;
	}
	
	public Password getPassword() {
		return password;
	}
	
	public String getAccesskey() {
		return accesskey;
	}
	
	public List<WebhookInvocation> getWebhookInvocations() {
		return unmodifiableList(invocations);
	}
	
	
}
