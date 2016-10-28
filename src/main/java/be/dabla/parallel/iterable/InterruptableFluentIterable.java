package be.dabla.parallel.iterable;

import static be.dabla.concurrent.InterruptableIterable.interruptableIterable;

import com.google.common.collect.FluentIterable;

public class InterruptableFluentIterable {
	private InterruptableFluentIterable() {}
	
	public static <TYPE> FluentIterable<TYPE> from(Iterable<TYPE> elements) {
        return FluentIterable.<TYPE>from(interruptableIterable(elements));
    }
}
