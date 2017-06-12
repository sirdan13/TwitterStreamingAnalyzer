package analysis;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;
import utilities.Tweet;

public class Analytics {

	private static JavaSparkContext jsc;
	private static JavaRDD<String> stopWords;
	private static Set<String> stopWordsSet;
	private static JavaRDD<String> likeEN;
	private static Set<String> likeENList;
	private static JavaRDD<String> sadEN;
	private static Set<String> sadENList;
	private static JavaRDD<String> angryEN;
	private static Set<String> angryENList;
	private static JavaRDD<String> hilariousEN;
	private static Set<String> hilariousENList;
	private static JavaRDD<String> likeIT;
	private static Set<String> likeITList;
	private static JavaRDD<String> sadIT;
	private static Set<String> sadITList;
	private static JavaRDD<String> angryIT;
	private static Set<String> angryITList;
	private static JavaRDD<String> hilariousIT;
	private static Set<String> hilariousITList;
	

	
	
	

	public static void setStopWords(JavaRDD<String> stopWords) {
		Analytics.stopWords = stopWords;
	}

	public Analytics(JavaSparkContext jsc){
		Analytics.jsc=jsc;
	}
	
	public static void init(){
	
		Logger.getLogger("org").setLevel(Level.ERROR);
		Logger.getLogger("akka").setLevel(Level.ERROR);
		SparkConf conf = new SparkConf();
		conf.setAppName("Twitter Streaming");
		conf.setMaster("local[*]");
		jsc = new JavaSparkContext(conf); 
		setStopWords("config/stopwords.txt");
		setStopWordsSet(new HashSet<String>(Analytics.getStopWords().collect()));
	//	setLikeEN("words/en/Love-Like-Wow.txt");
	//	setStopWordsSet(new HashSet<String>(Analytics.getStopWords().collect()));
		

	}
	
	public static List<Tuple2<Integer, String>> countHashtag(List<Tweet> tlist) {
		
		JavaRDD<Tweet> pTweet = jsc.parallelize(tlist);
		JavaRDD<List<String>> pHashtags = pTweet.map((x)->new ArrayList<String>(x.getHashtagsList()));
		JavaPairRDD<String, Integer> htCount = pHashtags.flatMapToPair(new HashtagsExtractor());
		JavaPairRDD<String, Integer> htFreq = htCount.reduceByKey(new SommaFrequenze()).filter(x->(x._2>1));
		JavaPairRDD<Integer, String> htFreq2 = htFreq.mapToPair(new SwapKeyValue()).sortByKey(false);
		List<Tuple2<Integer, String>> output = htFreq2.take(10);
		return output;
	
	}
	
public static JavaPairRDD<Integer, String> countHashtag(JavaRDD<String> words) {
		
//		JavaRDD<String> pTweet = lines.compute(lines);
	
	JavaPairRDD<String, Integer> wordCount = words.mapToPair((x)->new Tuple2<String, Integer>(x, 1)).reduceByKey(new Function2<Integer, Integer, Integer>() {
        @Override
        public Integer call(Integer arg0, Integer arg1) {
          return arg0+arg1;
        }
      });
	
	return  wordCount.mapToPair(new PairFunction<Tuple2<String, Integer>, Integer, String>(){
		@Override
		public Tuple2<Integer, String> call (Tuple2<String, Integer> arg0){
			return arg0.swap();
		}
	}).sortByKey(false);
	
	/*
	
		JavaRDD<List<String>> pHashtags = pTweet.map((x)->new ArrayList<String>(x.getHashtagsList()));
		JavaPairRDD<String, Integer> htCount = pHashtags.flatMapToPair(new HashtagsExtractor());
		JavaPairRDD<String, Integer> htFreq = htCount.reduceByKey(new SommaFrequenze()).filter(x->(x._2>1));
		JavaPairRDD<Integer, String> htFreq2 = htFreq.mapToPair(new SwapKeyValue()).sortByKey(false);
		List<Tuple2<Integer, String>> output = htFreq2.take(10);
		return output;
	*/
	}

public static JavaRDD<String> getStopWords() {
	return stopWords;
}

public static void setStopWords(String file) {
	
	Analytics.stopWords=jsc.textFile(file).distinct();

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

public static void setLikeEN(JavaRDD<String> likeEN) {
	Analytics.likeEN = likeEN;
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

public static void setSadEN(JavaRDD<String> sadEN) {
	Analytics.sadEN = sadEN;
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

public static void setAngryEN(JavaRDD<String> angryEN) {
	Analytics.angryEN = angryEN;
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

public static void setHilariousEN(JavaRDD<String> hilariousEN) {
	Analytics.hilariousEN = hilariousEN;
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

public static void setLikeIT(JavaRDD<String> likeIT) {
	Analytics.likeIT = likeIT;
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

public static void setSadIT(JavaRDD<String> sadIT) {
	Analytics.sadIT = sadIT;
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

public static void setAngryIT(JavaRDD<String> angryIT) {
	Analytics.angryIT = angryIT;
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

public static void setHilariousIT(JavaRDD<String> hilariousIT) {
	Analytics.hilariousIT = hilariousIT;
}

public static Set<String> getHilariousITList() {
	return hilariousITList;
}

public static void setHilariousITList(Set<String> hilariousITList) {
	Analytics.hilariousITList = hilariousITList;
}
	
	

}
