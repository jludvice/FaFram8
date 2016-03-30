package org.jboss.fuse.qa.fafram8.test.local;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.jboss.fuse.qa.fafram8.executor.Executor;
import org.jboss.fuse.qa.fafram8.util.callables.Response;

import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * Test {@link Executor#waitFor(Callable, long)}
 *
 * @author Josef Ludvicek
 */
@Slf4j
public class LocalWaitForTest {

	@Test
	public void waitSuccess() {

		final AtomicInteger atomicInteger = new AtomicInteger(0);
		final int maxCount = 2;
		final Callable<Response<String>> callable = new Callable<Response<String>>() {
			@Override
			public Response<String> call() throws Exception {
				int newVal = atomicInteger.getAndIncrement();
				log.info("attempt {}", newVal);
				if (newVal >= maxCount) {
					return Response.success("success, attempts: " + newVal);
				}
				return Response.timeOut();
			}
		};

		val start = System.currentTimeMillis();
		val response = Executor.waitFor(callable, TimeUnit.MINUTES.toSeconds(1));
		// 2 attempts x 3 seconds timeout = 6 seconds for waiting  < 10
		val timeDiff = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start);

		assertTrue("Wait should succeed before defined timeout " + timeDiff + " < 10", timeDiff < 10);
		assertTrue("Waiting should succeed after 3 attempts", response.getSuccess());
		assertEquals("There should be data in response", "success, attempts: " + maxCount, response.getData());
	}

	@Test
	public void waitTimeOutWithMsg() {
		final AtomicInteger atomicInteger = new AtomicInteger(0);
		final int changeAt = 1;
		final Callable<Response<String>> callable = new Callable<Response<String>>() {
			@Override
			public Response<String> call() throws Exception {
				int newVal = atomicInteger.getAndIncrement();
				log.info("attempt {}", newVal);
				if (newVal >= changeAt) {
					log.info("setting timeout message at attempt {}", newVal);
					return Response.timeOut("timeout at: " + newVal);
				}
				return Response.timeOut();
			}
		};

		val response = Executor.waitFor(callable, 10);
		assertFalse("Waiting should succeed after 3 attempts", response.getSuccess());
		assertTrue("Timeout response should start with 'setting timeout'", response.getData().startsWith("timeout at"));
	}

	@Test
	public void timeOutException() {
		final Callable<Response<String>> callable = new Callable<Response<String>>() {
			@Override
			public Response<String> call() throws Exception {
				throw new Exception("call throwed exception");
			}
		};

		val response = Executor.waitFor(callable, 4);
		assertFalse("Waitfor should timeout", response.getSuccess());
		assertNull("Data should be null", response.getData());
	}
}
