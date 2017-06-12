package analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;

import scala.Tuple2;

public class ConsumerAnalytics {
	
	static Icon icon = new ImageIcon("config/icon.png");
	static SparkConf conf;
	static JavaStreamingContext jssc;
	static String master = "";
	static String appName = "";
	static String zookeeper_server = "";
	static String kafka_consumer_group = "";
	static String threads = "";
	static long duration;
	static String topic = "";
	static Map<String, Integer> topics;
	static JavaPairDStream<String, String> messages;
	static JavaPairDStream<String, Integer> lines;
	
	
	public ConsumerAnalytics(JavaStreamingContext jssc, String zookeeper_server, String kafka_consumer_group, Map<String, Integer> topics){
		ConsumerAnalytics.jssc=jssc;
		ConsumerAnalytics.zookeeper_server=zookeeper_server;
		ConsumerAnalytics.kafka_consumer_group=kafka_consumer_group;
		ConsumerAnalytics.topics=topics;
	}
	
	public static void loadSentimentDictionaries() {
		
	}
	
	public void analyzeTopic(String topic) throws InterruptedException{
		if(topic=="hashtags")
			analyzeHashtags();
		if(topic=="mentions")
			analyzeMentions();
		if(topic=="original-text")
			analyzeOriginalText();
		if(topic=="processed-text")
			analyzeProcessedText();
	}
	
	private static void analyzeProcessedText() throws InterruptedException {
		messages =  KafkaUtils.createStream(jssc, zookeeper_server, kafka_consumer_group, topics);
		JavaPairDStream<String, Integer> words = messages.flatMap(wordFunc).mapToPair((x)->(new Tuple2<String, Integer>(x, 1))).reduceByKey(sumFunc);
		JavaPairDStream<Integer,String> sortedStream = words.mapToPair(x->x.swap()).transformToPair(sortFunc);
		sortedStream.print();
		jssc.start();
		jssc.awaitTermination();
	}

	private static void analyzeOriginalText() throws InterruptedException {
		messages =  KafkaUtils.createStream(jssc, zookeeper_server, kafka_consumer_group, topics);
		JavaPairDStream<String, Integer> words = messages.flatMap(wordFunc).mapToPair((x)->(new Tuple2<String, Integer>(x, 1))).reduceByKey(sumFunc);
		JavaPairDStream<Integer,String> sortedStream = words.mapToPair(x->x.swap()).transformToPair(sortFunc);
		sortedStream.print();
		jssc.start();
		jssc.awaitTermination();
		
	}

	private static void analyzeMentions() throws InterruptedException {
		messages =  KafkaUtils.createStream(jssc, zookeeper_server, kafka_consumer_group, topics);
		lines = messages.mapToPair((x)->(new Tuple2<String, Integer>(x._2, 1))).reduceByKey(sumFunc);
		JavaPairDStream<Integer,String> sortedStream = lines.mapToPair(x->x.swap()).transformToPair(sortFunc);
		sortedStream.print();
		jssc.start();
		jssc.awaitTermination();
		
	}

	private static void analyzeHashtags() throws InterruptedException {
		messages =  KafkaUtils.createStream(jssc, zookeeper_server, kafka_consumer_group, topics);
		lines = messages.mapToPair((x)->(new Tuple2<String, Integer>(x._2, 1))).reduceByKey(sumFunc);
		JavaPairDStream<Integer,String> sortedStream = lines.mapToPair(x->x.swap()).transformToPair(sortFunc);
		sortedStream.print();
		jssc.start();
		jssc.awaitTermination();
	}
	
	static Function2<Integer, Integer, Integer> sumFunc = new Function2<Integer, Integer, Integer>() {

		private static final long serialVersionUID = 1L;

		@Override public Integer call(Integer i1, Integer i2) throws Exception {
	  	    return i1 + i2;
	  	  }
	  	};
	  	
	static Function<JavaPairRDD<Integer,String>, JavaPairRDD<Integer, String>> sortFunc = new Function<JavaPairRDD<Integer,String>, JavaPairRDD<Integer,String>>() {
        
		private static final long serialVersionUID = 1L;

		@Override
        public JavaPairRDD<Integer,String> call(JavaPairRDD<Integer,String> rdd) throws Exception {
                   return rdd.sortByKey(false);
                 }
             };
           
     static FlatMapFunction<Tuple2<String, String>, String> wordFunc = new FlatMapFunction<Tuple2<String, String>, String>(){
    	 private static final long serialVersionUID = 1L;

 		@Override
         public Iterator<String> call(Tuple2<String,String> x) throws Exception {
                    List<String> output = new ArrayList<String>();
                    if(x._2.length()==0)
                    	return output.iterator();
                    for(String w : x._2().split(" "))
                    	output.add(w);
                    return output.iterator();
                    }

		
        };
	

}
