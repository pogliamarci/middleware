package it.polimi.distsys.hadoop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.mapreduce.Reducer;

public class HourlyTemperatureReducer extends Reducer<HourlyTemperatureKey, DoubleWritable, HourlyTemperatureKey, DoubleWritable> {
	
	@Override
	protected void reduce(HourlyTemperatureKey key, Iterable<DoubleWritable> values, Context context) throws IOException, InterruptedException {
		
		List<Double> lod = new ArrayList<Double>();
		for(DoubleWritable dblw : values) {
			lod.add(dblw.get());
		}
		Collections.sort(lod);
		
		if(lod.size() != 0)
			context.write(key, new DoubleWritable(computeMedian(lod)));
	}
	
	private double computeMedian(List<Double> lst) {
		if(lst.size() == 0)
			throw new IllegalArgumentException();
		if(lst.size()%2 == 0) {
			return (lst.get(lst.size()/2) + lst.get(lst.size()/2 -1)) / 2;
		} else {
			return lst.get(lst.size()/2);
		}
	}

}
