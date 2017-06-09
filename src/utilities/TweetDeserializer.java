package utilities;

import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TweetDeserializer implements Deserializer<Tweet>{

	@Override
	public void close() {

		
	}

	@Override
	public void configure(Map<String, ?> arg0, boolean arg1) {

		
	}

	public Tweet deserialize(String arg0, byte[] arg1) {
	    ObjectMapper mapper = new ObjectMapper();
	    Tweet t = null;
	    try {
	      t = mapper.readValue(arg1, Tweet.class);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return t;
	  }

}
