package be.dabla.concurrent;

import static be.dabla.concurrent.ResizeableSemaphore.resizeableSemaphore;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.Semaphore;

import org.junit.Test;

public class ResizeableSemaphoreTest {
    @Test
    public void size_equals_available_plus_acquired_permits() throws Exception {
        ResizeableSemaphore sem = resizeableSemaphore(10);
        assertThat(sem.size()).isEqualTo(10);
        sem.acquire();
        assertThat(sem.availablePermits()).isEqualTo(9);
        assertThat(sem.size()).isEqualTo(10);
    }
    
    @Test
    public void a_semaphore_always_can_grow() throws Exception {
        ResizeableSemaphore sem = resizeableSemaphore(10);
        assertThat(sem.size()).isEqualTo(10);
        sem.acquire();
        sem.resize(20);
        assertThat(sem.size()).isEqualTo(20);
    }
    
    @Test
    public void a_semaphore_can_shrink() throws Exception {
        ResizeableSemaphore sem = resizeableSemaphore(10);
        assertThat(sem.size()).isEqualTo(10);
        sem.acquire();
        sem.resize(5);
        assertThat(sem.size()).isEqualTo(5);
    }
    
    @Test
    public void the_semaphore_size_cannot_be_less_than_the_acquired_permits() throws Exception {
        ResizeableSemaphore sem = resizeableSemaphore(4);
        assertThat(sem.size()).isEqualTo(4);
        sem.acquire();
        sem.acquire();
        sem.resize(1);
        assertThat(sem.size()).isEqualTo(2);
    }
    
    @Test
    public void a_semaphore_cannot_get_shrunk_when_all_permits_are_acquired() throws Exception {
        ResizeableSemaphore sem = resizeableSemaphore(2);
        assertThat(sem.size()).isEqualTo(2);
        sem.acquire();
        sem.acquire();
        sem.resize(1);
        assertThat(sem.size()).isEqualTo(2);
    }
    
    @Test
    public void resizing_at_current_size_does_nothing() throws Exception {
        ResizeableSemaphore sem = resizeableSemaphore(4);
        assertThat(sem.size()).isEqualTo(4);
        sem.resize(4);
        assertThat(sem.size()).isEqualTo(4);
    }
    
    @Test
    public void negative_size_does_nothing() throws Exception {
        ResizeableSemaphore sem = resizeableSemaphore(4);
        sem.resize(-4);
        assertThat(sem.size()).isEqualTo(4);
    }
    
    @Test
    public void verify_expected_jdk_Semaphore_behaviour() {
        Semaphore sem = new Semaphore(10);
        assertThat(sem.availablePermits()).isEqualTo(10);
        sem.release(5);
        assertThat(sem.availablePermits()).isEqualTo(15);
    }
}
