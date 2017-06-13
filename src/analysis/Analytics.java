package analysis;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.PairFlatMapFunction;

import scala.Tuple2;
import utilities.Tweet;

public class Analytics {

	private static JavaSparkContext jsc;
	
	//Set di stopwords
	private static JavaRDD<String> stopWords;
	private static Set<String> stopWordsSet;
	
	//Set di dizionari
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
	
	//Set di emoji
	private static JavaRDD<String> emojiLike;
	private static Set<String> emojiLikeSet;
	private static JavaRDD<String> emojiSad;
	private static Set<String> emojiSadSet;
	private static JavaRDD<String> emojiAngry;
	private static Set<String> emojiAngrySet;
	private static JavaRDD<String> emojiHilarious;
	private static Set<String> emojiHilariousSet;
	

private static void setAllDictionaries() {
	
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
		setAllDictionaries();
		
	}
	
	

	public static List<Tuple2<Integer, String>> countHashtag(List<Tweet> tlist) {
		
		JavaRDD<Tweet> pTweet = jsc.parallelize(tlist);
		JavaRDD<List<String>> pHashtags = pTweet.map((x)->new ArrayList<String>(x.getHashtagsList()));
		JavaPairRDD<String, Integer> htCount = pHashtags.flatMapToPair(htExtractor);
		JavaPairRDD<String, Integer> htFreq = htCount.reduceByKey(ConsumerAnalytics.sumFunc).filter(x->(x._2>1));
		JavaPairRDD<Integer, String> htFreq2 = htFreq.mapToPair(x->x.swap()).sortByKey(false);
		List<Tuple2<Integer, String>> output = htFreq2.take(10);
		return output;
	
	}
	
public static JavaPairRDD<Integer, String> countHashtag(JavaRDD<String> words) {

	JavaPairRDD<String, Integer> wordCount = words.mapToPair((x)->new Tuple2<String, Integer>(x, 1)).reduceByKey(ConsumerAnalytics.sumFunc);
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
	
	

}
