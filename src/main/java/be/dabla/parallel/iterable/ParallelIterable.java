package be.dabla.parallel.iterable;

import static be.dabla.parallel.base.Functions.forFunction;
import static be.dabla.parallel.base.Functions.forPredicate;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.concurrent.Executors.newCachedThreadPool;

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
    private static ExecutorService executor = newCachedThreadPool();
    private final Iterable<TYPE> elements;
    private final CountDownLatch latch;
    private final Semaphore numberOfThreads;

    private ParallelIterable(Iterable<TYPE> elements, CountDownLatch latch, Semaphore numberOfThreads) {
        this.elements = elements;
        this.latch = latch;
        this.numberOfThreads = numberOfThreads;
    }
    
    public synchronized static <TYPE> ParallelIterableBuilder<TYPE> on(ExecutorService executor) {
        ParallelIterable.executor = executor;
        return new ParallelIterableBuilder<TYPE>();
    }
    
    public static <TYPE> ParallelIterable<TYPE> from(Iterable<TYPE> elements) {
        return new ParallelIterableBuilder<TYPE>().from(elements);
    }
    
    public static class ParallelIterableBuilder<TYPE> {
        public static final int DEFAULT_NUMBER_OF_THREADS = 5;
        private Semaphore numberOfThreads = new Semaphore(DEFAULT_NUMBER_OF_THREADS);
        
        private ParallelIterableBuilder() {}
        
        public ParallelIterableBuilder<TYPE> numberOfThreads(int numberOfThreads) {
            this.numberOfThreads = new Semaphore(numberOfThreads);
            return this;
        }
        
        public ParallelIterable<TYPE> from(Iterable<TYPE> elements) {
            final CountDownLatch latch = new CountDownLatch(size(elements));
            return new ParallelIterable<TYPE>(elements, latch, numberOfThreads);
        }
    }
    
    public <RESULT> ParallelIterable<RESULT> transform(Function<? super TYPE,RESULT> function) {
        try {
            List<Future<RESULT>> results = invokeAll(forFunction(function, release()));
            return from(FluentIterable.from(results).transform(Functions.<RESULT>forResult()));
            
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    public <RESULT,LIST extends List<RESULT>> ParallelIterable<RESULT> transformAndConcat(Function<? super TYPE,LIST> function) {
        try {
            List<Future<LIST>> results = invokeAll(forFunction(function, release()));
            return from(FluentIterable.from(results).transformAndConcat(Functions.<LIST>forResult()));
            
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    public ParallelIterable<TYPE> filter(Predicate<? super TYPE> predicate) {
        try {
            List<Future<TYPE>> results = invokeAll(forPredicate(predicate, release()));
            return from(FluentIterable.from(results).transform(Functions.<TYPE>forResult()).filter(notNull()));
            
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    private <RESULT> List<Future<RESULT>> invokeAll(Function<TYPE, Callable<RESULT>> function) throws InterruptedException {
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
