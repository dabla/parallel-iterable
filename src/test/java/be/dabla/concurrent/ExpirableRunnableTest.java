package be.dabla.concurrent;

import static be.dabla.asserters.Poller.aPoller;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import be.dabla.asserters.Assertion;

public class ExpirableRunnableTest {
	@Mock
	private Runnable runnable;
	
	@Before
    public void setUp() throws Exception {
        initMocks(this);
    }
	
	@Test
	public void isRunning_whenFalse() throws Exception {
		ExpirableRunnable actual = new ExpirableRunnable(runnable, 1L, MINUTES);
		
		assertThat(actual.isRunning()).isFalse();
	}
	
	@Test
	public void isRunning_whenTrue() throws Exception {
		ExpirableRunnable actual = new ExpirableRunnable(runnable, 1L, MINUTES);
		
		actual.run();
		
		assertThat(actual.isRunning()).isTrue();
	}
	
	@Test(expected=NullPointerException.class)
	public void getThreadName_whenNotRunning() throws Exception {
		ExpirableRunnable actual = new ExpirableRunnable(runnable, 1L, MINUTES);
		
		actual.getThreadName();
	}
	
	@Test
	public void getThreadName_whenRunning() throws Exception {
		ExpirableRunnable actual = new ExpirableRunnable(runnable, 1L, MINUTES);
		
		actual.run();
		
		assertThat(actual.getThreadName()).isEqualTo(currentThread().getName());
	}
	
	@Test
	public void isExpired_whenFalse() throws Exception {
		ExpirableRunnable actual = new ExpirableRunnable(runnable, 1L, MINUTES);
		
		actual.run();
		
		assertThat(actual.isExpired()).isFalse();
	}
	
	@Test
	public void isExpired_whenTrue() throws Exception {
		final ExpirableRunnable actual = new ExpirableRunnable(runnable, 1L, SECONDS);
		
		actual.run();
		
		aPoller().doAssert(new Assertion() {
			@Override
			public void assertion() throws Exception {
				assertThat(actual.isExpired()).isTrue();
			}
		});
	}
}
