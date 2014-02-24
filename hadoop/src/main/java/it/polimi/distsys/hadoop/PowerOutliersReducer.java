package it.polimi.distsys.hadoop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.mapreduce.Reducer;

public class PowerOutliersReducer extends Reducer<PowerOutliersKey, PowerOutliersValue, PowerOutliersKey, DoubleWritable> {
	
	@Override
	protected void reduce(PowerOutliersKey key, Iterable<PowerOutliersValue> values, Context context) 
			throws IOException, InterruptedException {
		
		// collects all the measurements for each plug
		Map<Integer, List<Integer>> mp = new HashMap<Integer, List<Integer>>();
		
		// a list of all the measurements (that will be sorted)
		List<Integer> loi = new ArrayList<Integer>();
		for(PowerOutliersValue v : values)
		{
			loi.add(v.getMeasurement());
			if(mp.get(v.getPlug()) == null) {
				mp.put(v.getPlug(), new ArrayList<Integer>());
			}
			mp.get(v.getPlug()).add(v.getMeasurement());
		}

		int median = computeMedian(loi);

		int tn = 0, p = 0;
		for(Entry<Integer, List<Integer>> e : mp.entrySet())
		{
			tn++;
			int m = computeMedian(e.getValue());
			if(m > median) {
				p += 1;
			}
		}
		context.write(key, new DoubleWritable(((double) tn / (double) p)));
	}
	
	private int computeMedian(List<Integer> loi) {
		Collections.sort(loi);
		if(loi.size() == 0)
			throw new IllegalArgumentException();
		if(loi.size()%2 == 0) {
			return (loi.get(loi.size()/2) + loi.get(loi.size()/2 -1)) / 2;
		} else {
			return loi.get(loi.size()/2);
		}
	}

}
