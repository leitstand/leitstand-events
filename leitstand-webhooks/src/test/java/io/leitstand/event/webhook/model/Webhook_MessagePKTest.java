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
