/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.webhook.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.URI;

import org.junit.Test;

import io.leitstand.event.webhook.service.Endpoint;

public class EndpointTest {

	@Test
	public void null_URI_is_translated_to_null() {
		assertNull(Endpoint.valueOf((URI)null));
	}

	@Test
	public void URI_is_translated_to_endpoint() {
		assertEquals(Endpoint.valueOf("/foo/bar"),Endpoint.valueOf(URI.create("/foo/bar")));
	}

	
}
