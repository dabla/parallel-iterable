package be.dabla.concurrent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;

import com.google.common.collect.Lists;

public class CancelExpiredRunnablesTest {
	@Mock
	private ExpirableFuture<?> expirableFuture;
	
	@Before
    public void setUp() throws Exception {
        initMocks(this);
        
        when(expirableFuture.isExpired()).thenReturn(true);
        when(expirableFuture.isCancelled()).thenReturn(true);
    }
	
	@Test
	public void run() throws Exception {
		CancelExpiredRunnables actual = new CancelExpiredRunnables(Lists.<ExpirableFuture<?>>newArrayList(expirableFuture));
		
		actual.run();
		
		assertThat(actual.expirableFutures).isEmpty();
		
		InOrder inOrder = inOrder(expirableFuture);
		inOrder.verify(expirableFuture).isExpired();
		inOrder.verify(expirableFuture).cancel(true);
		inOrder.verify(expirableFuture).isCancelled();
	}
}
