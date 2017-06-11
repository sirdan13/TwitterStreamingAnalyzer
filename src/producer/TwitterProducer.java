package producer;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;

import twitter4j.*;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;
import utilities.Tweet;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import analysis.Analytics;
import scala.Tuple2;



public class TwitterProducer {
	public static long lastLimitation = System.currentTimeMillis();

	public static void main(String[] args) throws Exception {
		final LinkedBlockingQueue<Status> queue = new LinkedBlockingQueue<Status>(1000);

		long tempoInizioRun = System.currentTimeMillis();
		String topicName = readTopic("config/topic.txt");
		String [] arguments = readTwitterAuth("config/credenziali_twitter.txt");
		String [] keyWords = readKeywords("config/keywords.txt");
		
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey(arguments[0]).setOAuthConsumerSecret(arguments[1])
				.setOAuthAccessToken(arguments[2]).setOAuthAccessTokenSecret(arguments[3]);
		
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
		twitterStream.addListener(listener);


		FilterQuery query = new FilterQuery().track(keyWords);

	//	query.language("it");
		query.language("it", "en");
		twitterStream.filter(query);


		Properties props = new Properties();
		props.put("metadata.broker.list", "localhost:2181");
		props.put("bootstrap.servers", "localhost:9092");
		props.put("acks", "all");
		props.put("retries", 0);
		props.put("batch.size", 16384);
		props.put("linger.ms", 1);
		props.put("buffer.memory", 33554432);

		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		
	//	props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
	//	props.put("value.serializer", "utilities.TweetSerializer");

		Producer<String, String> producer = new KafkaProducer<String, String>(props);
	//	int i = 0;
		int j = 0;

		/*
		 * Problema con l'encoding del file?
		 */
	//	System.out.println(System.getProperty("file.encoding"));
	//	System.setProperty("file.encoding", "UTF-8");
	//	System.out.println(System.getProperty("file.encoding"));
		 
		Analytics.init();
		 
		List<Tweet> tlist = new ArrayList<Tweet>();
		long timeX = System.currentTimeMillis();
		boolean stampato = false;
		double vecchioRitmo = 0.0;
		double maxRitmo = 0.0;
		double minRitmo = 100000.0;
		int nTweets = 0;
		int maxTweets = 0;
		int minTweets = 100000;
		 while (true) {
			Status ret = queue.poll();

			if (ret == null) {
				Thread.sleep(100);
				// i++;
			} else {
				Tweet t = new Tweet (ret.getId(), ret.getText(), ret.getCreatedAt(), ret.isRetweet(), ret.getHashtagEntities(), ret.getUser());
		//		producer.send(new ProducerRecord<String, String>(topicName, Integer.toString(j++), t.getText()));
				producer.send(new ProducerRecord<String, String>(topicName, Integer.toString(j++), t.getProcessedText()));
				tlist.add(t);
				if(t.isRetweet())
					System.out.println(t.getProcessedText());
			}
			
		 long timeY = System.currentTimeMillis();
		 long updateTime = 20000;
		 int nTweetsPeriodo = tlist.size()-nTweets;
		 if(timeY-timeX>=updateTime && nTweetsPeriodo>10){
			 System.out.println("******************");
			 System.out.println("STATISTICS");
			 System.out.println("******************");
			 System.out.println();
			 List<Tuple2<Integer, String>> htFrequencies = Analytics.countHashtag(tlist);
			 System.out.println("Tweet ricevuti dall'inizio: "+tlist.size());
			 System.out.println("Tweet ricevuti in questo periodo: "+(nTweetsPeriodo));
			 if((nTweetsPeriodo)>maxTweets){
				 maxTweets = (nTweetsPeriodo);
				 System.out.println("Nuovo record di tweet ricevuti in un solo periodo");
			 }
			 else
				 System.out.println("Record attuale di tweet ricevuti in un solo periodo: "+maxTweets);
			 if((nTweetsPeriodo)<minTweets){
				 minTweets=(nTweetsPeriodo);
				 System.out.println("Record negativo di tweet ricevuti in un solo periodo");
			 }
			 else
				 System.out.println("Record negativo attuale di tweet ricevuti in un solo periodo: "+minTweets);
			 double ritmo = (double) (nTweetsPeriodo)/((updateTime)/1000.0);
			 ritmo = round(ritmo, 2);
			 System.out.println("Ritmo di acquisizione: "+ritmo+" T/s");
			 if(vecchioRitmo>ritmo)
				 System.out.println("Differenza dal periodo precedente: -"+(round((vecchioRitmo-ritmo), 2)+" T/s"));
			 if(ritmo>vecchioRitmo)
				 System.out.println("Differenza dal periodo precedente: +"+(round((ritmo-vecchioRitmo), 2)+" T/s"));
			 vecchioRitmo = ritmo;
			 if(ritmo>maxRitmo){
				 System.out.println("Nuovo record di Tweet al secondo: "+ritmo+" T/s");
				 maxRitmo=ritmo;
			 }
			 else
				 System.out.println("Massimo numero di Tweet al secondo registrato finora: "+maxRitmo+" T/s");
			 
			 if(ritmo<minRitmo){
				 System.out.println("Record negativo per il numero di Tweet al secondo: "+ritmo+" T/s");
				 minRitmo=ritmo;
			 }
			 else
				 System.out.println("Minor numero di Tweet al secondo registrato finora: "+minRitmo+" T/s");
			 System.out.println();
			 System.out.println("******************");
			 System.out.println("HASHTAGS");
			 System.out.println("******************");
			 System.out.println();
			 for(Tuple2<Integer, String> ht : htFrequencies)
				 System.out.println("L'hashtag #"+ht._2+" e' stato citato\t"+ht._1+" volte.");
			 timeX=System.currentTimeMillis();
			 stampato=false;
			 nTweets = tlist.size();
		 }
		 
	
		 if((timeX+updateTime-timeY)/1000==updateTime/2/1000 && !stampato) {
			 System.out.println(); 
		//	 System.out.println("Aggiornamento tra: "+(timeX+updateTime-timeY)/1000+" secondi");
			 System.out.println("Tempo trascorso dall'inizio: "+(System.currentTimeMillis()-tempoInizioRun)/1000/60+" minuti");
			 System.out.println(); 
			 stampato=true;
		 }
			
	//	 producer.close();
		// Thread.sleep(500);
		// twitterStream.shutdown();
	}

}
	
	private static String readTopic(String file) throws FileNotFoundException {
		Scanner sc = new Scanner(new File(file));
		String topic = sc.nextLine();
		sc.close();
		return topic;
	}

	private static String[] readKeywords(String file) throws FileNotFoundException {
		List<String> keywords = new ArrayList<String>();
		Scanner sc = new Scanner(new File(file));
		while(sc.hasNextLine()){
			keywords.add(sc.nextLine());
		}
		sc.close();
		return keywords.toArray(new String[keywords.size()]);
		
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
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}
	
	//TODO implementare metodo che debba essere chiamato una sola volta (all'inizio del main) e che carichi la lista delle stopwords
		public static void loadStopWords(String file){
			
		}
	
}