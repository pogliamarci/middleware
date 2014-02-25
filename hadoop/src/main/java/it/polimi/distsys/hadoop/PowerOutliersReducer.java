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

public class PowerOutliersReducer
		extends
		Reducer<PowerOutliersKey, PowerOutliersValue, PowerOutliersKey, DoubleWritable> {

	@Override
	protected void reduce(PowerOutliersKey key,
			Iterable<PowerOutliersValue> values, Context context)
			throws IOException, InterruptedException {

		List<Integer> valueList = new ArrayList<Integer>();
		Map<IntPair, List<Integer>> mp = new HashMap<IntPair, List<Integer>>();
		for(PowerOutliersValue val : values) {
			int meas = val.getMeasurement();
			valueList.add(meas);
			IntPair p = new IntPair(val.getHousehold(), val.getPlug());
			if (!mp.containsKey(p)) {
				mp.put(p, new ArrayList<Integer>());
			}
			mp.get(p).add(meas);
		}
		
		int overallMedian = median(valueList);

		/*
		 * Compute the outliers
		 */
		int totalPlugs = mp.size();
		int outliers = 0;
		for (Entry<IntPair, List<Integer>> e : mp.entrySet()) {
			if (median(e.getValue()) > overallMedian) {
				outliers++;
			}
		}
		double ratio = (double) outliers / (double) totalPlugs;
		context.write(key, new DoubleWritable(ratio * 100));
	}

	private int median(List<Integer> loi) {
		assert (loi.size() > 0);
		Collections.sort(loi);
		if (loi.size() % 2 == 0) {
			return (loi.get(loi.size() / 2) + loi.get(loi.size() / 2 - 1)) / 2;
		} else {
			return loi.get(loi.size() / 2);
		}
	}

}
