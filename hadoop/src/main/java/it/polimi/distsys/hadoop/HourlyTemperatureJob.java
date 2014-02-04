package it.polimi.distsys.hadoop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class HourlyTemperatureJob extends Configured implements Tool {
	
	@Override
	public int run(String[] args) throws URISyntaxException {
		
		if(args.length != 2) {
			System.err.println("Usage: [in] [out]");
			ToolRunner.printGenericCommandUsage(System.err);
			return -1;
		}
		
		try {
			Job job = Job.getInstance(getConf(), "Hourly temperature");
			job.setJarByClass(getClass());
			FileInputFormat.addInputPath(job, new Path(args[0]));
			FileOutputFormat.setOutputPath(job, new Path(args[1]));
			
			FileSystem fs = FileSystem.get(getConf());
			InputStream input = fs.open(new Path(args[0]));
			BufferedReader br = new BufferedReader(new InputStreamReader(input));
			String initialTimestamp = br.readLine().split(",")[1].trim();
			fs.close();
			
			getConf().set("initialTimestamp", initialTimestamp);
			
			job.setMapperClass(HourlyTemperatureMapper.class);
			job.setReducerClass(HourlyTemperatureReducer.class);
			job.setOutputKeyClass(HourlyTemperatureKey.class);
			job.setOutputValueClass(DoubleWritable.class);
			
			return job.waitForCompletion(true) ? 0 : 1;
		} catch (IOException e) {
			System.err.println("Error: can't run the job: I/O error");
		} catch (ClassNotFoundException | InterruptedException e) {
			System.err.println("Error: can't run the job: ");
		}
		return -1;
	}
	
	public static void main(String[] args) throws Exception {
		System.exit(ToolRunner.run(new HourlyTemperatureJob(), args));
	}
	
}
