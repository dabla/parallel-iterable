package be.dabla.parallel.base;

import java.util.concurrent.Callable;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

class ParallelPredicate<TYPE> implements Function<TYPE, Callable<TYPE>> {
    private final Predicate<? super TYPE> predicate;
    private final Callback<Void> callback;

    ParallelPredicate(Predicate<? super TYPE> predicate, Callback<Void> callback) {
        this.predicate = predicate;
        this.callback = callback;
    }
    
    @Override
    public Callable<TYPE> apply(final TYPE input) {
        return new Task<TYPE>(callback) {
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