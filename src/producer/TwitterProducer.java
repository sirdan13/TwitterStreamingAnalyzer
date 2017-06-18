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

		long tempoInizioRun = System.currentTimeMillis();
		Graphics.setLF("Windows");
		String [] arguments  = Graphics.readTwitterAuth();

		
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
		int i = 0;
		int j = 0;
		int k = 0;

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
		int previousNTweets = 0;
		 while (true) {
			Status ret = queue.poll();
			if (ret == null) {
				Thread.sleep(100);
				// i++;
			} else {
				Tweet t;
				if(!ret.isRetweet()){
					t = new Tweet (ret.getId(), ret.getText(), ret.getCreatedAt(), ret.isRetweet(), ret.getHashtagEntities(), ret.getUser());
				}
				else{
					t = new Tweet (ret.getId(), ret.getText(), ret.getCreatedAt(), ret.isRetweet(), ret.getRetweetedStatus().getText(), ret.getHashtagEntities(), ret.getUser());
				}

				String regex = "£&€";
				String tweetRecord;
				/*
				 * Compongo la stringa contenente le info dettagliate sul tweet
				 */
				if(ret.isRetweet())
					tweetRecord = ret.getRetweetedStatus().getText()+regex+DateManager.getDate()+regex+ret.getUser().getScreenName()+regex+topic;
				
				else
					tweetRecord = ret.getText()+regex+DateManager.getDate()+regex+ret.getUser().getScreenName()+regex+topic;
				
				//Invia i dati al topic "tweet"
				producer.send(new ProducerRecord<String, String>("tweet", Integer.toString(j), tweetRecord));
				/*
				//Invia i dati al topic "original-text"
				producer.send(new ProducerRecord<String, String>("original-text", Integer.toString(j), t.getText()));*/
				//Invia i dati al topic "processed-text" (necessario per le analisi dei termini più citati)
				producer.send(new ProducerRecord<String, String>("processed-text", Integer.toString(j), t.getProcessedText()));
				//Invia i dati al topic "hashtags"
				for(HashtagEntity ht : ret.getHashtagEntities())
					producer.send(new ProducerRecord<String, String>("hashtags", Integer.toString(i++), ht.getText().toLowerCase()+";"+topic));
				//Invia i dati al topic "mentions"
				if(ret.getUserMentionEntities().length>0)
					for(UserMentionEntity ue : ret.getUserMentionEntities())
						producer.send(new ProducerRecord<String, String>("mentions", Integer.toString(k++), ue.getScreenName()));
				producer.send(new ProducerRecord<String, String>("sentiment", ret.getLang(), t.getProcessedText()+";"+topic));
				tlist.add(t);
				nTweets++;
				j++;
			}
			
			
		 long timeY = System.currentTimeMillis();
		 long updateTime = 20000;
//		 int nTweetsPeriodo = tlist.size()-nTweets;
		 int currentNTweets = nTweets-previousNTweets;
		 if(timeY-timeX>=updateTime && currentNTweets>10){
			 System.out.println("******************");
			 System.out.println("STATISTICS");
			 System.out.println("******************");
			 System.out.println();
			 List<Tuple2<Integer, String>> htFrequencies = Analytics.countHashtag(tlist);
			 System.out.println("Tweet ricevuti dall'inizio: "+nTweets);
			 System.out.println("Tweet ricevuti in questo periodo: "+(currentNTweets));
			 if((currentNTweets)>maxTweets){
				 maxTweets = (currentNTweets);
				 System.out.println("Nuovo record di tweet ricevuti in un solo periodo");
			 }
			 else
				 System.out.println("Record attuale di tweet ricevuti in un solo periodo: "+maxTweets);
			 if((currentNTweets)<minTweets){
				 minTweets=(currentNTweets);
				 System.out.println("Nuovo record negativo di tweet ricevuti in un solo periodo");
			 }
			 else
				 System.out.println("Record negativo attuale di tweet ricevuti in un solo periodo: "+minTweets);
			 double ritmo = (double) (currentNTweets)/((updateTime)/1000.0);
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
		//	 nTweets = tlist.size();
		//	 tweetPeriodo = nTweets;
			 previousNTweets = nTweets;
		 }
		 
	
		 if((timeX+updateTime-timeY)/1000==updateTime/2/1000 && !stampato) {
			 System.out.println(); 
		//	 System.out.println("Aggiornamento tra: "+(timeX+updateTime-timeY)/1000+" secondi");
			 System.out.println("Tempo trascorso dall'inizio: "+(System.currentTimeMillis()-tempoInizioRun)/1000/60+" minuti");
			 System.out.println(); 
			 stampato=true;
		 }

	}

}
	
	public static String convertDate(Date date){
		String converted = date.toString();
		String output;
		output = ""+converted.charAt(converted.length()-4)+converted.charAt(converted.length()-3)+converted.charAt(converted.length()-2)+converted.charAt(converted.length()-1);
		output += "-";
		if(converted.substring(4, 7).equals("Jan"))
			output+="01";
		if(converted.substring(4, 7).equals("Feb"))
			output+="02";
		if(converted.substring(4, 7).equals("Mar"))
			output+="03";
		if(converted.substring(4, 7).equals("Apr"))
			output+="04";
		if(converted.substring(4, 7).equals("May"))
			output+="05";
		if(converted.substring(4, 7).equals("Jun"))
			output+="06";
		if(converted.substring(4, 7).equals("Jul"))
			output+="07";
		if(converted.substring(4, 7).equals("Aug"))
			output+="08";
		if(converted.substring(4, 7).equals("Sep"))
			output+="09";
		if(converted.substring(4, 7).equals("Oct"))
			output+="10";
		if(converted.substring(4, 7).equals("Nov"))
			output+="11";
		if(converted.substring(4, 7).equals("Dec"))
			output+="12";
		output+="-";
		output+=converted.substring(8, 10);
		output+=" ";
		output+=converted.substring(11, 19);
		return output;
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

	
	@SuppressWarnings("unused")
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
	

	
}