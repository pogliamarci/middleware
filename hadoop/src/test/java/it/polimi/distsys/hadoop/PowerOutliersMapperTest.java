package it.polimi.distsys.hadoop;

import static org.mockito.Mockito.*;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.junit.Test;

public class PowerOutliersMapperTest {

	@Test
	public void testValidRecordMap() throws IOException, InterruptedException {
		
		PowerOutliersMapper mapper = new PowerOutliersMapper();
	
		Text val1 = new Text("4191,1393321383,9,1,3,44");
		Text val2 = new Text("4126,1393324983,7,0,1,146");
		
		@SuppressWarnings("unchecked")
		PowerOutliersMapper.Context ctx = 
				mock(PowerOutliersMapper.Context.class);
		Configuration conf = mock(Configuration.class);
		
		when(ctx.getConfiguration()).thenReturn(conf);
		when(conf.get("initialTimestamp")).thenReturn("1393321383");
		
		mapper.setup(ctx);
		mapper.map(null, val1, ctx);
		verify(ctx).write(new PowerOutliersKey(0, 9), 
				new PowerOutliersValue(1,3,44));
		mapper.map(null, val2, ctx);
		verify(ctx).write(new PowerOutliersKey(1, 7), 
				new PowerOutliersValue(0,1,146));
		
	}

}
