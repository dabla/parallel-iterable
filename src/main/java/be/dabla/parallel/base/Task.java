package be.dabla.parallel.base;

import java.util.concurrent.Callable;

abstract class Task<TYPE> implements Callable<TYPE> {
    
    private final Callback<Void> callback;

    Task(Callback<Void> callback) {
        this.callback = callback;
    }
    
    @Override
    public TYPE call() throws Exception {
        try {
            return execute();
        }
        finally {
            callback.execute();
        }
    }
    
    public abstract TYPE execute() throws Exception;
}