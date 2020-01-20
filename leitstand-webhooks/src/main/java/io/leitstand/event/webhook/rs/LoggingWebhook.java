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
package io.leitstand.event.webhook.rs;


import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;


@RequestScoped
@Path("webhooks")
@Consumes(APPLICATION_JSON)
public class LoggingWebhook {
	
	private static final Logger LOG = Logger.getLogger(LoggingWebhook.class.getName());

	@POST
	@Path("samples/post")
	public void post(String json) {
		LOG.info(json);
	}

	@PUT
	@Path("samples/put")
	public void put(String json) {
		LOG.info(json);
	}
	
}
