package be.dabla.concurrent;

import java.util.Collection;
import java.util.Iterator;

class CancelExpiredRunnables implements Runnable {
	final Collection<ExpirableFuture<?>> expirableFutures;
	
	CancelExpiredRunnables(Collection<ExpirableFuture<?>> expirableFutures) {
		this.expirableFutures = expirableFutures;
	}
	
	@Override
	public void run() {
		for (Iterator<ExpirableFuture<?>> iterator = expirableFutures.iterator(); iterator.hasNext();) {
			ExpirableFuture<?> expirableFuture = iterator.next();
			
			if (expirableFuture.isExpired()) {
		    	expirableFuture.cancel(true);
		    }
			
			if (expirableFuture.isCancelled() ||
				expirableFuture.isDone()) {
				iterator.remove();
			}
		}
	}
}