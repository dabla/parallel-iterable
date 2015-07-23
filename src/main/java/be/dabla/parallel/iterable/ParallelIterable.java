package be.dabla.parallel.iterable;

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
            List<Future<RESULT>> results = invokeAll(toTask(function));
            return from(FluentIterable.from(results).transform(ParallelIterable.<RESULT>toResult()));
            
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    public <RESULT,LIST extends List<RESULT>> ParallelIterable<RESULT> transformAndConcat(Function<? super TYPE,LIST> function) {
        try {
            List<Future<LIST>> results = invokeAll(toTask(function));
            return from(FluentIterable.from(results).transformAndConcat(ParallelIterable.<LIST>toResult()));
            
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    public ParallelIterable<TYPE> filter(Predicate<? super TYPE> predicate) {
        try {
            List<Future<TYPE>> results = invokeAll(toTask(predicate));
            return from(FluentIterable.from(results).transform(ParallelIterable.<TYPE>toResult()).filter(notNull()));
            
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    private <RESULT> List<Future<RESULT>> invokeAll(Function<TYPE, Callable<RESULT>> function) throws InterruptedException {
        List<Future<RESULT>> results = newArrayList();
        
        for (TYPE element : elements) {
            numberOfThreads.acquire();
            results.add(executor.submit(function.apply(element)));
        }
        
        latch.await();
        
        return results;
    }

    public List<TYPE> toList() {
        return newArrayList(elements);
    }
    
    public FluentIterable<TYPE> toFluentIterable() {
        return FluentIterable.from(elements);
    }

    private static <RESULT> Function<Future<RESULT>, RESULT> toResult() {
        return new Function<Future<RESULT>, RESULT>() {
            public RESULT apply(Future<RESULT> input) {
                try {
                    return input.get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    private <RESULT> Function<TYPE, Callable<RESULT>> toTask(final Function<? super TYPE,RESULT> function) {
        return new Function<TYPE, Callable<RESULT>>() {
            public Callable<RESULT> apply(final TYPE input) {
                return new Task<RESULT>() {
                    public RESULT execute() throws Exception {
                        return function.apply(input);
                    }
                };
            }
        };
    }
    
    private Function<TYPE, Callable<TYPE>> toTask(final Predicate<? super TYPE> predicate) {
        return new Function<TYPE, Callable<TYPE>>() {
            public Callable<TYPE> apply(final TYPE input) {
                return new Task<TYPE>() {
                    public TYPE execute() throws Exception {
                        if (predicate.apply(input)) {
                            return input;
                        }
                        
                        return null;
                    }
                };
            }
        };
    }
    
    private abstract class Task<RESULT> implements Callable<RESULT> {
        public RESULT call() throws Exception {
            try {
                return execute();
            }
            finally {
                numberOfThreads.release();
                latch.countDown();
            }
        }
        
        public abstract RESULT execute() throws Exception;
    }
}
