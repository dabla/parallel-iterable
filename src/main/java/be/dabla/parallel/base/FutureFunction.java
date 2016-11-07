package be.dabla.parallel.base;

import java.util.concurrent.Future;

import com.google.common.base.Function;

class FutureFunction<TYPE> implements Function<Future<TYPE>, TYPE> {
    private final ExceptionHandler exceptionHandler;

	FutureFunction(ExceptionHandler exceptionHandler) {
		this.exceptionHandler = exceptionHandler;}
    
    @Override
    public TYPE apply(Future<TYPE> input) {
        try {
            return input.get();
        } catch (Exception e) {
        	exceptionHandler.handle(e);
        	return null;
        }
    }
}