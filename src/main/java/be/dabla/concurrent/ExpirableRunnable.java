package be.dabla.concurrent;

import static java.lang.System.nanoTime;
import static java.lang.Thread.currentThread;

import java.util.concurrent.TimeUnit;

class ExpirableRunnable implements Runnable {
	private final Runnable runnable;
	private final long timeout;
	private final TimeUnit unit;
	private volatile Long time;
	private volatile Thread thread;
	
	ExpirableRunnable(Runnable runnable, long timeout, TimeUnit unit) {
		this.runnable = runnable;
		this.timeout = timeout;
		this.unit = unit;
	}
	
	@Override
	public void run() {
		thread = currentThread();
		time = nanoTime();
		runnable.run();
	}
	
	public String getThreadName() {
		return thread.getName();
	}
	
	boolean isRunning() {
		return time != null;
	}
	
	public boolean isExpired() {
		if (isRunning()) {
			return (time + unit.toNanos(timeout)) < nanoTime();
		}
		
	    return false;
	}
}