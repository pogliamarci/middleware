package it.polimi.distsys.jmscluster.worker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class JobsTracker {
	
	private Map<Integer, AtomicInteger> information;
	private AtomicInteger ongoingJobs;
	
	public JobsTracker() {
		information = new ConcurrentHashMap<Integer, AtomicInteger>();
		ongoingJobs = new AtomicInteger(0);
	}
	
	public void join(int n) {
		information.put(n, new AtomicInteger(0));
	}
	
	public void leave(int n) {
		information.remove(n);
	}
	
	public void update(int n, int cur) {
		information.get(n).set(cur);
	}
	
	public void update(int cur) {
		ongoingJobs.set(cur);
	}
	
	public void addJob(int n) {
		information.get(n).getAndIncrement();
	}
	
	public void addJob() {
		ongoingJobs.getAndIncrement();
	}
	
	public void removeJob(int n) {
		information.get(n).decrementAndGet();
	}
	
	public void removeJob() {
		ongoingJobs.decrementAndGet();
	}
	
	public boolean canAccept() {
		int min = 0;
		for(AtomicInteger ai : information.values()) {
			int cur = ai.get();
			if (cur < min) min = cur;
		}
		return ongoingJobs.get() <= min;
	}
	
	public int getJobs() {
		return ongoingJobs.get();
	}
	
}
