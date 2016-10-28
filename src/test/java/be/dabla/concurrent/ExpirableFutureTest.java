package be.dabla.concurrent;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class ExpirableFutureTest {
	@Mock
	private Future<Void> future;
	@Mock
	private ExpirableRunnable expirableRunnable;
	
	@Before
    public void setUp() throws Exception {
        initMocks(this);
    }

	@Test
	public void isExpired_whenFalse() {
		ExpirableFuture<Void> actual = new ExpirableFuture<Void>(future, expirableRunnable);
		
		assertThat(actual.isExpired()).isFalse();
	}
	
	@Test
	public void isExpired_whenTrue() {
		when(expirableRunnable.isExpired()).thenReturn(true);
		
		ExpirableFuture<Void> actual = new ExpirableFuture<Void>(future, expirableRunnable);
		
		assertThat(actual.isExpired()).isTrue();
	}
	
	@Test
	public void cancel() {
		ExpirableFuture<Void> actual = new ExpirableFuture<Void>(future, expirableRunnable);
		
		actual.cancel(true);
		
		verify(future).cancel(true);
	}

	@Test
	public void isCancelled() {
		ExpirableFuture<Void> actual = new ExpirableFuture<Void>(future, expirableRunnable);
		
		actual.isCancelled();
		
		verify(future).isCancelled();
	}

	@Test
	public void isDone() {
		ExpirableFuture<Void> actual = new ExpirableFuture<Void>(future, expirableRunnable);
		
		actual.isDone();
		
		verify(future).isDone();
	}

	@Test
	public void get() throws InterruptedException, ExecutionException {
		ExpirableFuture<Void> actual = new ExpirableFuture<Void>(future, expirableRunnable);
		
		actual.get();
		
		verify(future).get();
	}
	
	@Test
	public void get_whenTimeoutSpecified() throws InterruptedException, ExecutionException, TimeoutException {
		ExpirableFuture<Void> actual = new ExpirableFuture<Void>(future, expirableRunnable);
		
		actual.get(1L, MINUTES);
		
		verify(future).get(1L, MINUTES);
	}
}
