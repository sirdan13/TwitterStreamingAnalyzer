package utilities;

import java.util.Map;

import org.apache.kafka.common.serialization.Serializer;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TweetSerializer implements Serializer<Tweet>{

	@Override
	public void close() {
		
		
	}

	@Override
	public void configure(Map arg0, boolean arg1) {
	
	}

	public byte[] serialize(String arg0, Tweet t) {
		    
		byte[] out = null;
		ObjectMapper objectMapper = new ObjectMapper();
		try {
		      out = objectMapper.writeValueAsString(t).getBytes();
		    } catch (Exception e) {
		      e.printStackTrace();
		    }
		    return out;
		  }

	



	

}
