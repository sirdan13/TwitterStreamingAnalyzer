package consumer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Scanner;
import java.util.HashMap;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;

import scala.Tuple2;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
public class SparkConsumer {
	
	static Icon icon = new ImageIcon("config/icon.png");
	static SparkConf conf;
	static JavaStreamingContext jssc;
	static String master = "";
	static String appName = "";
	static String zookeeper_server = "";
	static String kafka_consumer_group = "";
	static String threads = "";
	static long duration;
	static String topic;
	static Map<String, Integer> topics;
	static JavaPairDStream<String, String> messages;
	static JavaPairDStream<String, Integer> lines;
	
	static Function2<Integer, Integer, Integer> sumFunc = new Function2<Integer, Integer, Integer>() {

		private static final long serialVersionUID = 1L;

		@Override public Integer call(Integer i1, Integer i2) throws Exception {
	  	    return i1 + i2;
	  	  }
	  	};
	  	
	 
	
	
	public SparkConsumer(SparkConf conf, JavaStreamingContext jssc){
		
	}

	public static void main(String[] args) throws FileNotFoundException, InterruptedException {
		
		String [] options = {"hashtags", "mentions", "original-text", "processed-text"};
		topic = (String) JOptionPane.showInputDialog(null, "Scegli un valore dalla lista", "Topic", 3, icon, options, options[0]);
		if(topic==null){
			JOptionPane.showMessageDialog(null, "Nessun topic selezionato", "Topic", 0, icon);
			System.exit(-1);
		}
			
		loadProperties();
		init();
		analyzeTopic(topic);

		/*
		 * Da qui ramificare l'esecuzione. A seconda del topic scelto, verrà chiamato il corrispondente metodo di analisi.
		 */
		
	}
	
	private static void init() {
		conf = new SparkConf().setAppName(appName).setMaster(master);
		jssc = new JavaStreamingContext(conf, new Duration(duration));
		Logger.getLogger("org").setLevel(Level.ERROR);
		Logger.getLogger("akka").setLevel(Level.ERROR);
		topics = new HashMap<String, Integer>();
		topics.put(topic, 0);
	}

	private static void loadProperties() throws FileNotFoundException{
		Scanner sc = new Scanner(new File("config/spark_conf.txt"));
		int count = 0;
		while(sc.hasNextLine()){
			if(count==0)
				appName = sc.nextLine().split("=")[1];
			if(count==1)
				master = sc.nextLine().split("=")[1];
			if(count==2)
				zookeeper_server = sc.nextLine().split("=")[1];
			if(count==3)
				kafka_consumer_group = sc.nextLine().split("=")[1];
			if(count==4)
				threads = sc.nextLine().split("=")[1];
			if(count==5)
				duration = Long.parseLong(sc.nextLine().split("=")[1]);
			count++;
			}
		sc.close();

	}
	
	
	private static void analyzeTopic(String topic) throws InterruptedException{
		if(topic=="hashtags")
			analyzeHashtags();
		if(topic=="mentions")
			analyzeMentions();
		if(topic=="original-text")
			analyzeOriginalText();
		if(topic=="processed-text")
			analyzeProcessedText();
	}

	private static void analyzeProcessedText() {
		messages =  KafkaUtils.createStream(jssc, zookeeper_server, kafka_consumer_group, topics);
	}

	private static void analyzeOriginalText() {
		messages =  KafkaUtils.createStream(jssc, zookeeper_server, kafka_consumer_group, topics);
		
	}

	private static void analyzeMentions() {
		messages =  KafkaUtils.createStream(jssc, zookeeper_server, kafka_consumer_group, topics);
		
	}

	private static void analyzeHashtags() throws InterruptedException {
		JavaPairDStream<String, String> messages2 =  KafkaUtils.createStream(jssc, zookeeper_server, kafka_consumer_group, topics);
		JavaPairDStream<String, Integer> lines2 = messages2.mapToPair((x)->(new Tuple2<String, Integer>(x._1, 1)));
		lines2.reduceByKey(sumFunc);
		lines2.print();
		jssc.start();
		jssc.awaitTermination();
	}

}
