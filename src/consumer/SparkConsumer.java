package consumer;

import java.awt.Color;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.ColorUIResource;
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
	static String topic = "";
	static Map<String, Integer> topics;
	static JavaPairDStream<String, String> messages;
	static JavaPairDStream<String, Integer> lines;
	
	
	 
     public static void main(String[] args) throws FileNotFoundException, InterruptedException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		
    	 for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
 	        if ("Windows".equals(info.getName())) {
 	            UIManager.setLookAndFeel(info.getClassName());
 	            break;
 	        }
 	    }
    	 
    	chooseTopic();
		while(topic==null){
			if(noTopicChosen()==0){
				if(chooseTopic()!=null)
					break;
			}
			else
				System.exit(-1);
		}
			
		loadProperties();
		init();
		analyzeTopic(topic);

		
	}
	
	private static void init() {
		conf = new SparkConf().setAppName(appName).setMaster(master);
		jssc = new JavaStreamingContext(conf, new Duration(duration));
		topics = new HashMap<String, Integer>();
		topics.put(topic, Integer.parseInt(threads));
		Logger.getLogger("org").setLevel(Level.ERROR);
		Logger.getLogger("akka").setLevel(Level.ERROR);
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
	
	private static String chooseTopic(){
		 String [] options = {"hashtags", "mentions", "original-text", "processed-text"};
		 UIManager.put("OptionPane.background",new ColorUIResource(255,255,204));
		 UIManager.put("Panel.background",new ColorUIResource(255,255,204));
		 UIManager.put("OptionPane.messageFont", "Verdana");
		 UIManager.put("OptionPane.messageForeground", Color.green);
		 Dimension size = UIManager.getDimension("OptionPane.minimumSize");
		 size.width = 350;
		 size.height= 220;
		 UIManager.put("OptionPane.minimumSize", size);
		 JLabel label = new JLabel("Scegli un topic:");
		 label.setFont(new Font("Calibri", Font.BOLD, 20));
		 label.setHorizontalAlignment(SwingConstants.LEFT);
		 label.setVerticalAlignment(SwingConstants.CENTER);
		 topic = (String) JOptionPane.showInputDialog(null, label, "Topic", 3, icon, options, label);
		 return topic;
	}
	
	private static int noTopicChosen() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException{
		JLabel label2 = new JLabel("<html>Nessun topic scelto.<br>Riprovare?</html>");
		label2.setFont(new Font("Calibri", Font.BOLD, 20));
		label2.setHorizontalAlignment(SwingConstants.LEFT);
		label2.setVerticalAlignment(SwingConstants.CENTER);
		String [] errorOptions = {"Riprova", "Esci"};
		return JOptionPane.showOptionDialog(null, label2, "Topic", 0, 0, icon, errorOptions, "Riprova");
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
