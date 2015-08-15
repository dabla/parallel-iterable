package be.dabla.parallel.iterable;

import static be.dabla.parallel.base.Functions.forFunction;
import static be.dabla.parallel.base.Functions.forPredicate;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.Executors.newFixedThreadPool;

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import be.dabla.parallel.base.Callback;
import be.dabla.parallel.base.Functions;

public class ParallelIterable<TYPE> {
    private static ExecutorService executor = newFixedThreadPool(getRuntime().availableProcessors());
    private final Iterable<TYPE> elements;
    private final CountDownLatch latch;
    private final Semaphore numberOfThreads;
    private final MessageFormat threadNamePattern;

    private ParallelIterable(Iterable<TYPE> elements, CountDownLatch latch, Semaphore numberOfThreads, MessageFormat threadNamePattern) {
        this.elements = elements;
        this.latch = latch;
        this.numberOfThreads = numberOfThreads;
		this.threadNamePattern = threadNamePattern;
    }
    
    public synchronized static <TYPE> ParallelIterableBuilder<TYPE> using(ExecutorService executor) {
        ParallelIterable.executor = executor;
        return aParallelIterable();
    }
    
    public static <TYPE> ParallelIterableBuilder<TYPE> aParallelIterable() {
        return new ParallelIterableBuilder<TYPE>();
    }
    
    public static <TYPE> ParallelIterable<TYPE> from(Iterable<TYPE> elements) {
        return ParallelIterable.<TYPE>aParallelIterable().from(elements);
    }
    
    public static class ParallelIterableBuilder<TYPE> {
        public static final int DEFAULT_NUMBER_OF_THREADS = 5;
		private static final String DEFAULT_THREAD_NAME_PATTERN = "ParallelIterable-{0}";
        private Semaphore numberOfThreads = new Semaphore(DEFAULT_NUMBER_OF_THREADS);
		private final MessageFormat threadNamePattern = new MessageFormat(DEFAULT_THREAD_NAME_PATTERN);
        
        private ParallelIterableBuilder() {}
        
        public ParallelIterableBuilder<TYPE> numberOfThreads(int numberOfThreads) {
            this.numberOfThreads = new Semaphore(numberOfThreads);
            return this;
        }
        
        public ParallelIterableBuilder<TYPE> threadNamePattern(String threadNamePattern) {
            this.threadNamePattern.applyPattern(threadNamePattern);
            return this;
        }
        
        public ParallelIterable<TYPE> from(Iterable<TYPE> elements) {
            final CountDownLatch latch = new CountDownLatch(size(elements));
            return new ParallelIterable<TYPE>(elements, latch, numberOfThreads, threadNamePattern);
        }
    }
    
    public <RESULT> ParallelIterable<RESULT> transform(Function<? super TYPE,RESULT> function) {
        try {
            Function<TYPE, Callable<RESULT>> forFunction = forFunction(function, release(), threadNamePattern);
            Iterable<Future<RESULT>> results = invokeAll(forFunction);
            return from(FluentIterable.from(results).transform(Functions.<RESULT>forFuture()));
            
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    public <RESULT,ITERABLE extends Iterable<RESULT>> ParallelIterable<RESULT> transformAndConcat(Function<? super TYPE,ITERABLE> function) {
        try {
            Function<TYPE, Callable<ITERABLE>> forFunction = forFunction(function, release(), threadNamePattern);
            Iterable<Future<ITERABLE>> results = invokeAll(forFunction);
            return from(FluentIterable.from(results).transformAndConcat(Functions.<ITERABLE>forFuture()));
            
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    public ParallelIterable<TYPE> filter(Predicate<? super TYPE> predicate) {
        try {
            Function<TYPE, Callable<TYPE>> forPredicate = forPredicate(predicate, release(), threadNamePattern);
            Iterable<Future<TYPE>> results = invokeAll(forPredicate);
            return from(FluentIterable.from(results).transform(Functions.<TYPE>forFuture()).filter(notNull()));
            
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    private <RESULT> Iterable<Future<RESULT>> invokeAll(Function<TYPE, Callable<RESULT>> function) throws InterruptedException {
        List<Future<RESULT>> results = newArrayList();
        
        for (TYPE element : elements) {
            results.add(submit(function.apply(element)));
        }
        
        latch.await();
        
        return results;
    }

    private <RESULT> Future<RESULT> submit(Callable<RESULT> callable) throws InterruptedException {
        numberOfThreads.acquire();
        return executor.submit(callable);
    }

    public List<TYPE> toList() {
        return newArrayList(elements);
    }
    
    public FluentIterable<TYPE> toFluentIterable() {
        return FluentIterable.from(elements);
    }

    Callback<Void> release() {
        return new Callback<Void>() {
            public Void execute() {
                numberOfThreads.release();
                latch.countDown();
                return null;
            }
        };
    }
}
