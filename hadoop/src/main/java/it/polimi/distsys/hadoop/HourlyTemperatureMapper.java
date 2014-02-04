package it.polimi.distsys.hadoop;

import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class HourlyTemperatureMapper extends Mapper<LongWritable, Text, HourlyTemperatureKey, DoubleWritable> {

	private long firstTimestamp = 0;
	private static final long WINDOW_LENGTH = 3600;
	
	@Override
	protected void setup(Context context) throws IOException,
			InterruptedException {
		super.setup(context);
		firstTimestamp = Long.parseLong(context.getConfiguration().get("initialTimestamp"));
	}

	@Override
	protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

		String line = value.toString();
		String[] tokens = line.split(",");
		if(tokens.length != 6) // sanity check
			return;
		
		int hour = timestampToHour(Long.parseLong(tokens[1]));
		int house = Integer.parseInt(tokens[2]);
		int household = Integer.parseInt(tokens[3]);
		int plug = Integer.parseInt(tokens[4]);
		double meas = Double.parseDouble(tokens[5]);
		
		context.write(new HourlyTemperatureKey(hour, house, household, plug),
				new DoubleWritable(meas));
		
	}
	
	private int timestampToHour(long timestamp) {
		return (int) ((timestamp - firstTimestamp) % WINDOW_LENGTH);
	}
	
}
