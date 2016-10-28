package be.dabla.concurrent;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Thread.MAX_PRIORITY;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class ExpirableExecutorWrapper implements ExecutorService {
	private static final ScheduledExecutorService watchdog = new ScheduledThreadPoolExecutor(1, new ThreadFactoryBuilder().setNameFormat("expirable-executor-watchdog-%d").setPriority(MAX_PRIORITY).build());
	private static final Collection<ExpirableFuture<?>> expirableFutures = newArrayList();
	private final ExecutorService executor;
	private final long timeout;
	private final TimeUnit unit;

	private ExpirableExecutorWrapper(ExecutorService executor, long timeout, TimeUnit unit) {
		this.executor = executor;
		this.timeout = timeout;
		this.unit = unit;
	}
	
	public static ExpirableExecutorWrapper expirableExecutorWrapper(ExecutorService executor, long timeout, TimeUnit unit) {
		watchdog.scheduleAtFixedRate(new CancelExpiredRunnables(expirableFutures), 0, timeout, unit);
		return new ExpirableExecutorWrapper(executor, timeout, unit);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void execute(final Runnable runnable) {
		final ExpirableRunnable expirableRunnable = new ExpirableRunnable(runnable, timeout, unit);
		final Future<?> future = executor.submit(expirableRunnable);
		expirableFutures.add(new ExpirableFuture(future, expirableRunnable));
	}

	@Override
	public void shutdown() {
		expirableFutures.clear();
		executor.shutdown();
	}

	@Override
	public List<Runnable> shutdownNow() {
		expirableFutures.clear();
		return executor.shutdownNow();
	}

	@Override
	public boolean isShutdown() {
		return executor.isShutdown();
	}

	@Override
	public boolean isTerminated() {
		return executor.isTerminated();
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return executor.awaitTermination(timeout, unit);
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		return executor.submit(task);
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		return executor.submit(task, result);
	}

	@Override
	public Future<?> submit(Runnable task) {
		return executor.submit(task);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		return executor.invokeAll(tasks);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException {
		return executor.invokeAll(tasks, timeout, unit);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		return executor.invokeAny(tasks);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		return executor.invokeAny(tasks, timeout, unit);
	}
}
