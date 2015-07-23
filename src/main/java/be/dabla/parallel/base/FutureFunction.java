package be.dabla.parallel.base;

import java.util.concurrent.Future;

import com.google.common.base.Function;

class FutureFunction<TYPE> implements Function<Future<TYPE>, TYPE> {
    FutureFunction() {}
    
    public TYPE apply(Future<TYPE> input) {
        try {
            return input.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}