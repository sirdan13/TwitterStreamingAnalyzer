package analysis;

import java.util.List;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;

import utilities.DateManager;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class CassandraManager {
	
	//Connessione a Cassandra
	static Builder builder;
	static Cluster cluster;
	static Session session;
	static int j = 0;
	
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
	
	
	public CassandraManager(List<String> contactPoints, String user, String password){
		builder = Cluster.builder().withCredentials(user, password);
		for(String cp : contactPoints)
			builder.addContactPoint(cp);
		cluster=CassandraManager.builder.build();
		session= cluster.connect("gbd2017_twitteranalysis");
		prepareStatements();
	}
	
	public CassandraManager(String contactPoint, String user, String password){
		builder = Cluster.builder().withCredentials(user, password);
		builder.addContactPoint(contactPoint);
		cluster=CassandraManager.builder.build();
		session= cluster.connect(user);
		prepareStatements();
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
		
		psGetHashtagsWithTime = session.prepare("select count(*) from hashtag where topic = ? and created_at> ? and created_at< ?");
		bsGetHashtagsWithTime = new BoundStatement(psGetHashtagsWithTime);
		
		psGetTopwords = session.prepare("select * from topwords where topic = ?");
		bsGetTopwords = new BoundStatement(psGetTopwords);
		
		psGetTopwordsWithTime = session.prepare("select count(*) from topwords where topic = ? and created_at> ? and created_at< ?");
		bsGetTopwordsWithTime = new BoundStatement(psGetTopwordsWithTime);
		
		psGetMentions = session.prepare("select * from mentions where topic = ?");
		bsGetMentions = new BoundStatement(psGetMentions);
		
		psGetMentionsWithTime = session.prepare("select count(*) from mentions where topic = ? and created_at> ? and created_at< ?");
		bsGetMentionsWithTime = new BoundStatement(psGetMentionsWithTime);
		
		psGetSentiment = session.prepare("select * from sentiment where topic = ?");
		bsGetSentiment = new BoundStatement(psGetSentiment);
		
		psGetSentimentWithTime = session.prepare("select count(*) from sentiment where topic = ? and created_at> ? and created_at< ?");
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

public ResultSet getTweets(String topic){
	ResultSet rs = session.execute(bsGetTweets.bind(topic));
	return rs;
}

public ResultSet getTweetsWithTime(String topic, String startTime, String endTime){
	ResultSet rs = session.execute(bsGetTweets.bind(topic, startTime, endTime));
	return rs;
	
}

public int getTweetCount(String topic){
	ResultSet rs = session.execute(bsGetTweets.bind(topic));
	for(Row r : rs)
		return r.getInt("count");
	return -1;
}

public int getTweetCountWithTime(String topic, String startTime, String endTime){
	ResultSet rs = session.execute(bsGetTweets.bind(topic, startTime, endTime));
	for(Row r : rs)
		return r.getInt("count");
	return -1;
}

public ResultSet getHashtags(String topic){
	ResultSet rs = session.execute(bsGetHashtags.bind(topic));
	return rs;
}

public ResultSet getHashtagsWithTime(String topic, String startTime, String endTime){
	ResultSet rs = session.execute(bsGetHashtagsWithTime.bind(topic, startTime, endTime));
	return rs;
	
}

public ResultSet getTopwords(String topic){
	ResultSet rs = session.execute(bsGetTopwords.bind(topic));
	return rs;
}

public ResultSet getTopwordsWithTime(String topic, String startTime, String endTime){
	ResultSet rs = session.execute(bsGetTopwordsWithTime.bind(topic, startTime, endTime));
	return rs;
	
}

public ResultSet getMentions(String topic){
	ResultSet rs = session.execute(bsGetMentions.bind(topic));
	return rs;
}

public ResultSet getMentionsWithTime(String topic, String startTime, String endTime){
	ResultSet rs = session.execute(bsGetMentionsWithTime.bind(topic, startTime, endTime));
	return rs;
	
}

public ResultSet getSentiment(String topic){
	ResultSet rs = session.execute(bsGetSentiment.bind(topic));
	return rs;
}

public ResultSet getSentimentWithTime(String topic, String startTime, String endTime){
	ResultSet rs = session.execute(bsGetSentimentWithTime.bind(topic, startTime, endTime));
	return rs;
	
}




}
