package analysis;

import java.util.List;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;
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
	
	
//	PreparedStatement psSelezionaTraccia = s.prepare(cql);
//	BoundStatement bsSelezionaTraccia = new BoundStatement(psSelezionaTraccia);
	
	public CassandraManager(String contactPoint, String user, String password){
		builder = Cluster.builder().withCredentials(user, password);
		builder.addContactPoint(contactPoint).addContactPoint("gpu.sta.uniroma1.it");
		cluster=CassandraManager.builder.build();
		session= cluster.connect(user);
		prepareStatements();
	}
	
	public CassandraManager(List<String> contactPoints, String user, String password){
		builder = Cluster.builder().withCredentials(user, password);
		for(String cp : contactPoints)
			builder.addContactPoint(cp);
		cluster=CassandraManager.builder.build();
		session= cluster.connect("gbd2017_twitteranalysis");
		prepareStatements();
	}
	
	public static void prepareStatements(){
		
		psInsertHashtag = session.prepare("INSERT INTO hashtags (id, text, frequence) VALUES (uuid(), ?, ?)");
		bsInsertHashtag = new BoundStatement(psInsertHashtag);
		
		psInsertTweet = session.prepare("INSERT INTO tweet (id, tweet_id, text, user_id, created_at, retweet, location, lang, topic) VALUES (uuid(), ?, ?, ?, ?, ?, ?, ?, ?)");
		bsInsertTweet = new BoundStatement(psInsertTweet);
	}
	
	public void insertHashtag(String hashtag, Integer freq){
		
		session.execute(bsInsertHashtag.bind(hashtag, freq));
	}
	
public void insertTweet(String tweet_id, String text, String user_id, String created_at, String retweet, String location, String language, String topic){
		
		session.execute(bsInsertTweet.bind(tweet_id, text, user_id, created_at, retweet, location, language, topic));
	}

}
