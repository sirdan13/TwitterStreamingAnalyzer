package consumer;

import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import utilities.Tweet;

public class TwitterConsumer {
	
	public static void main(String[] args){
		Properties props = new Properties();
	    props.put("bootstrap.servers", "localhost:9092");
	    props.put("group.id", "test");
	    props.put("enable.auto.commit", "true");
	    props.put("auto.commit.interval.ms", "1000");
	    props.put("session.timeout.ms", "30000");
	    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
	    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
	    KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
	    consumer.subscribe(Arrays.asList("twitter-test5"));
	    while (true) {
	    	
	        ConsumerRecords<String, String> records = consumer.poll(10000);
	  //      System.out.println(records.count());
	        for(ConsumerRecord<String, String> cr : records)
	        	System.out.println(cr.value());
	    }
	}
	

}
