# Parallel-Iterable
## Transform or filter collections concurrently
Just use the ParallelIterable like you would use the FluentIterable in Guava.
The transformation or filtering of each element will be done concurrently behind the scenes.

## Code sample
```java
package be.dabla.parallel.iterable;

import static com.google.common.collect.Lists.newArrayList;
import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;

import java.math.BigInteger;
import java.util.List;

import com.google.common.base.Function;

public class Sample {
    
    public List<BigInteger> incrementAllByOne() {
        return ParallelIterable.from(newArrayList(ZERO, ONE))
                        	   .transform(addOne())
                        	   .toList();
    }
    
    private Function<BigInteger, BigInteger> addOne() {
        return new Function<BigInteger, BigInteger>() {
            public BigInteger apply(BigInteger input) {
                return input.add(ONE);
            }
        };
    }
}
```

## How to use in your project

Example for Maven:
```xml
<dependency>
    <groupId>be.dabla</groupId>
    <artifactId>parallel-iterable</artifactId>
    <version>1.1</version>
</dependency>
```