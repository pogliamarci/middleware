/*
 * TemperatureOutliersJob - Apache Pig version
 *
 * Middleware Technologies for Distributed Systems project, February 2014
 * Marcello Pogliani, Alessandro Riva
 */
package it.polimi.distsys.pig;

import java.io.IOException;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.Tuple;

/**
 * Computes the median for a <b>sorted</b> input bag.
 *
 * Note that the implementation is not distributed (so, make sure the input bag is not too large) and
 * that the input bag MUST be already sorted (use ORDER BY in the pig script).
 */
public class Median extends EvalFunc<Integer> {

	@Override
	public Integer exec(Tuple p_input) throws IOException {
		
		DataBag bag = (DataBag) p_input.get(0);

		long halfSize = bag.size() / 2;
		boolean isEven = (bag.size() % 2 == 0 && bag.size() != 0);
		int median = 0;
		long i = 0;
		for(Tuple cur : bag) {
			if(i == (halfSize-1) && isEven)
				median += ((Integer) cur.get(0));
			if(i == halfSize) {
				median += ((Integer) cur.get(0));
				if(isEven) median /= 2;
				return median;
			}
			++i;
		}
		return median;
	}
}
