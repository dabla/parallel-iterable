package be.dabla.parallel.base;

import static java.lang.Thread.currentThread;

import java.text.MessageFormat;
import java.util.concurrent.Callable;

abstract class Task<TYPE> implements Callable<TYPE> {
    
	private final ExceptionHandler exceptionHandler;
    private final Callback<Void> callback;
    private final MessageFormat messageFormat;

    Task(ExceptionHandler exceptionHandler, Callback<Void> callback, MessageFormat messageFormat) {
        this.exceptionHandler = exceptionHandler;
		this.callback = callback;
		this.messageFormat = messageFormat;
    }
    
    @Override
    public TYPE call() throws Exception {
    	String threadName = currentThread().getName();
    	
        try {
        	currentThread().setName(threadName());
            return execute();
        }
        catch(Exception e) {
        	exceptionHandler.handle(e);
        	return null;
        }
        finally {
            callback.execute();
            currentThread().setName(threadName);
        }
    }
    
    private String threadName() {
		return messageFormat.format(new Object[]{currentThread().getId()});
	}

	public abstract TYPE execute() throws Exception;
}