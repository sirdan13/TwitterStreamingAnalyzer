package analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.Row;

import utilities.DateManager;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;

import consumer.SparkConsumer;
import graphics.Graphics;
import scala.Tuple2;
import twitter4j.TwitterException;

public class CassandraManager {
	
	//Connessione a Cassandra
	static Builder builder;
	static Cluster cluster;
	static Session session;
	static int j = 0;
	
	//Configurazione Spark
	static SparkConf conf;
	static String appName;
	static String master;
	static JavaSparkContext jsc;
	static JavaStreamingContext jssc;
	
	//Queries di inserimento 
	static BoundStatement bsInsertHashtag;
	static BoundStatement bsInserTText;
	static PreparedStatement psInsertText;
	static PreparedStatement psInsertHashtag;
	static BoundStatement bsInsertTweet;
	static PreparedStatement psInsertTweet;
	static BoundStatement bsInsertSentiment;
	static PreparedStatement psInsertSentiment;
	static BoundStatement bsInsertTopword;
	static PreparedStatement psInsertTopword;
	static BoundStatement bsInsertMentions;
	static PreparedStatement psInsertMentions;
	
	//Queries di lettura dati
	static BoundStatement bsGetTweets;
	static PreparedStatement psGetTweets;
	static BoundStatement bsGetTweetsWithTime;
	static PreparedStatement psGetTweetsWithTime;
	static BoundStatement bsGetTweetCount;
	static PreparedStatement psGetTweetCount;
	static BoundStatement bsGetTweetCountWithTime;
	static PreparedStatement psGetTweetCountWithTime;
	static BoundStatement bsGetHashtags;
	static PreparedStatement psGetHashtags;
	static BoundStatement bsGetHashtagsWithTime;
	static PreparedStatement psGetHashtagsWithTime;
	static BoundStatement bsGetTopwords;
	static PreparedStatement psGetTopwords;
	static BoundStatement bsGetTopwordsWithTime;
	static PreparedStatement psGetTopwordsWithTime;
	static BoundStatement bsGetMentions;
	static PreparedStatement psGetMentions;
	static BoundStatement bsGetMentionsWithTime;
	static PreparedStatement psGetMentionsWithTime;
	static BoundStatement bsGetSentiment;
	static PreparedStatement psGetSentiment;
	static BoundStatement bsGetSentimentWithTime;
	static PreparedStatement psGetSentimentWithTime;
	
	
	public CassandraManager(List<String> contactPoints, String user, String password, JavaSparkContext jsc) throws FileNotFoundException{
		builder = Cluster.builder().withCredentials(user, password);
		for(String cp : contactPoints)
			builder.addContactPoint(cp);
		cluster = CassandraManager.builder.build();
		session = cluster.connect("gbd2017_twitteranalysis");
		prepareStatements();
		CassandraManager.jsc=jsc;
	//	loadSparkConf();
	//	initSpark();
	}
	
	public CassandraManager(String contactPoint, String user, String password) throws FileNotFoundException{
		builder = Cluster.builder().withCredentials(user, password);
		builder.addContactPoint(contactPoint);
		cluster=CassandraManager.builder.build();
		session= cluster.connect(user);
		prepareStatements();
		//loadSparkConf();
		//initSpark();
	}
	
	public CassandraManager(List<String> hosts, String user, String password) throws FileNotFoundException {
			
		builder = Cluster.builder().withCredentials(user, password);
		for(String cp : hosts)
			builder.addContactPoint(cp);
		cluster=CassandraManager.builder.build();
		session= cluster.connect("gbd2017_twitteranalysis");
		prepareStatements();
		if(CassandraManager.jsc==null){
			loadSparkConf();
			initSpark();
		}
		
	}

	private static void initSpark() {
		conf = new SparkConf().setAppName(appName).setMaster(master).set("spark.driver.allowMultipleContexts", "true");
		jsc = new JavaSparkContext(conf);
		System.out.println("JSC ready");
		Logger.getLogger("org").setLevel(Level.ERROR);
		Logger.getLogger("akka").setLevel(Level.ERROR);
	}
	
	private static void loadSparkConf() throws FileNotFoundException{
		Scanner sc = new Scanner(new File("config/spark_conf.txt"));
		while(sc.hasNextLine()){
			String line = sc.nextLine();
			if(line.startsWith("appName"))
				appName = line.split("=")[1];
			if(line.startsWith("setMaster"))
				master = line.split("=")[1];
			}
		sc.close();

	}
	
	private static void initCassandra() throws FileNotFoundException{
		List<String> hosts = SparkConsumer.readCassandraHosts("config/cassandra_hosts.txt");
		List<String> credenzialiCassandra = SparkConsumer.readCassandraCredentials("config/credenziali_cassandra.txt");
		new CassandraManager(hosts, credenzialiCassandra.get(0), credenzialiCassandra.get(1));
		
	}
	
	public static void main(String [] args) throws FileNotFoundException{

		List<String> hosts = SparkConsumer.readCassandraHosts("config/cassandra_hosts.txt");
		List<String> credenzialiCassandra = SparkConsumer.readCassandraCredentials("config/credenziali_cassandra.txt");
		new CassandraManager(hosts, credenzialiCassandra.get(0), credenzialiCassandra.get(1));
	//	ResultSet rs = getTweetsWithTime("prova", "2017-06-19 16:20:00.000", "2017-06-19 20:20:00.000");
	//	ResultSet rs = getTopwords("calcio");
		System.out.println(getTweetCount("maturità2017"));
		/*for(Row r : rs)
			System.out.println(r.getString("topic")+" "+r.getString("text")+" "+r.getInt("frequence"));*/
	//	System.out.println(a);
		session.close();
	}
	
	
	
	public static void prepareStatements(){
		
		psInsertHashtag = session.prepare("INSERT INTO hashtags (created_at, text, frequence, topic) VALUES (?, ?, ?, ?)");
		bsInsertHashtag = new BoundStatement(psInsertHashtag);

		psInsertTweet = session.prepare("INSERT INTO tweet (text, created_at, user_name, topic, likeCount, retweetCount) VALUES (?, ?, ?, ?, ?, ?)");
		bsInsertTweet = new BoundStatement(psInsertTweet);
		
		psInsertSentiment = session.prepare("insert into sentiment(created_at, like, angry, hilarious, sad, neutral, topic) values (?, ?, ?, ?, ?, ?, ?)");
		bsInsertSentiment = new BoundStatement(psInsertSentiment);
		
		psInsertTopword = session.prepare("INSERT INTO topwords (created_at, text, frequence, topic) VALUES (?, ?, ?, ?)");
		bsInsertTopword = new BoundStatement(psInsertTopword);
		
		psInsertMentions = session.prepare("INSERT INTO mentions (created_at, user, frequence, topic) VALUES (?, ?, ?, ?)");
		bsInsertMentions = new BoundStatement(psInsertMentions);
		
		psGetTweets = session.prepare("select * from tweet where topic = ?");
		bsGetTweets = new BoundStatement(psGetTweets);
		
		psGetTweetsWithTime = session.prepare("select * from tweet where topic = ? and created_at> ? and created_at< ?");
		bsGetTweetsWithTime = new BoundStatement(psGetTweetsWithTime);
		
		psGetTweetCount = session.prepare("select count(*) from tweet where topic = ?");
		bsGetTweetCount = new BoundStatement(psGetTweetCount);
		
		psGetTweetCountWithTime = session.prepare("select count(*) from tweet where topic = ? and created_at> ? and created_at< ?");
		bsGetTweetCountWithTime = new BoundStatement(psGetTweetCountWithTime);
		
		psGetHashtags = session.prepare("select * from hashtags where topic = ?");
		bsGetHashtags = new BoundStatement(psGetHashtags);
		
		psGetHashtagsWithTime = session.prepare("select * from hashtags where topic = ? and created_at> ? and created_at< ?");
		bsGetHashtagsWithTime = new BoundStatement(psGetHashtagsWithTime);
		
		psGetTopwords = session.prepare("select * from topwords where topic = ?");
		bsGetTopwords = new BoundStatement(psGetTopwords);
		
		psGetTopwordsWithTime = session.prepare("select * from topwords where topic = ? and created_at> ? and created_at< ?");
		bsGetTopwordsWithTime = new BoundStatement(psGetTopwordsWithTime);
		
		psGetMentions = session.prepare("select * from mentions where topic = ?");
		bsGetMentions = new BoundStatement(psGetMentions);
		
		psGetMentionsWithTime = session.prepare("select * from mentions where topic = ? and created_at> ? and created_at< ?");
		bsGetMentionsWithTime = new BoundStatement(psGetMentionsWithTime);
		
		psGetSentiment = session.prepare("select sum(like) as like, sum(sad) as sad, sum(angry) as angry, sum(hilarious) as hilarious, sum(neutral) as neutral, count(*) from sentiment where topic = ?");
		bsGetSentiment = new BoundStatement(psGetSentiment);
		
		psGetSentimentWithTime = session.prepare("select sum(like) as like, sum(sad) as sad, sum(angry) as angry, sum(hilarious) as hilarious, sum(neutral) as neutral, count(*) from sentiment where topic = ? and created_at> ? and created_at< ?");
		bsGetSentimentWithTime = new BoundStatement(psGetSentimentWithTime);
		
	}
	
	public void insertHashtag(String hashtag, Integer freq){
		
		session.execute(bsInsertHashtag.bind(DateManager.getDate(), hashtag.split(";")[0], freq, hashtag.split(";")[1]));
	}
	
public void insertTweet(String text, String user_name, String created_at, String topic, String likeCount, String retweetCount){
		
	session.execute(bsInsertTweet.bind(text, user_name, created_at, topic, Integer.parseInt(likeCount), Integer.parseInt(retweetCount)));
	
	}

public void insertSentiment(double like, double angry, double hilarious, double sad, int neutral, String topic) {
	
		session.execute(bsInsertSentiment.bind(DateManager.getDate(), like, angry, hilarious, sad, neutral, topic));
	}

public void insertTopword(String word, Integer frequence, String topic) {
	session.execute(bsInsertTopword.bind(DateManager.getDate(), word, frequence, topic));
}

public void insertMention(String user_name, Integer frequence) {
	session.execute(bsInsertMentions.bind(DateManager.getDate(), user_name.split(";")[0], frequence, user_name.split(";")[1]));
}

public static ResultSet getTweets(String topic){
	ResultSet rs = session.execute(bsGetTweets.bind(topic));
	return rs;
}

public static ResultSet getTweetsWithTime(String topic, String startTime, String endTime){
	ResultSet rs = session.execute(bsGetTweetsWithTime.bind(topic, startTime, endTime));
	return rs;
	
}

public static String getTweetCount(String topic){
	ResultSet rs = session.execute(bsGetTweetCount.bind(topic));
	long output = 0;
	for(Row r : rs)
		output = r.getLong("count");
	return Long.toString(output);
}

public static String getTweetCountWithTime(String topic, String startTime, String endTime){
	ResultSet rs = session.execute(bsGetTweetCountWithTime.bind(topic, startTime, endTime));
	long output = 0;
	for(Row r : rs)
		output = r.getLong("count");
	return Long.toString(output);
	
}

public static ResultSet getHashtags(String topic){
	ResultSet rs = session.execute(bsGetHashtags.bind(topic));
	return rs;
}

public static ResultSet getHashtagsWithTime(String topic, String startTime, String endTime){
	ResultSet rs = session.execute(bsGetHashtagsWithTime.bind(topic, startTime, endTime));
	return rs;
	
}

public static ResultSet getTopwords(String topic){
	ResultSet rs = session.execute(bsGetTopwords.bind(topic));
	return rs;
}

public static ResultSet getTopwordsWithTime(String topic, String startTime, String endTime){
	ResultSet rs = session.execute(bsGetTopwordsWithTime.bind(topic, startTime, endTime));
	return rs;
	
}

public static ResultSet getMentions(String topic){
	System.out.println(topic);
	bsGetMentions.bind(topic);
	ResultSet rs = session.execute(bsGetMentions);
	return rs;
}

public static ResultSet getMentionsWithTime(String topic, String startTime, String endTime){
	ResultSet rs = session.execute(bsGetMentionsWithTime.bind(topic, startTime, endTime));
	return rs;
	
}

public static ResultSet getSentiment(String topic){
	ResultSet rs = session.execute(bsGetSentiment.bind(topic));
	return rs;
}

public ResultSet getSentimentWithTime(String topic, String startTime, String endTime){
	ResultSet rs = session.execute(bsGetSentimentWithTime.bind(topic, startTime, endTime));
	return rs;
	
}

public static void getTopUserWithTimeManager(String topic, String startTime, String endTime) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, TwitterException, IOException{
	initCassandra();
	ResultSet rs = getMentionsWithTime(topic, startTime, endTime);
	List<Tuple2<String, Integer>> tuplelist = new ArrayList<Tuple2<String, Integer>>();
	for(Row r : rs)
		tuplelist.add(new Tuple2<String, Integer>(r.getString("user"), r.getInt("frequence")));

	JavaPairRDD<String, Integer> topUsers = jsc.parallelizePairs(tuplelist).reduceByKey(sumFunc);
	Tuple2<Integer, String> topUser = topUsers.mapToPair(x->x.swap()).sortByKey(false).take(1).get(0);
	String user = topUser._2; Integer frequence = topUser._1;
	Graphics.mostMentionedUser(user, frequence);
}

static Function2<Integer, Integer, Integer> sumFunc = new Function2<Integer, Integer, Integer>() {
	
	
	private static final long serialVersionUID = 1L;
	
	@Override public Integer call(Integer i1, Integer i2) throws Exception {
	    return i1 + i2;
	  }
	};


public static void getTopUserManager(String topic) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, TwitterException, IOException{
	initCassandra();
	ResultSet rs = getMentions(topic);
	List<Tuple2<String, Integer>> tuplelist = new ArrayList<Tuple2<String, Integer>>();
	for(Row r : rs)
		tuplelist.add(new Tuple2<String, Integer>(r.getString("user"), r.getInt("frequence")));
	
	JavaPairRDD<String, Integer> topUsers = jsc.parallelizePairs(tuplelist).reduceByKey(sumFunc);
	Tuple2<Integer, String> topUser = topUsers.mapToPair(x->x.swap()).sortByKey(false).take(1).get(0);
	String user = topUser._2; Integer frequence = topUser._1;
	Graphics.mostMentionedUser(user, frequence);
}


public static void getTweetsWithTimeManager(String topic, String startTime, String endTime) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, TwitterException, IOException{
	initCassandra();
	ResultSet rs = getTweetsWithTime(topic, startTime, endTime);
	List<Tuple2<String, Integer>> tuplelist = new ArrayList<Tuple2<String, Integer>>();
	for(Row r : rs)
		tuplelist.add(new Tuple2<String, Integer>(r.getString("text")+":username"+r.getString("user_name"), r.getInt("likecount")+r.getInt("retweetcount")));
	JavaPairRDD<String, Integer> tweets = jsc.parallelizePairs(tuplelist);
	Tuple2<Integer, String> topTweet = tweets.mapToPair(x->x.swap()).sortByKey(false).take(1).get(0);
	String user = topTweet._2.split(":username")[1]; String text = topTweet._2.split(":username")[0];
	Graphics.topTweetWindow(text, user, topTweet._1);
}


public static void getTweetsManager(String topic) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, TwitterException, IOException{
	initCassandra();
	ResultSet rs = getTweets(topic);
	List<Tuple2<String, Integer>> tuplelist = new ArrayList<Tuple2<String, Integer>>();
	for(Row r : rs)
		tuplelist.add(new Tuple2<String, Integer>(r.getString("text")+":username"+r.getString("user_name"), r.getInt("likecount")+r.getInt("retweetcount")));
	JavaPairRDD<String, Integer> tweets = jsc.parallelizePairs(tuplelist);
	Tuple2<Integer, String> topTweet = tweets.mapToPair(x->x.swap()).sortByKey(false).take(1).get(0);
	String user = topTweet._2.split(":username")[1]; String text = topTweet._2.split(":username")[0];
	Graphics.topTweetWindow(text, user, topTweet._1);
	
}

public static void getTopWordsManagerWithTime(String topic, String startTime, String endTime) throws ClassNotFoundException, InstantiationException, IllegalAccessException, TwitterException, IOException, UnsupportedLookAndFeelException {
	initCassandra();
	ResultSet rs = getTopwordsWithTime(topic, startTime, endTime);
	List<Tuple2<String, Integer>> tuplelist = new ArrayList<Tuple2<String, Integer>>();
	for(Row r : rs)
		tuplelist.add(new Tuple2<String, Integer>(r.getString("text"), r.getInt("frequence")));
	
	JavaPairRDD<String, Integer> topwords = jsc.parallelizePairs(tuplelist).reduceByKey(sumFunc);
	List<Tuple2<Integer, String>> top10Words = topwords.mapToPair(x->x.swap()).sortByKey(false).take(10);
	if(top10Words.size()==0){
		JOptionPane.showMessageDialog(null, null, "Nessun risultato per la query desiderata", JOptionPane.ERROR_MESSAGE, new ImageIcon("config/icon.png"));
		System.exit(-1);
	}
	Graphics.topwordsWindow(top10Words);
	
}

public static void getTopWordsManager(String topic) throws ClassNotFoundException, InstantiationException, IllegalAccessException, TwitterException, IOException, UnsupportedLookAndFeelException {
	initCassandra();
	ResultSet rs = getTopwords(topic);
	List<Tuple2<String, Integer>> tuplelist = new ArrayList<Tuple2<String, Integer>>();
	for(Row r : rs)
		tuplelist.add(new Tuple2<String, Integer>(r.getString("text"), r.getInt("frequence")));
		
	JavaPairRDD<String, Integer> topwords = jsc.parallelizePairs(tuplelist).reduceByKey(sumFunc);
	System.out.println(tuplelist.size());
	System.out.println(topwords.count());
	List<Tuple2<Integer, String>> top10Words = topwords.mapToPair(x->x.swap()).sortByKey(false).take(10);
	System.out.println(top10Words.size());
	Graphics.topwordsWindow(top10Words);
}

public static void getHashtagsManagerWithTime(String topic, String startTime, String endTime) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, TwitterException, IOException {

	initCassandra();
	ResultSet rs = getHashtagsWithTime(topic, startTime, endTime);
	List<Tuple2<String, Integer>> tuplelist = new ArrayList<Tuple2<String, Integer>>();
	for(Row r : rs)
		tuplelist.add(new Tuple2<String, Integer>(r.getString("text"), r.getInt("frequence")));
	
	JavaPairRDD<String, Integer> topHT = jsc.parallelizePairs(tuplelist).reduceByKey(sumFunc);
	List<Tuple2<Integer, String>> top10HT = topHT.mapToPair(x->x.swap()).sortByKey(false).take(10);
	
	Graphics.topHTWindow(top10HT);
}

public static void getHashtagsManager(String topic) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, TwitterException, IOException {
	
	initCassandra();
	ResultSet rs = getHashtags(topic);
	List<Tuple2<String, Integer>> tuplelist = new ArrayList<Tuple2<String, Integer>>();
	for(Row r : rs)
		tuplelist.add(new Tuple2<String, Integer>(r.getString("text"), r.getInt("frequence")));
	
	JavaPairRDD<String, Integer> topHT = jsc.parallelizePairs(tuplelist).reduceByKey(sumFunc);
	List<Tuple2<Integer, String>> top10HT = topHT.mapToPair(x->x.swap()).sortByKey(false).take(10);
	
	Graphics.topHTWindow(top10HT);
}

public static void getTweetCountManagerWithTime(String topic, String startTime, String endTime) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, TwitterException, IOException {

	initCassandra();
	Graphics.tweetCount(getTweetCountWithTime(topic, startTime, endTime));
		
}

public static void getTweetCountManager(String topic) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, TwitterException, IOException {
	
	initCassandra();
	Graphics.tweetCount(getTweetCount(topic));
	
}

public static void getSentimentManager(String topic) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, TwitterException, IOException {
	
	initCassandra();
	ResultSet rs = getSentiment(topic);
	double like = 0, sad = 0, angry = 0, hilarious = 0;
	long total = 0;
	for(Row r : rs){
		total = r.getLong("count");
		like = r.getDouble("like")/total*100; sad = r.getDouble("sad")/total*100; angry = r.getDouble("angry")/total*100; hilarious = r.getDouble("hilarious")/total*100;
	}
		
	Graphics.sentimentWindow(like, sad, angry, hilarious);
}

public static void getSentimentWithTimeManager(String topic, String startTime, String endTime) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, TwitterException, IOException {
	initCassandra();
	ResultSet rs = getSentiment(topic);
	double like = 0, sad = 0, angry = 0, hilarious = 0;
	long total = 0;
	for(Row r : rs){
		total = r.getLong("count");
		like = r.getDouble("like")/total*100; sad = r.getDouble("sad")/total*100; angry = r.getDouble("angry")/total*100; hilarious = r.getDouble("hilarious")/total*100;
		
	}
		
	Graphics.sentimentWindow(Analytics.round(like, 2), Analytics.round(sad, 2), Analytics.round(angry, 2), Analytics.round(hilarious, 2));
	
}

}
