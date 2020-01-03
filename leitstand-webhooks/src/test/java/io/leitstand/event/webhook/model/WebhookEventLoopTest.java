/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
 */
package io.leitstand.event.webhook.model;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Semaphore;

import javax.enterprise.concurrent.ManagedExecutorService;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;

import io.leitstand.commons.messages.Messages;

@RunWith(MockitoJUnitRunner.class)
public class WebhookEventLoopTest {
	

	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Mock
	private Semaphore permits;
	
	@Mock
	private ManagedExecutorService wm;
	
	@Mock
	private WebhookInvocationService service;
	
	@Mock
	private Messages messages;
	
	@InjectMocks
	private WebhookEventLoop loop = new WebhookEventLoop();
	
	private ArgumentCaptor<WebhookBatchProcessor> processorCaptor;
	
	@Before
	public void initTestEnvironment() {
		processorCaptor = forClass(WebhookBatchProcessor.class);
		doNothing().when(wm).execute(processorCaptor.capture());
	}
	
	@Test
	public void start_does_nothing_when_loop_is_already_started() {
		loop.startEventLoop();
		reset(permits,wm,service);
		loop.startEventLoop();
		verifyZeroInteractions(wm,service,permits,messages);
	}
	
	@Test
	public void start_event_loop_when_stopped() {
		loop.startEventLoop();
		verify(wm).execute(loop);
		assertTrue(loop.isEnabled());
		verifyZeroInteractions(wm,service,permits,messages);
	}

	@Test
	public void stop_event_loop() {
		loop.stopEventLoop();
		assertFalse(loop.isEnabled());
		verifyZeroInteractions(wm,service,permits,messages);
	}	
	
	@Test
	public void run_does_nothing_when_loop_is_stopped() {
		loop.stopEventLoop();
		loop.run();
		verifyZeroInteractions(service,permits);
	}
	
	@Test
	public void wait_for_executable_batches() {
		List<WebhookBatch> batches = asList(mock(WebhookBatch.class));
		
		when(service.findInvocations()).thenReturn(emptyList())
										.thenReturn(batches);
		
		
		assertSame(batches,loop.batches());
		
	}
	
	@Test
	public void process_batches() throws InterruptedException {
		WebhookBatch batch = mock(WebhookBatch.class);
		when(service.findInvocations()).thenReturn(asList(batch));
		ArgumentCaptor<WebhookBatchProcessor> processorCaptor = ArgumentCaptor.forClass(WebhookBatchProcessor.class);
		// This test requires a little trick to cancel the event loop after processing the first batch.
		loop.startEventLoop();
		stopEventLoop().when(wm).execute(processorCaptor.capture());
		loop.run();
		verify(service).populateWebhookQueues();
		verify(permits).acquire();
		assertSame(batch,processorCaptor.getValue().getBatch());
		
	}

	private Stubber stopEventLoop() {
		return doAnswer(new Answer<>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				loop.stopEventLoop();
				return null;
			}
		});
	}
	
}
