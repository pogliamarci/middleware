package it.polimi.distsys.hadoop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.mapreduce.Reducer;

public class TemperatureOutliersReducer extends Reducer<TemperatureOutliersKey, TemperatureOutliersValue, TemperatureOutliersKey, DoubleWritable> {
	
	@Override
	protected void reduce(TemperatureOutliersKey key, Iterable<TemperatureOutliersValue> values, Context context) throws IOException, InterruptedException {
		
		// compute the "general" median
		// divide the iterator for each plug
		// and compute the median of the plug
		// TODO check if we can exploit Hadoop to do some sorting and simplify the processing in the reducer!
		
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
