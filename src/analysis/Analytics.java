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
	
	

}
