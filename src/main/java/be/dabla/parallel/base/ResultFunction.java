package be.dabla.parallel.base;

import java.util.concurrent.Future;

import com.google.common.base.Function;

class ResultFunction<TYPE> implements Function<Future<TYPE>, TYPE> {
    ResultFunction() {}
    
    public TYPE apply(Future<TYPE> input) {
        try {
            return input.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}