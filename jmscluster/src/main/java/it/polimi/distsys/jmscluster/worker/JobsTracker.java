/*
 * JMSCluster
 *
 * Middleware Technologies for Distributed Systems project, February 2014
 * Marcello Pogliani, Alessandro Riva
 */

package it.polimi.distsys.jmscluster.worker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class keeps track of all the jobs currently assigned to the workers
 * and informs the listener(s) of this worker about whether they should or
 * should not accept any new job.
 * 
 * Due to the serial semantics of the message listener (i.e., the one in 
 * CoordinationManager) there is no need to care about concurrent access to
 * the `information` map, while there can be concurrent access to the `ongoingJobs`
 * integer and to the `information` map.
 */
public class JobsTracker {
	
	private Map<Integer, AtomicInteger> information;
	private AtomicInteger ongoingJobs;
	
	private List<ServerStatusListener> lsts;
	
	public JobsTracker() {
		lsts = new ArrayList<ServerStatusListener>();
		information = new ConcurrentHashMap<Integer, AtomicInteger>();
		ongoingJobs = new AtomicInteger(0);
	}
	
	public void addObserver(ServerStatusListener lst) {
		lsts.add(lst);
		informListeners();
	}
	
	public boolean exists(int n) {
		return information.containsKey(n);
	}
	
	public void join(int n, int jobs) {
		if(!information.containsKey(n)) {
			// System.out.println(n + " joined with " + jobs + "jobs.");
			information.put(n, new AtomicInteger(jobs));
			informListeners();
		}
	}
	
	public void leave(int n) {
		information.remove(n);
		informListeners();
	}

	public void update(int n, int cur) {
		// System.out.println(n + " has " + cur + " jobs (I have " + ongoingJobs.get() + ")");
		AtomicInteger ai = information.get(n);
		if(ai != null) {
			ai.set(cur);
		}
		informListeners();
	}
	
	public void update(int cur) {
		ongoingJobs.set(cur);
		informListeners();
	}
	
	public void addJob(int n) {
		AtomicInteger ai = information.get(n);
		if(ai != null) {
			ai.getAndIncrement();
		}
		informListeners();
	}
	
	public void addJob() {
		ongoingJobs.getAndIncrement();
		informListeners();
	}
	
	public void removeJob(int n) {
		AtomicInteger ai = information.get(n);
		if(ai != null) {
			ai.decrementAndGet();
		}
		informListeners();
	}
	
	public void removeJob() {
		ongoingJobs.decrementAndGet();
		informListeners();
	}
	
	public int getJobs() {
		return ongoingJobs.get();
	}
	
	private void informListeners() {
		boolean st = canAccept();
		for(ServerStatusListener lst : lsts) {
			lst.canAccept(st);
		}
		
	}
	
	private boolean canAccept() {
		int myJobs = ongoingJobs.get();
		
		for(AtomicInteger ai : information.values()) {
			int cur = ai.get();
			if (myJobs > cur) {
				return false;
			}
		}
		return true;
	}

}
