/*
 * Temperature Outliers - Apache Pig version
 *
 * Middleware Technologies for Distributed Systems project, February 2014
 * Marcello Pogliani, Alessandro Riva
 */
package it.polimi.distsys.pig;

import java.io.IOException;
import java.util.Iterator;

import org.apache.pig.Algebraic;
import org.apache.pig.EvalFunc;
import org.apache.pig.PigException;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;

/**
 * Input: a tuple (Reference, PartialValue)
 * Output: The percentage of elements such that PartialValue > Reference.
 */
public class OutliersPercentage extends EvalFunc<Double> implements Algebraic {
	
    private static TupleFactory mTupleFactory = TupleFactory.getInstance();
	
    public Double exec(Tuple input) throws IOException {
    	long cnt = 0;
    	long tot = 0;
		DataBag local = (DataBag) input.get(0);
		DataBag global = (DataBag) input.get(1);
		if(local == null || global == null)
			return null;
		assert(local.size() == global.size());
		Iterator<Tuple> localItr = local.iterator();
		Iterator<Tuple> globalItr = global.iterator();
		while(localItr.hasNext()) {
			Tuple localTuple = localItr.next();
			Tuple globalTuple = globalItr.next();
			if(localTuple != null && globalTuple != null) {
				tot++;
				if((Integer) localTuple.get(0) > (Integer) globalTuple.get(0)) {
					cnt++;
				}
			}
		}
    	return (double) cnt / (double) tot;
    	
    }
    public String getInitial() {
    	return Initial.class.getName();
    }
    
    public String getIntermed() {
    	return Intermediate.class.getName();
    }
    
    public String getFinal() {
    	return Final.class.getName();
    }
    
    static public class Initial extends EvalFunc<Tuple> {

        @Override
        public Tuple exec(Tuple input) throws IOException {
            Tuple t = mTupleFactory.newTuple(2);
    		DataBag global = (DataBag) input.get(0);
    		DataBag local = (DataBag) input.get(1);
    		if(local == null || global == null)
    			return null;
    		assert(local.size() == global.size());
    		Iterator<Tuple> localItr = local.iterator();
    		Iterator<Tuple> globalItr = global.iterator();
            if (localItr.hasNext()) {
                Tuple localTuple = (Tuple)localItr.next();
                Tuple globalTuple = (Tuple)globalItr.next();
                if(localTuple != null && globalTuple != null) {
                	t.set(0, Long.valueOf(1));
    				if((Integer) localTuple.get(0) > (Integer) globalTuple.get(0)) {
    					t.set(1, Long.valueOf(1));
    				} else {
    					t.set(1, Long.valueOf(0));
    				}
    			}
            }
            return t;
        }
    }

    static public class Intermediate extends EvalFunc<Tuple> {
        @Override
        public Tuple exec(Tuple input) throws IOException {
            try {
                DataBag b = (DataBag)input.get(0);
                return combine(b);
            } catch (ExecException ee) {
                throw ee;
            } catch (Exception e) {
                int errCode = 2106;
                String msg = "Error while computing outliers percentage in " + this.getClass().getSimpleName();
                throw new ExecException(msg, errCode, PigException.BUG, e);
            }
        }
    }
    
    static public class Final extends EvalFunc<Double> {
        @Override
        public Double exec(Tuple input) throws IOException {
            try {
                DataBag b = (DataBag)input.get(0);
                Tuple combined = combine(b);

                return ((Long)combined.get(1)) * 100.0 / ((Long)combined.get(0));
            } catch (ExecException ee) {
                throw ee;
            } catch (Exception e) {
                int errCode = 2106;
                String msg = "Error while computing average in " + this.getClass().getSimpleName();
                throw new ExecException(msg, errCode, PigException.BUG, e);
            }
        }
    }
    
    static protected Tuple combine(DataBag values) throws ExecException {
        long sum = 0;
        long sat = 0;
        Tuple output = mTupleFactory.newTuple(2);
        for (Iterator<Tuple> it = values.iterator(); it.hasNext();) {
            Tuple t = it.next();
            sum += (Long)t.get(0);
            sat += (Long)t.get(1);
        }
        output.set(0, Long.valueOf(sum));
        output.set(1, Long.valueOf(sat));
        return output;
    }
   
}
