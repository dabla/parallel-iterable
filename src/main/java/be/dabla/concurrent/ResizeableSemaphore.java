package be.dabla.concurrent;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("serial")
 

/**
 * {@link Semaphore}
 *
 * @author sptz45
 * @see <a href="https://github.com/sptz45/pool-ng">https://github.com/sptz45/pool-ng</a>
 * Date: 3/3/12
 */
public class ResizeableSemaphore extends Semaphore {
    private final AtomicInteger size;

    private ResizeableSemaphore(int permits, boolean fair, AtomicInteger size) {
        super(permits, fair);
        this.size = size;
    }
    
    public static ResizeableSemaphore resizeableSemaphore(int permits) {
        return resizeableSemaphore(permits, false);
    }
    
    public static ResizeableSemaphore resizeableSemaphore(int permits, boolean fair) {
        return new ResizeableSemaphore(permits, fair, new AtomicInteger(permits));
    }
    
    public int size() {
        return size.get();
    }
    
    public synchronized int resize(int newSize) {
        if (newSize < 0) {
            return size();
        }
        
        if (newSize > size.get()) {
            release(newSize - size.get());
            size.set(newSize);
        }
        else if (newSize < size.get()) {
            int acquired = size.get() - newSize;
            for (; acquired != 0 && !tryAcquire(acquired); acquired--) {
            }
            size.set(size.get() - acquired);
        }
        
        return size.get();
    }
}