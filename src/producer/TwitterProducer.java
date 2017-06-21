package producer;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;


import twitter4j.FilterQuery;
import twitter4j.HashtagEntity;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.UserMentionEntity;
import twitter4j.conf.ConfigurationBuilder;
import utilities.DateManager;
import utilities.Tweet;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import analysis.Analytics;
import graphics.Graphics;
import scala.Tuple2;



public class TwitterProducer {
	
	public static long lastLimitation = System.currentTimeMillis();
	
	public static void main(String[] args) throws Exception {
		
		final LinkedBlockingQueue<Status> queue = new LinkedBlockingQueue<Status>(1000);

		//Imposto la grafica (Look and Feel) secondo il tema di nome "Windows"
		Graphics.setLF("Windows");
		
		//Recupero le credenziali di accesso a dev.twitter.com
		String [] arguments  = Graphics.readTwitterAuth();

		//Inserisco in una variabile di configurazione le credenziali di autenticazione a dev.twitter.com
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey(arguments[0]).setOAuthConsumerSecret(arguments[1]).setOAuthAccessToken(arguments[2]).setOAuthAccessTokenSecret(arguments[3]);
		
		//Inizializzo un oggetto TwitterStream a partire dalle credenziali appena fornite
		TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
		StatusListener listener = new StatusListener() {
		
			@Override
			public void onStatus(Status status) {
				queue.offer(status);
			}

			@Override
			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
				System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
			}

			@Override
			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
				if(System.currentTimeMillis()-TwitterProducer.lastLimitation>=30000){
					System.out.println();
					System.out.println("Got track limitation notice: " + numberOfLimitedStatuses);
					System.out.println();
					TwitterProducer.lastLimitation=System.currentTimeMillis();
				}
					
			}

			@Override
			public void onScrubGeo(long userId, long upToStatusId) {
				System.out.println("Got scrub_geo event userId:" + userId + "upToStatusId:" + upToStatusId);
			}

			@Override
			public void onStallWarning(StallWarning warning) {
				System.out.println("Got stall warning:" + warning);
			}

			@Override
			public void onException(Exception ex) {
				ex.printStackTrace();
			}
		};

		FilterQuery query = new FilterQuery();
		
		List<Object> params = Graphics.insertMultipleValues();
		String [] lang = (String[]) params.get(1);
		String [] keyWords = (String[]) params.get(0);
		String topic = (String) params.get(2);
		query.track(keyWords);
		query = importLanguagesInQuery(query, lang);
		
		twitterStream.addListener(listener);
		
	//	query.language("it", "en");
		twitterStream.filter(query);
		
		Properties props = new Properties();
		
		/*
		 * Inserisco le proprietà necessarie nell'oggetto props:
		 * - metadata.broker.list:	lista dei nodi su cui lavora Kafka
		 * - bootstrap.servers: 	lista dei nodi su cui lavora Zookeeper (entrambi lavorano in locale in questo caso)
		 * - acks: 					configurazione che stabilisce secondo quale criterio le richieste sono considerate complete
		 * - retries: 				numero di tentativi permessi al producer quando vi sono fallimenti (ripetizioni possono creare messaggi duplicati)
		 * - batch.size:			ampiezza dei batch contenenti record pronti per essere inviati, ma ancora non inviati
		 * - linger.ms:				millisecondi di ritardo che vengono concessi al producer prima di inviare il contenuto del buffer (con zero si invia tutto immediatamente)
		 * - buffer.memory:			memoria totale concessa al producer per le operazioni di buffering
		 * - key.serializer:		scelta del serializzatore che invia i dati K (chiave) sotto forma di array di byte
		 * - value.serializer:		scelta del serializzatore che invia i dati V (valore) sotto forma di array di byte
		 */
		
		props.put("metadata.broker.list", "localhost:2181");
		props.put("bootstrap.servers", "localhost:9092");
		
		props.put("acks", "all");
		props.put("retries", 0);
		props.put("batch.size", 16384);
		props.put("linger.ms", 20);
		props.put("buffer.memory", 33554432);
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		

		Producer<String, String> producer = new KafkaProducer<String, String>(props);
		
		int i = 0, j = 0, k = 0;
		int nTweets = 0;
		
		Analytics.init();
		
		long lastUpdate = System.currentTimeMillis();
		
		while (true) {
			Status status = queue.poll();
			if (status == null) {
				//Se lo status appena ricevuto è nullo, stoppiamo il processo per 0.1 secondi in attesa di dati non nulli
				Thread.sleep(100);
	
			} else {
				//Questo oggetto di classe Tweet mi è necessario per poter processare il suo testo e ripulirlo da tutto ciò che non ci interessa
				Tweet t;
				
				if(!status.isRetweet())
					t = new Tweet(status.getText(), status.isRetweet());
				else
					t = new Tweet(status.getRetweetedStatus().getText(), status.isRetweet());

				String separatore = "£&€";
				String tweetRecord;

				//Compongo la stringa contenente le info dettagliate sul tweet, distinguendo i tweet dai retweet
				if(status.isRetweet())
					tweetRecord = status.getRetweetedStatus().getText()+separatore+DateManager.getDate()+separatore+status.getRetweetedStatus().getUser().getScreenName()+separatore+topic+separatore+status.getRetweetedStatus().getFavoriteCount()+separatore+status.getRetweetedStatus().getRetweetCount();
				
				else
					tweetRecord = status.getText()+separatore+DateManager.getDate()+separatore+status.getUser().getScreenName()+separatore+topic+separatore+status.getFavoriteCount()+separatore+status.getRetweetCount();
				
				//Invia i dati al topic "tweet"
				producer.send(new ProducerRecord<String, String>("tweet", Integer.toString(j), tweetRecord));
				//Invia i dati al topic "processed-text" (necessario per le analisi dei termini più citati)
				producer.send(new ProducerRecord<String, String>("top-words", Integer.toString(j), t.getProcessedText()+";"+topic));
				//Invia i dati al topic "hashtags"
				for(HashtagEntity ht : status.getHashtagEntities())
					producer.send(new ProducerRecord<String, String>("hashtags", Integer.toString(i++), ht.getText().toLowerCase()+";"+topic));
				//Invia i dati al topic "mentions"
				if(status.getUserMentionEntities().length>0)
					for(UserMentionEntity ue : status.getUserMentionEntities())
						producer.send(new ProducerRecord<String, String>("mentions", Integer.toString(k++), ue.getScreenName()+";"+topic));
				producer.send(new ProducerRecord<String, String>("sentiment", status.getLang(), t.getProcessedText()+";"+topic));
				
				if(System.currentTimeMillis()-lastUpdate>=10000){
					System.out.println("Tweet ricevuti: "+nTweets);
					lastUpdate = System.currentTimeMillis();
				}
					
				nTweets++;
				j++;
			}
			
	}

}
	
	
	
	private static FilterQuery importLanguagesInQuery(FilterQuery query, String[] lang) {
		if(lang.length==0)
			return query.language();
		if(lang.length==1)
			return query.language(lang[0]);
		if(lang.length==2)
			return query.language(lang[0], lang[1]);
		if(lang.length==3)
			return query.language(lang[0], lang[1], lang[2]);
		if(lang.length==4)
			return query.language(lang[0], lang[1], lang[2], lang[3]);
		if(lang.length==5)
			return query.language(lang[0], lang[1], lang[2], lang[3], lang[4]);
		if(lang.length==6)
			return query.language(lang[0], lang[1], lang[2], lang[3], lang[4], lang[5]);
		if(lang.length==7)
			return query.language(lang[0], lang[1], lang[2], lang[3], lang[4], lang[5], lang[6]);
		if(lang.length==8)
			return query.language(lang[0], lang[1], lang[2], lang[3], lang[4], lang[5], lang[6], lang[7]);
		return null;
		
	}

	
	public static String [] readTwitterAuth(String file) throws FileNotFoundException{
		String [] output = new String[4];
		Scanner sc = new Scanner(new File(file));
		int count = 0;
		while(sc.hasNextLine()){
			output[count++] = sc.nextLine();
		}
		sc.close();
		return output;
	}
	

	
}