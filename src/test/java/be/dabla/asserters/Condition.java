package be.dabla.asserters;

import java.util.concurrent.TimeoutException;

public abstract class Condition {
    public abstract boolean validate();

    public TimeoutException exceptionToThrowAfterTimeout() {
        return new TimeoutException();
    }
}
