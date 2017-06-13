package analysis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;

import com.vdurmont.emoji.EmojiParser;

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
	
	
	public void analyzeTopic(String topic) throws InterruptedException{
		if(topic=="hashtags")
			analyzeHashtags();
		if(topic=="mentions")
			analyzeMentions();
		if(topic=="original-text")
			analyzeOriginalText();
		if(topic=="processed-text")
			analyzeProcessedText();
		if(topic=="sentiment")
			analyzeSentiment();
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
	
	private static void analyzeSentiment() throws InterruptedException{
		messages =  KafkaUtils.createStream(jssc, zookeeper_server, kafka_consumer_group, topics);
	//	setSentiment();
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
                    	output.add("ERR");
                    for(String w : x._2().split(" "))
                    	output.add(w);
                    return output.iterator();
                    }

		
        };
        
        
        static Function<Tuple2<String, String>, Tuple2<String, Integer>> sentimentFunc = new Function<Tuple2<String, String>, Tuple2<String, Integer>>(){
        	private static final long serialVersionUID = 1L;


			@Override
			public Tuple2<String, Integer> call(Tuple2<String, String> x) throws Exception {
				boolean like = false, sad = false, angry = false, hilarious = false, neutral = false;
				boolean [] sentiments = {like, angry, sad, hilarious, neutral};
				sentiments = checkEmojis(x, sentiments);
				if(checkSentiment(sentiments))
					return setSentiment(sentiments);
				sentiments = checkText(x, sentiments);
				return setSentiment(sentiments);
			}
			
			//Verifica se un sentiment è stato trovato
			private boolean checkSentiment(boolean [] sentiments){
				int count = 0;
				for(boolean b : sentiments){
					if(!b){
						count++;
					}
				}
				//Se count=5, il sentiment non è ancora stato stabilito, poiché sono tutti false
				if(count==5)
					return false;
				//Restituisco true se ho trovato almeno un sentiment
				if(count<5)
					return true;
				return false;
					
			}

			//Stabilisce definitivamente di quale sentiment si tratta
			private Tuple2<String, Integer> setSentiment(boolean [] sentiments) {
				//Se sono stati individuati 3 o più sentiment differenti, classificheremo il testo come neutrale
				if(sentiments[4])
					return new Tuple2<String, Integer>("neutral", 1);
				
				//Altrimenti verifichiamo se sia stato dichiarato un solo sentiment
				if(sentiments[0] && !sentiments[1] && !sentiments[2] && !sentiments[3])
					return new Tuple2<String, Integer>("like", 1);
				if(!sentiments[0] && sentiments[1] && !sentiments[2] && !sentiments[3])
					return new Tuple2<String, Integer>("angry", 1);
				if(!sentiments[0] && !sentiments[1] && sentiments[2] && !sentiments[3])
					return new Tuple2<String, Integer>("sad", 1);
				if(!sentiments[0] && !sentiments[1] && !sentiments[2] && sentiments[3])
					return new Tuple2<String, Integer>("hilarious", 1);
				
				//Risolvo eventuali ambiguità (2 sentiment individuati nello stesso testo)
				if(sentiments[0] && sentiments[1])
					return new Tuple2<String, Integer>("angry", 1);
				if(sentiments[0] && sentiments[3])
					return new Tuple2<String, Integer>("hilarious", 1);
				if(sentiments[0] && sentiments[2])
					return new Tuple2<String, Integer>("sad", 1);
				if(sentiments[2] && sentiments[3])
					return new Tuple2<String, Integer>("sad", 1);
				if(sentiments[2] && sentiments[1])
					return new Tuple2<String, Integer>("angry", 1);
				if(sentiments[1] && sentiments[3])
					return new Tuple2<String, Integer>("hilarious", 1);
				
				return null;
			}


		private boolean[] checkEmojis(Tuple2<String, String> x, boolean [] sentiments) {
			String text = EmojiParser.parseToAliases(x._2);
			int count = 0;
			for(String w : text.split(" ")){
				if(Analytics.getEmojiLikeSet().contains(w) && !sentiments[0]){
					sentiments[0]=true;
					count++;
				}
					
				if(Analytics.getEmojiAngrySet().contains(w) && !sentiments[1]){
					sentiments[1]=true;
					count++;
				}
					
				if(Analytics.getEmojiSadSet().contains(w) && !sentiments[2]){
					sentiments[2]=true;
					count++;
				}
					
				if(Analytics.getEmojiHilariousSet().contains(w) && !sentiments[3]){
					sentiments[3]=true;
					count++;
				}
					
			}
			//Se il conteggio è maggiore di 3, avremo un sentiment neutrale
			if(count>=3)
				sentiments[4]=true;
			
			return sentiments;
			
			
		}
        
        
        
       private boolean[] checkText(Tuple2<String, String> x, boolean[] sentiments){
 
        	int count = 0;
			//Verifico lingua del tweet
			if(x._1().equals("it")){
				for(String w : x._2.split(" ")){
					if(Analytics.getLikeITList().contains(w) && !sentiments[0]){
						sentiments[0]=true;
						count++;
					}
						
					if(Analytics.getAngryITList().contains(w) && !sentiments[1]){
						sentiments[1]=true;
						count++;
					}
						
					if(Analytics.getSadITList().contains(w) && !sentiments[2]){
						sentiments[2]=true;
						count++;
					}
						
					if(Analytics.getHilariousITList().contains(w) && !sentiments[3]){
						sentiments[3]=true;
						count++;
					}
						
				}
			}
			if(x._1().equals("en")){
				for(String w : x._2.split(" ")){
					if(Analytics.getLikeENList().contains(w) && !sentiments[0]){
						sentiments[0]=true;
						count++;
					}
						
					if(Analytics.getAngryENList().contains(w) && !sentiments[1]){
						sentiments[1]=true;
						count++;
					}
						
					if(Analytics.getSadENList().contains(w) && !sentiments[2]){
						sentiments[2]=true;
						count++;
					}
						
					if(Analytics.getHilariousENList().contains(w) && !sentiments[3]){
						sentiments[3]=true;
						count++;
					}
						
				}
			}
			//Se il conteggio è maggiore di 3, avremo un sentiment neutrale
			if(count>=3)
				sentiments[4]=true;
			return sentiments;
			
        }
    
      };

}
