package it.polimi.distsys.hadoop;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Writable;


public class TemperatureOutliersValue implements Writable {
	
	private IntWritable plug;
	private IntWritable measurement;
	
	public TemperatureOutliersValue() {
		plug = new IntWritable();
		measurement = new IntWritable();
	}
	
	public TemperatureOutliersValue(int plug, int measurement) {
		this.plug = new IntWritable(plug);
		this.measurement = new IntWritable(measurement);
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
	}

	@Override
	public void write(DataOutput out) throws IOException {
		plug.write(out);
		measurement.write(out);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		if (getClass() != obj.getClass())
			return false;
		TemperatureOutliersValue other = (TemperatureOutliersValue) obj;
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
		return "TemperatureOutliersValue [plug=" + plug + ", measurement="
				+ measurement + "]";
	}
	
}
