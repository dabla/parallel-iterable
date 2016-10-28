package be.dabla.parallel.iterable;

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Lists.newArrayList;
import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;
import static java.math.BigInteger.valueOf;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import com.google.common.base.Function;

@OutputTimeUnit(TimeUnit.MICROSECONDS)
@BenchmarkMode(Mode.AverageTime)
@OperationsPerInvocation
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 5, time = 1)
@State(Scope.Thread)
@Threads(1)
@Fork(2)
public class ParallelIterableTest {

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include( ".*" + ParallelIterableTest.class.getSimpleName() + ".*" )
                .build();
        new Runner( opt ).run();
    }
    
    @Test
    @GenerateMicroBenchmark
    public void transform() throws InterruptedException {
        Set<BigInteger> actual = ParallelIterable.<BigInteger>defaultExecutor(newCachedThreadPool())
                .from(newArrayList(ZERO, ZERO, ONE))
                                                 .transform(addOne())
                                                 .toSet();

        assertThat(actual).containsExactly(valueOf(1), valueOf(2));
    }
    
    @Test
    @GenerateMicroBenchmark
    public void transformAndConcat() {
        List<BigInteger> actual = ParallelIterable.<BigInteger>aParallelIterable()
        										  .threadNamePattern("BigInteger-{0}")
        										  .from(newArrayList(ZERO, ONE))
                                                  .transformAndConcat(addMultipleOnes())
                                                  .toList();
        
        assertThat(actual).containsExactly(valueOf(1), valueOf(1), valueOf(2), valueOf(2));
    }

    @Test
    @GenerateMicroBenchmark
    public void filter() {
        List<BigInteger> actual = ParallelIterable.<BigInteger>aParallelIterable()
                .using(newCachedThreadPool())
                .from(newArrayList(ZERO, null, ONE))
                .filter(notNull())
                .toList();

        assertThat(actual).containsExactly(ZERO, ONE);
    }

    @Test
    @GenerateMicroBenchmark
    public void transformTestNullFiltering() {
        ArrayList<BigInteger> elements = newArrayList(ZERO, ONE);
        List<BigInteger> actual =  ParallelIterable.<BigInteger>aParallelIterable()
                .from(elements).transformAndConcat(transformAndOccasionallyReturnNull())
                .toList();

        assertThat(actual).containsExactly(valueOf(1));
    }

    public Function<BigInteger, List<BigInteger>> transformAndOccasionallyReturnNull() {
        return new Function<BigInteger, List<BigInteger>>() {

            @Override
            public List<BigInteger> apply(BigInteger input) {
                return Arrays.asList(input.compareTo(ONE) == 0 ? null : input.add(ONE));
            }

        };
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
