package be.dabla.parallel.base;

import java.text.MessageFormat;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

public class Functions {
    public static <TYPE> Function<Future<TYPE>, TYPE> forFuture() {
        return new FutureFunction<TYPE>();
    }
    
    public static <TYPE, RESULT> Function<TYPE, Callable<RESULT>> forFunction(Function<? super TYPE,RESULT> function, Callback<Void> callback, MessageFormat threadNamePattern) {
        return new ParallelFunction<TYPE, RESULT>(function, callback, threadNamePattern);
    }
    
    public static <TYPE> Function<TYPE, Callable<TYPE>> forPredicate(Predicate<? super TYPE> predicate, Callback<Void> callback, MessageFormat threadNamePattern) {
        return new ParallelPredicate<TYPE>(predicate, callback, threadNamePattern);
    }
}