package be.dabla.parallel.iterable;

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Lists.newArrayList;
import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;
import static java.math.BigInteger.valueOf;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import java.util.List;

import org.junit.Test;

import com.google.common.base.Function;

public class ParallelIterableTest {
    
    @Test
    public void transform() {
        List<BigInteger> actual = ParallelIterable.<BigInteger>on(newCachedThreadPool())
                                                  .from(newArrayList(ZERO, ONE))
                                                  .transform(addOne())
                                                  .toList();
        
        assertThat(actual).containsExactly(valueOf(1), valueOf(2));
    }
    
    @Test
    public void transformAndConcat() {
        List<BigInteger> actual = ParallelIterable.<BigInteger>aParallelIterable()
        										  .threadNamePattern("BigInteger-{0}")
        										  .from(newArrayList(ZERO, ONE))
                                                  .transformAndConcat(addMultipleOnes())
                                                  .toList();
        
        assertThat(actual).containsExactly(valueOf(1), valueOf(1), valueOf(2), valueOf(2));
    }
    
    @Test
    public void filter() {
        List<BigInteger> actual = ParallelIterable.from(newArrayList(ZERO, null, ONE))
                                                  .filter(notNull())
                                                  .toList();
        
        assertThat(actual).containsExactly(ZERO, ONE);
    }
    
    private Function<BigInteger, BigInteger> addOne() {
        return new Function<BigInteger, BigInteger>() {
            public BigInteger apply(BigInteger input) {
                return input.add(ONE);
            }
        };
    }
    
    private Function<BigInteger, List<BigInteger>> addMultipleOnes() {
        return new Function<BigInteger, List<BigInteger>>() {
            public List<BigInteger> apply(BigInteger input) {
                return newArrayList(input.add(ONE), input.add(ONE));
            }
        };
    }
}