package be.dabla.concurrent;

import static be.dabla.concurrent.ResizeableSemaphore.resizeableSemaphore;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * {@link Executor} wrapper, limit max parallel threads to provided limit.
 * May be useful for task with different parallelism over the same executor.
 *
 * @author alexey
 * @see <a href="https://github.com/alx3apps/ctz-utils">https://github.com/alx3apps/ctz-utils</a>
 * Date: 7/6/12
 */
public class LimitedExecutorWrapper implements ExecutorService {
    private final ExecutorService executor = newSingleThreadExecutor();
    private final ExecutorService target;
    private final ResizeableSemaphore semaphore;

    private LimitedExecutorWrapper(ExecutorService target, ResizeableSemaphore semaphore) {
        this.target = target;
        this.semaphore = semaphore;
    }

    public static LimitedExecutorWrapper limitedExecutorWrapper(ExecutorService executor) {
        return limitedExecutorWrapper(executor, getRuntime().availableProcessors());
    }

    /**
     * @param executor executor service to wrap
     * @param limit max parallel threads available in provided executor service through this instance
     */
    public static LimitedExecutorWrapper limitedExecutorWrapper(ExecutorService executor, int maxNumberOfThreads) {
        checkNotNull(executor, "Provided executor is null");
        checkArgument(maxNumberOfThreads > 0, "MaxNumberOfThreads must be positive but was: '%s'", maxNumberOfThreads);
        return new LimitedExecutorWrapper(executor, resizeableSemaphore(maxNumberOfThreads));
    }

    public int getMaxNumberOfThreads() {
        return semaphore.size();
    }

    public void setMaxNumberOfThreads(int maxNumberOfThreads) {
        semaphore.resize(maxNumberOfThreads);
    }
    
    public int getNumberOfThreads() {
        return getMaxNumberOfThreads() - semaphore.availablePermits();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final Runnable command) {
        executor.execute(new SemaphoreRunnable(command));
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
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
        return executor.submit(new SemaphoreCallable<T>(task));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return executor.submit(new SemaphoreRunnable(task), result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return executor.submit(new SemaphoreRunnable(task));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        throw new UnsupportedOperationException();
    }

    private class SemaphoreRunnable implements Runnable {
        private final Runnable command;

        private SemaphoreRunnable(Runnable command) {
            this.command = command;
        }

        @Override
        public void run() {
            try {
                semaphore.acquire();
                target.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            command.run();
                        }
                        finally {
                            semaphore.release();
                        }
                    }
                });
            } catch(InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class SemaphoreCallable<T> implements Callable<T> {
        private final Callable<T> command;

        private SemaphoreCallable(Callable<T> command) {
            this.command = command;
        }

        @Override
        public T call() {
            try {
                semaphore.acquire();
                return target.submit(new Callable<T>() {
                    @Override
                    public T call() {
                        try {
                            return command.call();
                        }
                        catch(Exception e) {
                            throw new RuntimeException(e);
                        }
                        finally {
                            semaphore.release();
                        }
                    }
                }).get();
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}