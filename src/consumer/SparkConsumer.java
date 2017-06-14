package consumer;

import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Scanner;
import java.util.HashMap;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import analysis.Analytics;
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
	static String master = "";
	static String appName = "";
	static String zookeeper_server = "";
	static String kafka_consumer_group = "";
	static String threads = "";
	static long duration;
	static String topic = "";
	static Map<String, Integer> topics;

	
	public static void main(String[] args) throws FileNotFoundException, InterruptedException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		
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
		 * Carico le proprietà di Spark e inizializzo le variabili corrispondenti
		 */
		loadProperties();
		init();
		
		/*
		 * Creo un oggetto di classe Analytics, passandogli i parametri di Spark
		 * e chiamo il suo metodo analyzeTopic, per analizzare il topic desiderato
		 */
		Analytics analytics = new Analytics(jssc, zookeeper_server, kafka_consumer_group, topics);
		analytics.analyzeTopic(topic);

	}
	
	private static void init() {
		conf = new SparkConf().setAppName(appName).setMaster(master);
		jssc = new JavaStreamingContext(conf, new Duration(duration));
		topics = new HashMap<String, Integer>();
		topics.put(topic, Integer.parseInt(threads));
		Logger.getLogger("org").setLevel(Level.ERROR);
		Logger.getLogger("akka").setLevel(Level.ERROR);
	}

	private static void loadProperties() throws FileNotFoundException{
		Scanner sc = new Scanner(new File("config/spark_conf.txt"));
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
		 String [] options = {"hashtags2", "mentions", "original-text", "processed-text", "sentiment"};
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
