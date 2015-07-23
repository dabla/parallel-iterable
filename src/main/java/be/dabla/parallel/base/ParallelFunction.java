package be.dabla.parallel.base;

import java.util.concurrent.Callable;

import com.google.common.base.Function;

class ParallelFunction<TYPE, RESULT> implements Function<TYPE, Callable<RESULT>> {
    private final Function<? super TYPE, RESULT> function;
    private final Callback<Void> callback;

    ParallelFunction(Function<? super TYPE,RESULT> function, Callback<Void> callback) {
        this.function = function;
        this.callback = callback;
    }
    
    public Callable<RESULT> apply(final TYPE input) {
        return new Task<RESULT>(callback) {
            public RESULT execute() throws Exception {
                return function.apply(input);
            }
        };
    }
}