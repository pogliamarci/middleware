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

public class TemperatureOutliersReducer extends Reducer<TemperatureOutliersKey, TemperatureOutliersValue, TemperatureOutliersKey, DoubleWritable> {
	
	@Override
	protected void reduce(TemperatureOutliersKey key, Iterable<TemperatureOutliersValue> values, Context context) throws IOException, InterruptedException {
		
		
		int median = computeMedian(values);
		
		Map<Integer, List<Integer>> mp = new HashMap<Integer, List<Integer>>();
		for(TemperatureOutliersValue v : values)
		{
			if(mp.get(v.getPlug()) != null) {
				mp.put(v.getPlug(), new ArrayList<Integer>());
			}
			mp.get(v.getPlug()).add(v.getMeasurement());
		}
		
		int tn = 0, p = 0;
		for(Entry<Integer, List<Integer>> e : mp.entrySet())
		{
			tn++;
			int m = computeMedian(e.getValue());
			if(m > median)
				p += 1;
		}
		context.write(key, new DoubleWritable(((double) tn / (double) p)));
	}
	
	private int computeMedian(Iterable<TemperatureOutliersValue> values) 
	{
		List<Integer> loi = new ArrayList<Integer>();
		for(TemperatureOutliersValue v : values)
		{
			loi.add(v.getMeasurement());
		}
		return computeMedian(loi);
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
