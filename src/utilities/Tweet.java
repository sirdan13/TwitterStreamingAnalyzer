package utilities;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import scala.Serializable;
import twitter4j.HashtagEntity;
import twitter4j.User;

public class Tweet implements Serializable {
	
	private long tweet_id;
	private String text;
	private Date created_at;
	private boolean retweet;
	private transient HashtagEntity [] hashtags;
	private List<String> hashtagsList;
	private User user;
	
	public Tweet(){
		
	}
	
	public Tweet(long tweet_id, String text, Date created_at, boolean retweet, HashtagEntity[] hashtags, User user) {
		super();
		this.tweet_id = tweet_id;
		this.text = text;
		this.created_at = created_at;
		this.retweet = retweet;
		this.hashtags = hashtags;
		this.user = user;
		hashtagsList = new ArrayList<String>();
		for(HashtagEntity h : hashtags)
			hashtagsList.add(h.getText().toLowerCase());
	}

	public long getTweet_id() {
		return tweet_id;
	}

	public void setTweet_id(long tweet_id) {
		this.tweet_id = tweet_id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Date getCreated_at() {
		return created_at;
	}

	public void setCreated_at(Date created_at) {
		this.created_at = created_at;
	}

	public boolean isRetweet() {
		return retweet;
	}

	public void setRetweet(boolean retweet) {
		this.retweet = retweet;
	}

	public HashtagEntity[] getHashtags() {
		return hashtags;
	}

	public void setHashtags(HashtagEntity[] hashtags) {
		this.hashtags = hashtags;
	}

	public List<String> getHashtagsList() {
		return hashtagsList;
	}

	public void setHashtagsList(List<String> hashtagsList) {
		this.hashtagsList = hashtagsList;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	
}
