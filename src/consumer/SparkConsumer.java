package consumer;

import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import analysis.Analytics;
import analysis.CassandraManager;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.ColorUIResource;
public class SparkConsumer {
	
	static Icon icon = new ImageIcon("config/icon.png");
	static SparkConf conf;
	static JavaStreamingContext jssc;
	static JavaSparkContext jsc;
	static String master = "";
	static String appName = "";
	static String zookeeper_server = "";
	static String kafka_consumer_group = "";
	static String threads = "";
	static long duration;
	static String topic = "";
	static Map<String, Integer> topics;

	
	public static void main(String[] args) throws FileNotFoundException, InterruptedException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, SQLException {
		
    	/*
    	 * Imposto il Look and Feel per la grafica delle finestre interattive
    	 */
    	setGraphic("Windows");
    	
    	/*
    	 * Scelgo il topic da analizzare
    	 */
    	chooseTopic();
		while(topic==null){
			if(noTopicChosen()==0){
				if(chooseTopic()!=null)
					break;
			}
			else
				System.exit(-1);
		}
		
		/*
		 * Carico le proprietÓ di Spark e inizializzo le variabili corrispondenti
		 */
		loadProperties();
		init();
		
		/*
		 * Importo gli indirizzi degli host di Cassandra, importo le credenziali di accesso alla connessione a Cassandra
		 * Creo un oggetto di classe CassandraManager, per inizializzare al connessione
		 * Infine creo un oggetto di classe Analytics per iniziare l'analisi del topic desiderato
		 */
		List<String> hosts = readCassandraHosts("config/cassandra_hosts.txt");
		List<String> credenzialiCassandra = readCassandraCredentials("config/credenziali_cassandra.txt");
		CassandraManager cm = new CassandraManager(hosts, credenzialiCassandra.get(0), credenzialiCassandra.get(1), jsc);
		Analytics analytics = new Analytics(jssc, zookeeper_server, kafka_consumer_group, topics, cm);
		analytics.analyzeTopic(topic);

	}
	
	public static List<String> readCassandraCredentials(String file) throws FileNotFoundException {
		Scanner sc = new Scanner(new File(file));
		List<String> credenziali = new ArrayList<String>();  
		while(sc.hasNextLine())
			credenziali.add(sc.nextLine().split("=")[1]);
		sc.close();
		return credenziali;
	}

	public static List<String> readCassandraHosts(String file) throws FileNotFoundException {
		Scanner sc = new Scanner(new File(file));
		List<String> hosts = new ArrayList<String>();  
		while(sc.hasNextLine())
			hosts.add(sc.nextLine());
		sc.close();
		return hosts;
	}

	private static void init() {
		conf = new SparkConf().setAppName(appName).setMaster(master).set("spark.driver.allowMultipleContexts", "true");
		jsc = new JavaSparkContext(conf);
		jssc = new JavaStreamingContext(jsc, new Duration(duration));
		topics = new HashMap<String, Integer>();
		topics.put(topic, Integer.parseInt(threads));
		Logger.getLogger("org").setLevel(Level.ERROR);
		Logger.getLogger("akka").setLevel(Level.ERROR);
	}

	private static void loadProperties() throws FileNotFoundException{
		Scanner sc = new Scanner(new File("config/spark_streaming_conf.txt"));
		int count = 0;
		while(sc.hasNextLine()){
			if(count==0)
				appName = sc.nextLine().split("=")[1];
			if(count==1)
				master = sc.nextLine().split("=")[1];
			if(count==2)
				zookeeper_server = sc.nextLine().split("=")[1];
			if(count==3)
				kafka_consumer_group = sc.nextLine().split("=")[1];
			if(count==4)
				threads = sc.nextLine().split("=")[1];
			if(count==5)
				duration = Long.parseLong(sc.nextLine().split("=")[1]);
			count++;
			}
		sc.close();

	}
	
	private static String chooseTopic(){
		 String [] options = {"tweet", "hashtags", "mentions", "top-words", "sentiment"};
		 UIManager.put("OptionPane.background", new ColorUIResource(214,227,249));
		 UIManager.put("Panel.background",new ColorUIResource(214,227,249));
		 Dimension size = UIManager.getDimension("OptionPane.minimumSize");
		 size.width = 350;
		 size.height= 220;
		 UIManager.put("OptionPane.minimumSize", size);
		 JLabel label = new JLabel("Scegli un topic:");
		 label.setFont(new Font("Calibri", Font.BOLD, 20));
		 topic = (String) JOptionPane.showInputDialog(null, label, "Topic", 3, icon, options, options[0]);
		 return topic;
	}
	
	private static int noTopicChosen() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException{
		JLabel label2 = new JLabel("<html>Nessun topic scelto.<br>Riprovare?</html>");
		label2.setFont(new Font("Calibri", Font.BOLD, 20));
		String [] errorOptions = {"Riprova", "Esci"};
		return JOptionPane.showOptionDialog(null, label2, "Topic", 0, 0, icon, errorOptions, "Riprova");
		}
	
	private static void setGraphic(String lf) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException{
		for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
 	        if (lf.equals(info.getName())) {
 	            UIManager.setLookAndFeel(info.getClassName());
 	            break;
 	        }
 	    }
	}
	
}
