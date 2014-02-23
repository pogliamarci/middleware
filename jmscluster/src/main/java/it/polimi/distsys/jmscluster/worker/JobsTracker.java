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
	
	public boolean exists(int n) {
		return information.containsKey(n);
	}
	
	public void join(int n, int jobs) {
		if(!information.containsKey(n))
			information.put(n, new AtomicInteger(jobs));
	}
	
	public void leave(int n) {
		information.remove(n);
	}
	
	public void update(int n, int cur) {
		AtomicInteger ai = information.get(n);
		if(ai != null) ai.set(cur);
	}
	
	public void update(int cur) {
		ongoingJobs.set(cur);
	}
	
	public void addJob(int n) {
		AtomicInteger ai = information.get(n);
		if(ai != null) ai.getAndIncrement();
	}
	
	public void addJob() {
		ongoingJobs.getAndIncrement();
	}
	
	public void removeJob(int n) {
		AtomicInteger ai = information.get(n);
		if(ai != null) ai.decrementAndGet();
	}
	
	public void removeJob() {
		ongoingJobs.decrementAndGet();
	}
	
	public boolean canAccept() {
		int myJobs = ongoingJobs.get();
		
		for(AtomicInteger ai : information.values()) {
			int cur = ai.get();
			if (myJobs > cur) return false;
		}
		return true;
	}
	
	public int getJobs() {
		return ongoingJobs.get();
	}
	
}
