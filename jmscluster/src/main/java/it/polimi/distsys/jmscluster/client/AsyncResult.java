package it.polimi.distsys.jmscluster.client;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AsyncResult implements Future<Serializable> {

	private final String corrId;
	private final ReplyProcesser listener;
	private boolean cancelled = false;
	
	public AsyncResult(String corrId, ReplyProcesser listener)
	{
		this.corrId = corrId;
		this.listener = listener;
	}
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		cancelled = true;
		listener.markAsCancelled(corrId);
		return cancelled;
	}

	@Override
	public Serializable get() 
			throws InterruptedException, ExecutionException {
		Serializable obj = listener.get(corrId);
		if(obj instanceof Throwable) {
			throw new ExecutionException((Throwable) obj);
		}
		return obj;
	}

	@Override
	public Serializable get(long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		Serializable obj = listener.get(corrId, unit.toMillis(timeout));
		if(obj instanceof Throwable) {
			throw new ExecutionException((Throwable) obj);
		}
		return obj;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public boolean isDone() {
		return listener.isReady(corrId);
	}

}
