package it.polimi.distsys.hadoop;

import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class HourlyTemperatureMapper extends Mapper<LongWritable, Text, HourlyTemperatureKey, DoubleWritable> {

	public static final long firstTimestamp = 0; // TODO TODO TODO
	
	@Override
	protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
		
		//TODO hadoop does guarantee to break on newline, right?
		String line = value.toString();
		String[] tokens = line.split(",");
		if(tokens.length != 5) // sanity check
			return;
		
		int hour = timestampToHour(Long.parseLong(tokens[0]));
		int house = Integer.parseInt(tokens[1]);
		int household = Integer.parseInt(tokens[2]);
		int plug = Integer.parseInt(tokens[3]);
		double meas = Double.parseDouble(tokens[4]);
		
		context.write(new HourlyTemperatureKey(hour, house, household, plug),
				new DoubleWritable(meas));
		
	}
	
	private int timestampToHour(long timestamp) {
		final long WINDOW_LENGTH = 3600;
		return (int) ((timestamp - firstTimestamp) % WINDOW_LENGTH);
	}
	
}
