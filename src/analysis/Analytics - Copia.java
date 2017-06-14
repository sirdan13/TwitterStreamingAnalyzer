package analysis;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;

import com.vdurmont.emoji.EmojiParser;

import scala.Tuple2;
import utilities.Tweet;

public class Analytics {

	//Configurazione di Spark e Spark Streaming
	private static JavaSparkContext jsc;
	//TODO eliminare jsc, una volta che lavoreremo con jssc non ne avremo più bisogno
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
	
	//Icona per le finestre interattive
	static Icon icon = new ImageIcon("config/icon.png");
	
	//Set di stopwords
	private static JavaRDD<String> stopWords;
	private static Set<String> stopWordsSet;
	
	//Set di dizionari
	public static JavaRDD<String> likeEN;
	public static Set<String> likeENList;
	public static JavaRDD<String> sadEN;
	public static Set<String> sadENList;
	public static JavaRDD<String> angryEN;
	public static Set<String> angryENList;
	public static JavaRDD<String> hilariousEN;
	public static Set<String> hilariousENList;
	public static JavaRDD<String> likeIT;
	public static Set<String> likeITList;
	public static JavaRDD<String> sadIT;
	public static Set<String> sadITList;
	public static JavaRDD<String> angryIT;
	public static Set<String> angryITList;
	public static JavaRDD<String> hilariousIT;
	public static Set<String> hilariousITList;
	
	//Set di emoji
	public static JavaRDD<String> emojiLike;
	public static Set<String> emojiLikeSet;
	public static JavaRDD<String> emojiSad;
	public static Set<String> emojiSadSet;
	public static JavaRDD<String> emojiAngry;
	public static Set<String> emojiAngrySet;
	public static JavaRDD<String> emojiHilarious;
	public static Set<String> emojiHilariousSet;
	
	
	public Analytics(JavaStreamingContext jssc, String zookeeper_server, String kafka_consumer_group, Map<String, Integer> topics) throws FileNotFoundException{
		Analytics.jssc=jssc;
		Analytics.zookeeper_server=zookeeper_server;
		Analytics.kafka_consumer_group=kafka_consumer_group;
		Analytics.topics=topics;
		leggiDizionari();
	}
	
	public static void init(){
	
		Logger.getLogger("org").setLevel(Level.ERROR);
		Logger.getLogger("akka").setLevel(Level.ERROR);
		SparkConf conf = new SparkConf();
		conf.setAppName("Twitter Streaming");
		conf.setMaster("local[*]");
		conf.set("spark.driver.allowMultipleContexts", "true");
		jsc = new JavaSparkContext(conf); 
		setAllDictionaries();
	}
	
	public static void leggiDizionari() throws FileNotFoundException{
		
		setStopWordsSet(leggiDizionario("config/stopwords.txt"));
		setLikeENList(leggiDizionario("words/en/Love-Like-Wow.txt"));
		setAngryENList(leggiDizionario("words/en/Angry.txt"));
		setHilariousENList(leggiDizionario("words/en/Hilarious.txt"));
		setSadENList(leggiDizionario("words/en/Sad.txt"));
		setLikeITList(leggiDizionario("words/it/Love-Like-Wow.txt"));
		setAngryITList(leggiDizionario("words/it/Angry.txt"));
		setHilariousITList(leggiDizionario("words/it/Hilarious.txt"));
		setSadITList(leggiDizionario("words/it/Sad.txt"));
		setEmojiLikeSet(leggiDizionario("words/emoji/emojilike.txt"));
		setEmojiSadSet(leggiDizionario("words/emoji/emojisad.txt"));
		setEmojiAngrySet(leggiDizionario("words/emoji/emojiangry.txt"));
		setEmojiHilariousSet(leggiDizionario("words/emoji/emojihilarious.txt"));
		
	}
	
	public static Set<String> leggiDizionario(String file) throws FileNotFoundException{
		Scanner sc = new Scanner(new File(file));
		Set<String> set = new HashSet<>();
		while(sc.hasNextLine())
			set.add(sc.nextLine());
		sc.close();
		return set;
	}
	

private static void setAllDictionaries() {
	
	/*
	 * Imposta il dizionario dei termini da rimuovere
	 */
		setStopWords("config/stopwords.txt");
	
	
	/*
	 * Imposto i dizionari dei sentiment in lingua inglese
	 */
		setLikeEN("words/en/Love-Like-Wow.txt");
		setAngryEN("words/en/Angry.txt");
		setHilariousEN("words/en/Hilarious.txt");
		setSadEN("words/en/Sad.txt");
		
	/*
	 * Imposto i dizionari dei sentiment in lingua italiana
	 */
		setLikeIT("words/it/Love-Like-Wow.txt");
		setAngryIT("words/it/Angry.txt");
		setHilariousIT("words/it/Hilarious.txt");
		setSadIT("words/it/Sad.txt");
		
	/*
	 * Imposto i dizionari delle emoji
	 */
		setEmojiLike("words/emoji/emojilike.txt");
		setEmojiSad("words/emoji/emojisad.txt");
		setEmojiAngry("words/emoji/emojiangry.txt");
		setEmojiHilarious("words/emoji/emojihilarious.txt");
	}
	
	public static void setStopWords(JavaRDD<String> stopWords) {
		Analytics.stopWords = stopWords;
	}

	static PairFunction<Tuple2<String, String>, String, Integer> sentimentFunc = new PairFunction<Tuple2<String, String>, String, Integer>(){
	    	private static final long serialVersionUID = 1L;
	
	
			@Override
			public Tuple2<String, Integer> call(Tuple2<String, String> x) throws Exception {
				if(x._2==null)
					return new Tuple2<String, Integer>("ERR", 1);
				boolean like = false, sad = false, angry = false, hilarious = false, neutral = false;
				boolean [] sentiments = {like, angry, sad, hilarious, neutral};
				sentiments = checkEmojis(x, sentiments);
				if(checkSentiment(sentiments))
					return setSentiment(sentiments);
				sentiments = checkText(x, sentiments);
				return setSentiment(sentiments);
			}
			
	  };
	  
	  
	//Verifica se un sentiment è stato trovato
		private static boolean checkSentiment(boolean [] sentiments){
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
		private static Tuple2<String, Integer> setSentiment(boolean [] sentiments) {
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


	public static boolean[] checkEmojis(Tuple2<String, String> x, boolean [] sentiments) {
		String text = EmojiParser.parseToAliases(x._2);
		int count = 0;
		for(String w : text.split(" ")){
			if(w==null)
				continue;
			if(getEmojiLikeSet().contains(w) && !sentiments[0]){
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
  
  
  
 private static boolean[] checkText(Tuple2<String, String> x, boolean[] sentiments){

  	int count = 0;
		//Verifico lingua del tweet
		if(x._1().equals("it")){
			for(String w : x._2.split(" ")){
				if(w==null)
					continue;
				
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
				if(w==null)
					continue;
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

	  
	  
	static Function<JavaPairRDD<Integer,String>, JavaPairRDD<Integer, String>> sortFunc = new Function<JavaPairRDD<Integer,String>, JavaPairRDD<Integer,String>>() {
	
	private static final long serialVersionUID = 1L;
	
	@Override
	public JavaPairRDD<Integer,String> call(JavaPairRDD<Integer,String> rdd) throws Exception {
	           return rdd.sortByKey(false);
	         }
	     };
	static Function2<Integer, Integer, Integer> sumFunc = new Function2<Integer, Integer, Integer>() {
	
	
	private static final long serialVersionUID = 1L;
	
	@Override public Integer call(Integer i1, Integer i2) throws Exception {
	    return i1 + i2;
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


	
	
	

	public static List<Tuple2<Integer, String>> countHashtag(List<Tweet> tlist) {
		
		JavaRDD<Tweet> pTweet = jsc.parallelize(tlist);
		JavaRDD<List<String>> pHashtags = pTweet.map((x)->new ArrayList<String>(x.getHashtagsList()));
		JavaPairRDD<String, Integer> htCount = pHashtags.flatMapToPair(htExtractor);
		JavaPairRDD<String, Integer> htFreq = htCount.reduceByKey(sumFunc).filter(x->(x._2>1));
		JavaPairRDD<Integer, String> htFreq2 = htFreq.mapToPair(x->x.swap()).sortByKey(false);
		List<Tuple2<Integer, String>> output = htFreq2.take(10);
		return output;
	
	}
	
public static JavaPairRDD<Integer, String> countHashtag(JavaRDD<String> words) {

	JavaPairRDD<String, Integer> wordCount = words.mapToPair((x)->new Tuple2<String, Integer>(x, 1)).reduceByKey(sumFunc);
	return  wordCount.mapToPair(x->x.swap()).sortByKey(false);
	
}

public static JavaRDD<String> getStopWords() {
	return stopWords;
}

public static void setStopWords(String file) {
	Analytics.stopWords=jsc.textFile(file).distinct();
	setStopWordsSet(new HashSet<String>(Analytics.getStopWords().collect()));
}

public static JavaSparkContext getJsc() {
	return jsc;
}

public static void setJsc(JavaSparkContext jsc) {
	Analytics.jsc = jsc;
}

public static Set<String> getStopWordsSet() {
	return stopWordsSet;
}

public static void setStopWordsSet(Set<String> stopWordsSet) {
	Analytics.stopWordsSet = stopWordsSet;
}


public static JavaRDD<String> getLikeEN() {
	return likeEN;
}

public static void setLikeEN(String file) {
	setLikeENList(new HashSet<String>(jsc.textFile(file).distinct().collect()));
}

public static Set<String> getLikeENList() {
	return likeENList;
}

public static void setLikeENList(Set<String> likeENList) {
	Analytics.likeENList = likeENList;
}

public static JavaRDD<String> getSadEN() {
	return sadEN;
}

public static void setSadEN(String file) {
	setSadENList(new HashSet<String>(jsc.textFile(file).distinct().collect()));
}

public static Set<String> getSadENList() {
	return sadENList;
}

public static void setSadENList(Set<String> sadENList) {
	Analytics.sadENList = sadENList;
}

public static JavaRDD<String> getAngryEN() {
	return angryEN;
}

public static void setAngryEN(String file) {
	setAngryENList(new HashSet<String>(jsc.textFile(file).distinct().collect()));
}

public static Set<String> getAngryENList() {
	return angryENList;
}

public static void setAngryENList(Set<String> angryENList) {
	Analytics.angryENList = angryENList;
}

public static JavaRDD<String> getHilariousEN() {
	return hilariousEN;
}

public static void setHilariousEN(String file) {
	setHilariousENList(new HashSet<String>(jsc.textFile(file).distinct().collect()));
}

public static Set<String> getHilariousENList() {
	return hilariousENList;
}

public static void setHilariousENList(Set<String> hilariousENList) {
	Analytics.hilariousENList = hilariousENList;
}


public static JavaRDD<String> getLikeIT() {
	return likeIT;
}

public static void setLikeIT(String file) {
	setLikeITList(new HashSet<String>(jsc.textFile(file).distinct().collect()));
}

public static Set<String> getLikeITList() {
	return likeITList;
}

public static void setLikeITList(Set<String> likeITList) {
	Analytics.likeITList = likeITList;
}

public static JavaRDD<String> getSadIT() {
	return sadIT;
}

public static void setSadIT(String file) {
	setSadITList(new HashSet<String>(jsc.textFile(file).distinct().collect()));
}

public static Set<String> getSadITList() {
	return sadITList;
}

public static void setSadITList(Set<String> sadITList) {
	Analytics.sadITList = sadITList;
}

public static JavaRDD<String> getAngryIT() {
	return angryIT;
}

public static void setAngryIT(String file) {
	setAngryITList(new HashSet<String>(jsc.textFile(file).distinct().collect()));
}

public static Set<String> getAngryITList() {
	return angryITList;
}

public static void setAngryITList(Set<String> angryITList) {
	Analytics.angryITList = angryITList;
}

public static JavaRDD<String> getHilariousIT() {
	return hilariousIT;
}

public static void setHilariousIT(String file) {
	setHilariousITList(new HashSet<String>(jsc.textFile(file).distinct().collect()));
}

public static Set<String> getHilariousITList() {
	return hilariousITList;
}

public static void setHilariousITList(Set<String> hilariousITList) {
	Analytics.hilariousITList = hilariousITList;
}



public static JavaRDD<String> getEmojiLike() {
	return emojiLike;
}



public static void setEmojiLike(String file) {
	setEmojiLikeSet(new HashSet<String>(jsc.textFile(file).distinct().collect()));
}



public static Set<String> getEmojiLikeSet() {
	return emojiLikeSet;
}



public static void setEmojiLikeSet(Set<String> emojiLikeSet) {
	Analytics.emojiLikeSet = emojiLikeSet;
}



public static JavaRDD<String> getEmojiSad() {
	return emojiSad;
}



public static void setEmojiSad(String file) {
	setEmojiSadSet(new HashSet<String>(jsc.textFile(file).distinct().collect()));
}



public static Set<String> getEmojiSadSet() {
	return emojiSadSet;
}



public static void setEmojiSadSet(Set<String> emojiSadSet) {
	Analytics.emojiSadSet = emojiSadSet;
}



public static JavaRDD<String> getEmojiAngry() {
	return emojiAngry;
}



public static void setEmojiAngry(String file) {
	setEmojiAngrySet(new HashSet<String>(jsc.textFile(file).distinct().collect()));
}



public static Set<String> getEmojiAngrySet() {
	return emojiAngrySet;
}



public static void setEmojiAngrySet(Set<String> emojiAngrySet) {
	Analytics.emojiAngrySet = emojiAngrySet;
}



public static JavaRDD<String> getEmojiHilarious() {
	return emojiHilarious;
}



public static void setEmojiHilarious(String file) {
	setEmojiHilariousSet(new HashSet<String>(jsc.textFile(file).distinct().collect()));
}



public static Set<String> getEmojiHilariousSet() {
	return emojiHilariousSet;
}



public static void setEmojiHilariousSet(Set<String> emojiHilariousSet) {
	Analytics.emojiHilariousSet = emojiHilariousSet;
}

static PairFlatMapFunction<List<String>, String, Integer> htExtractor = new PairFlatMapFunction<List<String>, String, Integer>(){
	private static final long serialVersionUID = 1L;

	public Iterator<Tuple2<String, Integer>> call(List<String> list) throws Exception {
		ArrayList<Tuple2<String, Integer>> temp = new ArrayList<Tuple2<String, Integer>>();
		for(String h : list)
			temp.add(new Tuple2<String, Integer>(h, 1));
		return temp.iterator();
		}

};


private static void analyzeHashtags() throws InterruptedException {
	messages =  KafkaUtils.createStream(jssc, zookeeper_server, kafka_consumer_group, topics);
	lines = messages.mapToPair((x)->(new Tuple2<String, Integer>(x._2, 1))).reduceByKey(sumFunc);
	JavaPairDStream<Integer,String> sortedStream = lines.mapToPair(x->x.swap()).transformToPair(sortFunc);
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



private static void analyzeOriginalText() throws InterruptedException {
	messages =  KafkaUtils.createStream(jssc, zookeeper_server, kafka_consumer_group, topics);
	JavaPairDStream<String, Integer> words = messages.flatMap(wordFunc).mapToPair((x)->(new Tuple2<String, Integer>(x, 1))).reduceByKey(sumFunc);
	JavaPairDStream<Integer,String> sortedStream = words.mapToPair(x->x.swap()).transformToPair(sortFunc);
	sortedStream.print();
	jssc.start();
	jssc.awaitTermination();
	
}



private static void analyzeProcessedText() throws InterruptedException {
	messages =  KafkaUtils.createStream(jssc, zookeeper_server, kafka_consumer_group, topics);
	JavaPairDStream<String, Integer> words = messages.flatMap(wordFunc).mapToPair((x)->(new Tuple2<String, Integer>(x, 1))).reduceByKey(sumFunc);
	JavaPairDStream<Integer,String> sortedStream = words.mapToPair(x->x.swap()).transformToPair(sortFunc);
	sortedStream.print();
	jssc.start();
	jssc.awaitTermination();
}



private static void analyzeSentiment() throws InterruptedException{
	messages =  KafkaUtils.createStream(jssc, zookeeper_server, kafka_consumer_group, topics);
	lines = messages.mapToPair(sentimentFunc).reduceByKey(sumFunc);
	lines = messages.flatMap(sentiment2).mapToPair((x)->(new Tuple2<String, Integer>(x, 1))).reduceByKey(sumFunc);
	lines.print();
	jssc.start();
	jssc.awaitTermination();
}


static FlatMapFunction<Tuple2<String, String>, String> sentiment2 = new FlatMapFunction<Tuple2<String, String>, String>(){
	 private static final long serialVersionUID = 1L;
	
	@Override
	 public Iterator<String> call(Tuple2<String,String> x) throws Exception {
	            List<String> output = new ArrayList<String>();
	            if(x._2.length()==0)
	            	output.add("ERR");
	            for(String w : x._2().split(" "))
	            	output.add("neutral");
	            return output.iterator();
	            }
	
	
	};





public void analyzeTopic(String topic) throws InterruptedException{
	if(topic=="hashtags2")
		analyzeHashtags();
	if(topic=="mentions6")
		analyzeMentions();
	if(topic=="original-text2")
		analyzeOriginalText();
	if(topic=="processed-text2")
		analyzeProcessedText();
	if(topic=="sentiment")
		analyzeSentiment();
}
	
	

}
