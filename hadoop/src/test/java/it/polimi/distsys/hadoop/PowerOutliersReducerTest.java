package it.polimi.distsys.hadoop;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.io.DoubleWritable;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class PowerOutliersReducerTest {

	@Test
	public void testReduce() 
			throws IOException, InterruptedException {
		
		PowerOutliersReducer reducer = new PowerOutliersReducer();
		
		PowerOutliersKey k = new PowerOutliersKey(0, 1);
		List<PowerOutliersValue> values = Arrays.asList(
				new PowerOutliersValue(1,0,50),
				new PowerOutliersValue(1,0,60),
				new PowerOutliersValue(1,0,20),
				new PowerOutliersValue(1,0,30),
				new PowerOutliersValue(1,0,25),
				new PowerOutliersValue(1,1,20),
				new PowerOutliersValue(1,1,30),
				new PowerOutliersValue(1,1,10),
				new PowerOutliersValue(1,1,10),
				new PowerOutliersValue(2,1,20),
				new PowerOutliersValue(2,1,40),
				new PowerOutliersValue(2,1,30),
				new PowerOutliersValue(2,1,25)
				);
		
		@SuppressWarnings("unchecked")
		PowerOutliersReducer.Context ctx =
				mock(PowerOutliersReducer.Context.class);
		
		reducer.reduce(k, values, ctx);
		
		final ArgumentCaptor<DoubleWritable> captor = 
				ArgumentCaptor.forClass(DoubleWritable.class);
		verify(ctx).write(eq(k), captor.capture());
		double delta = 0.3;
		assertEquals(66.6, captor.getValue().get(), delta);
	}

}
