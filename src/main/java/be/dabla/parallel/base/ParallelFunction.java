package be.dabla.parallel.base;

import java.text.MessageFormat;
import java.util.concurrent.Callable;

import com.google.common.base.Function;

class ParallelFunction<TYPE, RESULT> implements Function<TYPE, Callable<RESULT>> {
    private final Function<? super TYPE, RESULT> function;
    private final Callback<Void> callback;
    private final MessageFormat threadNamePattern;

    ParallelFunction(Function<? super TYPE,RESULT> function, Callback<Void> callback, MessageFormat threadNamePattern) {
        this.function = function;
        this.callback = callback;
		this.threadNamePattern = threadNamePattern;
    }
    
    @Override
    public Callable<RESULT> apply(final TYPE input) {
        return new Task<RESULT>(callback, threadNamePattern) {
            @Override
            public RESULT execute() throws Exception {
                return function.apply(input);
            }
        };
    }
}