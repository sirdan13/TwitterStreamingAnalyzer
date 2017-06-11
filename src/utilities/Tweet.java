package utilities;
import java.text.Normalizer;
import java.text.Normalizer.Form;
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
	private boolean isRetweet;
	private transient HashtagEntity [] hashtags;
	private List<String> hashtagsList;
	private User user;
	
	private String processedText;
	
	public Tweet(){
		
	}
	
	public Tweet(long tweet_id, String text, Date created_at, boolean retweet, HashtagEntity[] hashtags, User user) {
		super();
		this.tweet_id = tweet_id;
		this.text = text;
		this.created_at = created_at;
		this.isRetweet = retweet;
		this.hashtags = hashtags;
		this.user = user;
		hashtagsList = new ArrayList<String>();
		for(HashtagEntity h : hashtags)
			hashtagsList.add(h.getText().toLowerCase());
		processText();
	}
	
	
	public void processText(){
		//Rimuove il termine "RT" che indica il retweet
		if(isRetweet())
			processedText = text.substring(3);
		else
			processedText=text;
		//Rimuove i ritorni a capo 
		processedText=processedText.replaceAll("(\\r|\\n)", "");
		//Rimuove i link
		processedText=processedText.replaceAll("https?://[^ ]*", "");
		//Rimuove gli accenti
		processedText = removeAccents(processedText);
		//Tutto minuscolo
		processedText=processedText.toLowerCase();
		//Rimuove caratteri non alfanumerici (# e @ escluso)
		processedText=processedText.replaceAll("[^a-zA-Z0-9#@]", " ");
		//Separa l'hashtag dalla parola precedente
		processedText=processedText.replaceAll("#", " #");
		//Separa la menzione dalla parola precedente
		processedText=processedText.replaceAll("@", " @");
		//Rimuove gli spazi extra
		processedText=processedText.replaceAll("  *", " ");
		//Rimuove gli spazi all'inizio del testo
		processedText=processedText.replaceAll("^ ", "");
		//Rimuove termini indesiderati
	//	processedText=removeStopWords(processedText);
	}
	
	//TODO implementare metodo che rimuove le stopwords dal testo del tweet
		private String removeStopWords(String text) {
			
			return null;
		}
		
		public static String removeAccents(String text) {
		    return text == null ? null :
		        Normalizer.normalize(text, Form.NFD)
		            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
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
		return isRetweet;
	}

	public void setRetweet(boolean retweet) {
		this.isRetweet = retweet;
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
	
	public String getProcessedText() {
		return processedText;
	}

	public void setProcessedText(String processedText) {
		this.processedText = processedText;
	}

	
}
