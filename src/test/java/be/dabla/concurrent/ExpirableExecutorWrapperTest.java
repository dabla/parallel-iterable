package be.dabla.concurrent;

import static be.dabla.asserters.Sleeper.sleep;
import static be.dabla.asserters.ThreadPoolExecutorAsserter.assertThat;
import static be.dabla.concurrent.ExpirableExecutorWrapper.expirableExecutorWrapper;
import static java.lang.Integer.MAX_VALUE;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.AfterClass;
import org.junit.Test;

public class ExpirableExecutorWrapperTest {
	private final static ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
	private static final long TIMEOUT = 5L;
	
	private static ExpirableExecutorWrapper expirableExecutorWrapper = expirableExecutorWrapper(executor, TIMEOUT, SECONDS);
	
	@AfterClass
	public static void tearDown() {
		expirableExecutorWrapper.shutdown();
	}
	
	@Test
	public void execute() throws Exception {
		LongRunningTask task1 = new LongRunningTask();
		LongRunningTask task2 = new LongRunningTask();
		LongRunningTask task3 = new LongRunningTask();
		LongRunningTask task4 = new LongRunningTask();
		
		expirableExecutorWrapper.execute(task1);
		expirableExecutorWrapper.execute(task2);
		expirableExecutorWrapper.execute(task3);
		expirableExecutorWrapper.execute(task4);
		
		assertThat(executor).isIdle();
		assertThat(task1.getValue()).isGreaterThan(0);
		assertThat(task2.getValue()).isGreaterThan(0);
		assertThat(task3.getValue()).isGreaterThan(0);
		assertThat(task4.getValue()).isGreaterThan(0);
	}

	private static class LongRunningTask implements Runnable {
		final AtomicInteger value = new AtomicInteger();
		
		@Override
		public void run() {
			for (int i = 0; i < MAX_VALUE; i++) {
				value.incrementAndGet();
				sleep(10);
			}
		}
		
		public int getValue() {
			return value.get();
		}
	}
}
