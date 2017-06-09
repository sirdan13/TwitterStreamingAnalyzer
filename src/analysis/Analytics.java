package analysis;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;
import utilities.Tweet;

public class Analytics {

	private static JavaSparkContext jsc;
	
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

}
