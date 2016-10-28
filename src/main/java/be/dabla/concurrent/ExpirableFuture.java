package be.dabla.concurrent;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;

class ExpirableFuture<RESULT> implements Future<RESULT> {
	private static final Logger LOGGER = getLogger(ExpirableFuture.class);
	private final Future<RESULT> future;
	private final ExpirableRunnable expirableRunnable;
	
	ExpirableFuture(Future<RESULT> future, ExpirableRunnable expirableRunnable) {
		this.future = future;
		this.expirableRunnable = expirableRunnable;
	}
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		LOGGER.warn("Cancelling expired thread named {}", expirableRunnable.getThreadName());
		return future.cancel(mayInterruptIfRunning);
	}

	@Override
	public boolean isCancelled() {
		if (future.isCancelled()) {
			LOGGER.warn("Thread named {} was cancelled due to timeout!", expirableRunnable.getThreadName());
			return true;
		}
		
		return false;
	}

	@Override
	public boolean isDone() {
		return future.isDone();
	}

	@Override
	public RESULT get() throws InterruptedException, ExecutionException {
		return future.get();
	}

	@Override
	public RESULT get(long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		return future.get(timeout, unit);
	}
	
	public boolean isExpired() {
		return expirableRunnable.isExpired();
	}
}