package be.dabla.concurrent;

import static java.lang.Thread.currentThread;

import java.util.Iterator;

public class InterruptableIterator<TYPE> implements Iterator<TYPE> {
	private final Iterator<TYPE> delegate;
	
	private InterruptableIterator(Iterator<TYPE> delegate) {
		this.delegate = delegate;
	}
	
	public static <TYPE> InterruptableIterator<TYPE> interruptableIterator(Iterator<TYPE> delegate) {
		return new InterruptableIterator<TYPE>(delegate);
	}
	
	@Override
	public boolean hasNext() {
		assertIsNotInterrupted();
		return delegate.hasNext() ;
	}

	@Override
	public TYPE next() {
		return delegate.next();
	}

	@Override
	public void remove() {
		delegate.remove();
	}
	
	private static void assertIsNotInterrupted() {
		if (currentThread().isInterrupted()) {
    		throw new RuntimeException(new InterruptedException());
    	}
	}
}