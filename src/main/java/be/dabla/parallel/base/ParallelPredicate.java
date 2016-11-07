package be.dabla.parallel.base;

import java.text.MessageFormat;
import java.util.concurrent.Callable;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

class ParallelPredicate<TYPE> implements Function<TYPE, Callable<TYPE>> {
    private final Predicate<? super TYPE> predicate;
    private final ExceptionHandler exceptionHandler;
    private final Callback<Void> callback;
    private final MessageFormat threadNamePattern;

    ParallelPredicate(Predicate<? super TYPE> predicate,
    				  ExceptionHandler exceptionHandle,
    				  Callback<Void> callback,
    				  MessageFormat threadNamePattern) {
        this.predicate = predicate;
		exceptionHandler = exceptionHandle;
        this.callback = callback;
		this.threadNamePattern = threadNamePattern;
    }
    
    @Override
    public Callable<TYPE> apply(final TYPE input) {
        return new Task<TYPE>(exceptionHandler, callback, threadNamePattern) {
            @Override
            public TYPE execute() throws Exception {
                if (predicate.apply(input)) {
                    return input;
                }
                
                return null;
            }
        };
    }
}