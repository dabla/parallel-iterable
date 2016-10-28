package be.dabla.concurrent;

import static be.dabla.concurrent.InterruptableIterator.interruptableIterator;

import java.util.Iterator;

public class InterruptableIterable<TYPE> implements Iterable<TYPE> {
	private final Iterator<TYPE> delegate;
	
	private InterruptableIterable(Iterator<TYPE> delegate) {
		this.delegate = delegate;
	}
	
	public static <TYPE> InterruptableIterable<TYPE> interruptableIterable(Iterable<TYPE> iterable) {
		return new InterruptableIterable<TYPE>(interruptableIterator(iterable.iterator()));
	}

	@Override
	public Iterator<TYPE> iterator() {
		return delegate;
	}
}