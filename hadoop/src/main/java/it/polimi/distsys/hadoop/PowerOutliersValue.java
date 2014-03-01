/*
 * Temperature Outliers - Map-Reduce version
 *
 * Middleware Technologies for Distributed Systems project, February 2014
 * Marcello Pogliani, Alessandro Riva
 */
package it.polimi.distsys.hadoop;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Writable;


public class PowerOutliersValue implements Writable {
	
	private IntWritable household;
	private IntWritable plug;
	private IntWritable measurement;
	
	public PowerOutliersValue() {
		household = new IntWritable();
		plug = new IntWritable();
		measurement = new IntWritable();
	}
	
	public PowerOutliersValue(int household, int plug, int measurement) {
		this.household = new IntWritable(household);
		this.plug = new IntWritable(plug);
		this.measurement = new IntWritable(measurement);
	}
	
	public int getHousehold() {
		return household.get();
	}
	
	public int getPlug() {
		return plug.get();
	}
	
	public int getMeasurement() {
		return measurement.get();
	}
	
	@Override
	public void readFields(DataInput in) throws IOException {
		plug.readFields(in);
		measurement.readFields(in);
		household.readFields(in);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		plug.write(out);
		measurement.write(out);
		household.write(out);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((household == null) ? 0 : household.hashCode());
		result = prime * result
				+ ((measurement == null) ? 0 : measurement.hashCode());
		result = prime * result + ((plug == null) ? 0 : plug.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof PowerOutliersValue))
			return false;
		PowerOutliersValue other = (PowerOutliersValue) obj;
		if (household == null) {
			if (other.household != null)
				return false;
		} else if (!household.equals(other.household))
			return false;
		if (measurement == null) {
			if (other.measurement != null)
				return false;
		} else if (!measurement.equals(other.measurement))
			return false;
		if (plug == null) {
			if (other.plug != null)
				return false;
		} else if (!plug.equals(other.plug))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PowerOutliersValue [plug=");
		builder.append(plug);
		builder.append(", measurement=");
		builder.append(measurement);
		builder.append(", household=");
		builder.append(household);
		builder.append("]");
		return builder.toString();
	}

}
