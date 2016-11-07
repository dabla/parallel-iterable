package be.dabla.parallel.base;

import java.text.MessageFormat;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

public class Functions {
    public static <TYPE> Function<Future<TYPE>, TYPE> forFuture(ExceptionHandler exceptionHandler) {
        return new FutureFunction<TYPE>(exceptionHandler);
    }
    
    public static <TYPE, RESULT> Function<TYPE, Callable<RESULT>> forFunction(Function<? super TYPE,RESULT> function,
    																		  ExceptionHandler exceptionHandler,
    																		  Callback<Void> callback,
    																		  MessageFormat threadNamePattern) {
        return new ParallelFunction<TYPE, RESULT>(function, exceptionHandler, callback, threadNamePattern);
    }
    
    public static <TYPE> Function<TYPE, Callable<TYPE>> forPredicate(Predicate<? super TYPE> predicate,
    																 ExceptionHandler exceptionHandler,
    																 Callback<Void> callback,
    																 MessageFormat threadNamePattern) {
        return new ParallelPredicate<TYPE>(predicate, exceptionHandler, callback, threadNamePattern);
    }
}