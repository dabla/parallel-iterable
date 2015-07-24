package be.dabla.parallel.base;

import java.text.MessageFormat;
import java.util.concurrent.Callable;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

class ParallelPredicate<TYPE> implements Function<TYPE, Callable<TYPE>> {
    private final Predicate<? super TYPE> predicate;
    private final Callback<Void> callback;
    private final MessageFormat threadNamePattern;

    ParallelPredicate(Predicate<? super TYPE> predicate, Callback<Void> callback, MessageFormat threadNamePattern) {
        this.predicate = predicate;
        this.callback = callback;
		this.threadNamePattern = threadNamePattern;
    }
    
    @Override
    public Callable<TYPE> apply(final TYPE input) {
        return new Task<TYPE>(callback, threadNamePattern) {
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