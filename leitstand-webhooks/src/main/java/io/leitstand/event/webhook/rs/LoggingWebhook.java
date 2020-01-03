/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
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