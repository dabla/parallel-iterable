package be.dabla.asserters;

import static be.dabla.asserters.Poller.aPoller;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeoutException;

import org.assertj.core.api.Assertions;

public class ThreadPoolExecutorAsserter {
    private final ThreadPoolExecutor executor;

    private ThreadPoolExecutorAsserter(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    public static ThreadPoolExecutorAsserter assertThat(ThreadPoolExecutor executor) {
        return new ThreadPoolExecutorAsserter(executor);
    }

    public ThreadPoolExecutorAsserter isIdle() {
        try {
            aPoller().doAssert(new Assertion() {
                @Override
                public void assertion() throws Exception {
                    Assertions.assertThat(executor.getActiveCount()).isZero();
                }
            });
        }
        catch(TimeoutException e) {
            throw new RuntimeException(e);
        }
        
        return this;
    }
    
    public ThreadPoolExecutorAsserter isActive() {
        try {
            aPoller().doAssert(new Assertion() {
                @Override
                public void assertion() throws Exception {
                    Assertions.assertThat(executor.getActiveCount()).isGreaterThan(0);
                }
            });
        }
        catch(TimeoutException e) {
            throw new RuntimeException(e);
        }
        
        return this;
    }
}