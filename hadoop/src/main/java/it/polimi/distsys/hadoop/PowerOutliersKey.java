package it.polimi.distsys.hadoop;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.WritableComparable;

public class PowerOutliersKey implements WritableComparable<PowerOutliersKey> {
	
	private LongWritable hour;
	private IntWritable house;
	
	public PowerOutliersKey() {
		hour = new LongWritable();
		house = new IntWritable();
	}
	
	public PowerOutliersKey(long hour, int house) {
		this.hour = new LongWritable(hour);
		this.house = new IntWritable(house);
	}
	
	@Override
	public void readFields(DataInput in) throws IOException {
		hour.readFields(in);
		house.readFields(in);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		hour.write(out);
		house.write(out);
	}

	@Override
	public int compareTo(PowerOutliersKey o) {
		if(o == this)
			return 0;
		if(this.house.compareTo(o.house) > 0)
			return 1;
		if(this.house.compareTo(o.house)== 0) {
			return this.hour.compareTo(o.hour);
		}
		return -1;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hour == null) ? 0 : hour.hashCode());
		result = prime * result + ((house == null) ? 0 : house.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof PowerOutliersKey))
			return false;
		PowerOutliersKey other = (PowerOutliersKey) obj;
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
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PowerOutliersKey [hour=");
		builder.append(hour);
		builder.append(", house=");
		builder.append(house);
		builder.append("]");
		return builder.toString();
	}

}