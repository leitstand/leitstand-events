/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.webhook.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class Webhook_MessagePKTest {

	@Test
	public void equals_is_reflexive() {
		Webhook_MessagePK pk = new Webhook_MessagePK();
		assertTrue(pk.equals(pk));
	}
	
	@Test
	public void equals_is_null_safe() {
		Webhook_MessagePK pk = new Webhook_MessagePK();
		assertFalse(pk.equals(null));
	}
	
	@Test
	public void equals_detects_invalid_class() {
		Webhook_MessagePK pk = new Webhook_MessagePK();
		assertFalse(pk.equals(new Object()));
	}
	
	@Test
	public void equals_detects_different_webhook() {
		
		Webhook_MessagePK pk1 = new Webhook_MessagePK(1L, 1L);
		Webhook_MessagePK pk2 = new Webhook_MessagePK(2L, 1L);		
		
		assertFalse(pk1.equals(pk2));
		assertFalse(pk2.equals(pk1));
	}
	
	@Test
	public void equals_detects_different_message() {
		
		Webhook_MessagePK pk1 = new Webhook_MessagePK(1L, 1L);
		Webhook_MessagePK pk2 = new Webhook_MessagePK(1L, 2L);		
		
		assertFalse(pk1.equals(pk2));
		assertFalse(pk2.equals(pk1));
	}

	@Test
	public void equals_detects_equal_message() {
		
		Webhook_MessagePK pk1 = new Webhook_MessagePK(1L, 1L);
		Webhook_MessagePK pk2 = new Webhook_MessagePK(1L, 1L);		
		
		assertTrue(pk1.equals(pk2));
		assertTrue(pk2.equals(pk1));
	}
	
}
