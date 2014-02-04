package it.polimi.distsys.hadoop;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.WritableComparable;

public class HourlyTemperatureKey implements WritableComparable<HourlyTemperatureKey> {
	
	private IntWritable hour;
	private IntWritable house;
	private IntWritable household;
	private IntWritable plug;
	
	public HourlyTemperatureKey() {
		hour = new IntWritable();
		house = new IntWritable();
		household = new IntWritable();
		plug = new IntWritable();
	}
	
	public HourlyTemperatureKey(int hour, int house, int household, int plug) {
		this.hour = new IntWritable(hour);
		this.house = new IntWritable(house);
		this.household = new IntWritable(household);
		this.plug = new IntWritable(plug);
	}
	
	@Override
	public void readFields(DataInput in) throws IOException {
		hour.readFields(in);
		house.readFields(in);
		household.readFields(in);
		plug.readFields(in);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		hour.write(out);
		house.write(out);
		household.write(out);
		plug.write(out);
	}

	@Override
	public int compareTo(HourlyTemperatureKey o) {
		if(o == this)
			return 0;
		if(this.house.compareTo(o.house) > 0)
			return 1;
		if(this.house.compareTo(o.house)== 0) {
			if(this.household.compareTo(o.household) > 0)
				return 1;
			if(this.household.compareTo(o.household) == 0) {
				if(this.plug.compareTo(o.plug) > 0)
					return 1;
				if(this.plug.compareTo(o.plug) == 0) {
					return this.hour.compareTo(o.hour);
				}
			}
		}
		return -1;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hour == null) ? 0 : hour.hashCode());
		result = prime * result + ((house == null) ? 0 : house.hashCode());
		result = prime * result
				+ ((household == null) ? 0 : household.hashCode());
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
		HourlyTemperatureKey other = (HourlyTemperatureKey) obj;
		if (hour == null) {
			if (other.hour != null)
				return false;
		} else if (!hour.equals(other.hour))
			return false;
		if (house == null) {
			if (other.house != null)
				return false;
		} else if (!house.equals(other.house))
			return false;
		if (household == null) {
			if (other.household != null)
				return false;
		} else if (!household.equals(other.household))
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
		return "( " + this.hour + ", " + this.house + ", " + this.household
				+ ", " + this.plug + " )";
	}

}
