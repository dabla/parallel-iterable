package be.dabla.parallel.base;

public interface Callback<RETURN> {
    RETURN execute();
}