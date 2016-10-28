package be.dabla.asserters;

import java.util.concurrent.TimeoutException;

public class DefaultCondition extends Condition {

    private final Runnable runnable;
    private TimeoutException throwable = new TimeoutException();

    public DefaultCondition(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public boolean validate() {
        try {
            runnable.run();
            return true;
        } catch (Exception e) {
            throwable = new TimeoutException(e.getMessage());
            return false;
        }
    }

    @Override
    public TimeoutException exceptionToThrowAfterTimeout() {
        return throwable;
    }
}
