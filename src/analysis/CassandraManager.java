package analysis;

import java.util.List;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;

import utilities.DateManager;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;

public class CassandraManager {
	
	//Connessione a Cassandra
	static Builder builder;
	static Cluster cluster;
	static Session session;
	static int j = 0;
	
	//Queries
	static BoundStatement bsInsertHashtag;
	static BoundStatement bsInserTText;
	static PreparedStatement psInsertText;
	static PreparedStatement psInsertHashtag;
	static BoundStatement bsInsertTweet;
	static PreparedStatement psInsertTweet;
	static BoundStatement bsInsertSentiment;
	static PreparedStatement psInsertSentiment;
	
	
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
		
	//	psInsertTweet = session.prepare("INSERT INTO tweet (tweet_id, text, user_id, created_at, lang, topic) VALUES (?, ?, ?, ?, ?, ?)");
	//	psInsertTweet = session.prepare("INSERT INTO tweet (tweet_id, text, user_id, created_at, topic) VALUES (?, ?, ?, ?, ?)");
		psInsertTweet = session.prepare("INSERT INTO tweet (text, created_at, user_name, topic) VALUES (?, ?, ?, ?)");
		bsInsertTweet = new BoundStatement(psInsertTweet);
		
		psInsertSentiment = session.prepare("insert into sentiment(created_at, like, angry, hilarious, sad, neutral, topic) values (?, ?, ?, ?, ?, ?, ?)");
		bsInsertSentiment = new BoundStatement(psInsertSentiment);
	}
	
	public void insertHashtag(String hashtag, Integer freq){
		
		session.execute(bsInsertHashtag.bind(DateManager.getDate(), hashtag.split(";")[0], freq, hashtag.split(";")[1]));
	}
	
public void insertTweet(String text, String user_name, String created_at, String topic){
		
	//	session.execute(bsInsertTweet.bind(tweet_id, text, user_id, created_at, language, topic));
	//	session.execute(bsInsertTweet.bind(text, created_at, topic));
	session.execute(bsInsertTweet.bind(text, user_name, created_at, topic));
	
	}

public void insertSentiment(double like, double angry, double hilarious, double sad, int neutral, String topic) {
	
		session.execute(bsInsertSentiment.bind(DateManager.getDate(), like, angry, hilarious, sad, neutral, topic));
	}




}
