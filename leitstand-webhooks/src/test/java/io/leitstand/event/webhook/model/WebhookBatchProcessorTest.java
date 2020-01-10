/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.webhook.model;

import static io.leitstand.commons.model.StringUtil.toUtf8Bytes;
import static io.leitstand.event.webhook.service.WebhookSettings.HttpMethod.POST;
import static io.leitstand.event.webhook.service.WebhookSettings.HttpMethod.PUT;
import static java.lang.String.format;
import static java.util.Base64.getEncoder;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import javax.security.enterprise.credential.Password;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import io.leitstand.event.webhook.service.Endpoint;
import io.leitstand.security.auth.UserName;

public class WebhookBatchProcessorTest {

	private WebhookBatch batch;
	private WebhookInvocation invocation;
	private WebhookEventLoop loop;
	private WebhookBatchProcessor processor;
	private Builder call;
	private Client client;
	
	
	@Before
	public void initTestEnvironment() {
		batch = mock(WebhookBatch.class);
		invocation   = mock(WebhookInvocation.class);
		loop	  = mock(WebhookEventLoop.class);
		processor = new WebhookBatchProcessor(loop, batch);
		call 	  = mock(Builder.class);
		client	  = mock(Client.class);
		when(invocation.getEndpoint()).thenReturn(Endpoint.valueOf("http://test.leitstand.io"));
		WebTarget target = mock(WebTarget.class);
		when(target.request()).thenReturn(call);
		when(client.target(invocation.getEndpoint().toUri())).thenReturn(target);
	}
	
	@Test
	public void set_basic_authentication_when_user_password_credentials_exist() {
		when(batch.getUserName()).thenReturn(new UserName("unittest"));
		when(batch.getPassword()).thenReturn(new Password("password"));
		
		processor.authenticate(call);
		
		verify(call).header("Authorization",format("Basic %s",getEncoder().encodeToString(toUtf8Bytes("unittest:password"))));
	}
	
	@Test
	public void set_bearer_token_when_access_key_exists() {
		when(batch.getAccesskey()).thenReturn("ENCODED_ACCESS_KEY");
		
		processor.authenticate(call);
		
		verify(call).header("Authorization", format("Bearer ENCODED_ACCESS_KEY"));
	}
	
	@Test
	public void omit_authorization_header_for_unauthenticated_request() {
		processor.authenticate(call);
		verifyZeroInteractions(call);
	}
	
	@Test
	public void notify_loop_about_successful_invocation_invocation() {
		when(invocation.getContentType()).thenReturn("application/json");
		when(invocation.getMessage()).thenReturn("unittest");
		when(batch.getMethod()).thenReturn(POST);
		Response success = mock(Response.class);
		when(success.getStatusInfo()).thenReturn(OK);
		when(call.post(any(Entity.class))).thenReturn(success);
		
		
		processor.call(client,invocation);
		
		verify(loop).webhookSucceeded(eq(batch),
								   	  eq(invocation),
								   	  eq(OK), 
								   	  anyLong());
	}
	
	@Test
	public void notify_loop_about_failed_invocation() {
		when(invocation.getContentType()).thenReturn("application/json");
		when(invocation.getMessage()).thenReturn("unittest");
		when(batch.getMethod()).thenReturn(POST);
		Response success = mock(Response.class);
		when(success.getStatusInfo()).thenReturn(BAD_REQUEST);
		when(call.post(any(Entity.class))).thenReturn(success);
		
		processor.call(client,invocation);
		verify(loop).webhookFailed(eq(batch),
								   eq(invocation),
								   eq(BAD_REQUEST), 
								   anyLong());
	}
	
	@Test
	public void invoke_put() {
		when(invocation.getContentType()).thenReturn("text/plain");
		when(invocation.getMessage()).thenReturn("unittest");
		when(batch.getMethod()).thenReturn(PUT);

		ArgumentCaptor<Entity> entityCaptor = forClass(Entity.class);
		when(call.put(entityCaptor.capture())).thenReturn(null);
		
		processor.invokeWebhook(call,invocation);
		
		Entity entity = entityCaptor.getValue();
		assertEquals("text/plain",entity.getMediaType().toString());
		assertEquals("unittest",entity.getEntity());
	}
	
	@Test
	public void invoke_post() {
		when(invocation.getContentType()).thenReturn("application/json");
		when(invocation.getMessage()).thenReturn("unittest");
		when(batch.getMethod()).thenReturn(POST);

		ArgumentCaptor<Entity> entityCaptor = forClass(Entity.class);
		when(call.post(entityCaptor.capture())).thenReturn(null);
		
		processor.invokeWebhook(call,invocation);
		
		Entity entity = entityCaptor.getValue();
		assertEquals("application/json",entity.getMediaType().toString());
		assertEquals("unittest",entity.getEntity());
	}
	
}
