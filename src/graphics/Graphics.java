package graphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.ColorUIResource;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import analysis.CassandraManager;
import producer.TwitterProducer;
import scala.Tuple2;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

public class Graphics {
	
	static Icon icon =  new ImageIcon("config/icon.png");
	static String analisi = "";
	
	static SparkConf conf;
	static String appName;
	static String master;
	static JavaSparkContext jsc;
	
	public Graphics(String fileIcon){
		Graphics.icon = new ImageIcon(fileIcon);
	}
	
	public static List<Object> insertMultipleValues() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException{

		   JTextField key = new JTextField();
		   JTextField lang = new JTextField();
		   JTextField topic = new JTextField();
		   JLabel label = new JLabel();
		   label.setText("Inserire i parametri: ");
		   label.setFont(new Font("Calibri", Font.BOLD, 20));
		   Object[] message = {label, "Keywords:", key, "Languages:", lang, "Topic:", topic};
		   Dimension size = UIManager.getDimension("OptionPane.minimumSize");
		   size.width = 450;
		   size.height= 300;
		   UIManager.put("OptionPane.background", new ColorUIResource(214,227,249));
		   UIManager.put("Panel.background",new ColorUIResource(214,227,249));
		   int option = JOptionPane.showConfirmDialog(null, message, "Producer", JOptionPane.OK_CANCEL_OPTION, 0, icon);
		   
		   while(option != JOptionPane.OK_OPTION || key.getText().length()<1 || lang.getText().length()<2){
					if(noParamsChosen()==0){
						option = JOptionPane.showConfirmDialog(null, message, "Producer", JOptionPane.OK_CANCEL_OPTION, 0, icon);
						if(option == JOptionPane.OK_OPTION && key.getText().length()>1 && lang.getText().length()>=2)
							break;
					}
					else{
						System.exit(-1);
					}
						
				}
		   
		   String [] keywords = key.getText().split(",");
		   String [] languages = lang.getText().split(",");
		   String topicScelto = topic.getText();
		   List<Object> output = new ArrayList<Object>();
		   output.add(keywords);
		   output.add(languages);
		   output.add(topicScelto);
		   return output;
	}
	
	
	public static String [] insertKeywords() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException{
		 setLF("Windows");
		 UIManager.put("OptionPane.background", new ColorUIResource(214,227,249));
		 UIManager.put("Panel.background",new ColorUIResource(214,227,249));
		 Dimension size = UIManager.getDimension("OptionPane.minimumSize");
		 size.width = 350;
		 size.height= 220;
		 UIManager.put("OptionPane.minimumSize", size);
		 JLabel label = new JLabel("Inserire le keywords da monitorare (separate da virgola):");
		 label.setFont(new Font("Calibri", Font.BOLD, 20));
		 String [] topic = JOptionPane.showInputDialog(null, label, "Keywords", 0, Graphics.icon, null, null).toString().split(",");
		 return topic;

	}
	
	
	public static String [] insertLanguages(){
		 
		 UIManager.put("OptionPane.background", new ColorUIResource(214,227,249));
		 UIManager.put("Panel.background",new ColorUIResource(214,227,249));
		 Dimension size = UIManager.getDimension("OptionPane.minimumSize");
		 size.width = 350;
		 size.height= 220;
		 UIManager.put("OptionPane.minimumSize", size);
		 JLabel label = new JLabel("Inserire le lingue da monitorare (separate da virgola):");
		 label.setFont(new Font("Calibri", Font.BOLD, 20));
		 String [] topic = JOptionPane.showInputDialog(null, label, "Lingue", 0, Graphics.icon, null, null).toString().split(",");
		 return topic;

	}
	
	private static int noParamsChosen() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException{
		JLabel label2 = new JLabel("<html>Parametri non inseriti correttamente.<br>Riprovare?</html>");
		label2.setFont(new Font("Calibri", Font.BOLD, 20));
		String [] errorOptions = {"Riprova", "Esci"};
		return JOptionPane.showOptionDialog(null, label2, "Topic", 0, 0, icon, errorOptions, "Riprova");
		}
	
	
	public static void setLF(String lf) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException{
		for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
 	        if (lf.equals(info.getName())) {
 	            UIManager.setLookAndFeel(info.getClassName());
 	            break;
 	        }
 	    }
	}

	public static String[] readTwitterAuth() throws FileNotFoundException {
		UIManager.put("OptionPane.background", new ColorUIResource(214,227,249));
		 UIManager.put("Panel.background",new ColorUIResource(214,227,249));
		 Dimension size = UIManager.getDimension("OptionPane.minimumSize");
		 size.width = 450;
		 size.height= 150;
		 UIManager.put("OptionPane.minimumSize", size);
		 JLabel message = new JLabel();
		 message.setText("Scegliere credenziali dev.twitter.com");
		 message.setFont(new Font("Calibri", Font.BOLD, 20));
		 String [] options = {"(default)", "2", "3"};
		 int scelta = JOptionPane.showOptionDialog(null, message, "Credenziali", 2, 0, icon, options, options[0]);
		 if(scelta==0)
			 return TwitterProducer.readTwitterAuth("config/credenziali_twitter.txt");
		 if(scelta==1)
			 return TwitterProducer.readTwitterAuth("config/credenziali_twitter2.txt");
		 if(scelta==2)
			 return TwitterProducer.readTwitterAuth("config/credenziali_twitter3.txt");
		 return null;
	}
	
	
	public static void mostMentionedUser(String screen_name, Integer frequence) throws TwitterException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException{
		String [] auth = TwitterProducer.readTwitterAuth("config/credenziali_twitter3.txt");
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey(auth[0]).setOAuthConsumerSecret(auth[1])
				.setOAuthAccessToken(auth[2]).setOAuthAccessTokenSecret(auth[3]);
        Twitter twitter = new TwitterFactory(cb.build()).getInstance();
        ResponseList<User> users = twitter.lookupUsers(screen_name);
        User user = users.get(0);
        URL url = new URL(user.getBiggerProfileImageURL());
        BufferedImage img = ImageIO.read(url);
        ImageIcon userIcon = new ImageIcon(img);
        JPanel panel = new JPanel(null);
        JLabel titolo = new JLabel(), nome = new JLabel(), account = new JLabel(), immagine = new JLabel(userIcon), descrizione = new JLabel(), label_citazioni = new JLabel();
        titolo.setSize(800, 80); titolo.setLocation(0, 0); titolo.setFont(new Font("Verdana", Font.PLAIN, 20));
        nome.setSize(500, 28); nome.setLocation(100, 65); nome.setFont(new Font("Verdana", Font.BOLD, 20));
        label_citazioni.setText("Citato in "+frequence.toString()+" tweets");  label_citazioni.setSize(300, 28); label_citazioni.setLocation(400, 65); label_citazioni.setFont(new Font("Verdana", Font.BOLD, 13));
        account.setSize(200, 15); account.setLocation(100, 95); account.setFont(new Font("Verdana", Font.ITALIC, 15));
        descrizione.setSize(1300, 200); descrizione.setLocation(0, 75); descrizione.setFont(new Font("Verdana", Font.ITALIC, 18));
        immagine.setSize(80, 80); immagine.setLocation(0, 65);
        nome.setText(user.getName()); account.setText("@"+user.getScreenName()); titolo.setText("L'utente più citato:"); descrizione.setText(user.getDescription());
        
        panel.add(descrizione); panel.add(immagine); panel.add(account); panel.add(nome); panel.add(titolo); panel.add(label_citazioni);
        
        Graphics.setLF("Windows");
		UIManager.put("OptionPane.background", new ColorUIResource(214,227,249));
		UIManager.put("Panel.background",new ColorUIResource(214,227,249));
		Dimension size = UIManager.getDimension("OptionPane.minimumSize");
		size.width = 1300;
		size.height= 350;
		UIManager.put("OptionPane.minimumSize", size);
		panel.setForeground(new ColorUIResource(214,227,249));
		panel.setBackground(new ColorUIResource(214,227,249));
		String [] options = {"Menù principale", "Esci"};
		int azione = JOptionPane.showOptionDialog(null, panel, "Top User", 2, 0, icon, options, options[0]);
		if(azione==0)
			mainMenu();
		else
			System.exit(-1);
		
        
	}
	
	public static void mainMenu() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, TwitterException, IOException{
		JPanel panel = new JPanel(null);
		JButton button1 = new JButton();
		button1.setText("Conteggio Tweet"); button1.setSize(300, 80); button1.setLocation(100, 200); button1.setFont(new Font("Verdana", Font.ITALIC, 20));
		JButton button2 = new JButton();
		button2.setText("Top #Hashtags"); button2.setSize(300, 80); button2.setLocation(100, 300); button2.setFont(new Font("Verdana", Font.ITALIC, 20));
		JButton button3 = new JButton();
		button3.setText("Top Words"); button3.setSize(300, 80); button3.setLocation(450, 200); button3.setFont(new Font("Verdana", Font.ITALIC, 20));
		JButton button4 = new JButton();
		button4.setText("Top Utenti"); button4.setSize(300, 80); button4.setLocation(450, 300); button4.setFont(new Font("Verdana", Font.ITALIC, 20));
		JButton button5 = new JButton();
		button5.setText("Sentiment analysis"); button5.setSize(300, 80); button5.setLocation(100, 400); button5.setFont(new Font("Verdana", Font.ITALIC, 20));
		JButton button6 = new JButton();
		button6.setText("Tweet più popolari"); button6.setSize(300, 80); button6.setLocation(450, 400); button6.setFont(new Font("Verdana", Font.ITALIC, 20));
		JLabel titolo = new JLabel();
		titolo.setText("Scegliere un'analisi:"); titolo.setSize(650, 80); titolo.setLocation(250, 70); titolo.setFont(new Font("Verdana", Font.BOLD, 30));
		
		ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JButton source = (JButton) e.getSource();
               	analisi = source.getText();
            }
        };
        
        button1.addActionListener(listener);
        button2.addActionListener(listener);
        button3.addActionListener(listener);
        button4.addActionListener(listener);
        button5.addActionListener(listener);
        button6.addActionListener(listener);
		panel.add(button2); panel.add(button1); panel.add(button3); panel.add(button4); panel.add(button5); panel.add(button6); panel.add(titolo);
		
		
		Graphics.setLF("Windows");
		UIManager.put("OptionPane.background", new ColorUIResource(214,227,249));
		UIManager.put("Panel.background",new ColorUIResource(214,227,249));
		Dimension size = UIManager.getDimension("OptionPane.minimumSize");
		size.width = 1000;
		size.height= 700;
		UIManager.put("OptionPane.minimumSize", size);
		panel.setForeground(new ColorUIResource(214,227,249));
		panel.setBackground(new ColorUIResource(214,227,249));
		
		int azione = JOptionPane.showOptionDialog(null, panel, "Twitter", 2, 0, icon, null, null);
		if(azione==1)
			System.exit(-1);
		if(analisi.equals(null))
			System.exit(-1);
		
		if(analisi.equals("Conteggio Tweet")){
			tweetCountAnalysis();
		}
		if(analisi.equals("Top #Hashtags")){
			topHashtagsAnalysis();
		}
		if(analisi.equals("Top Words")){
			topwordsAnalysis();
		}
		if(analisi.equals("Top Utenti")){
			topUsersAnalysis();
		}
		if(analisi.equals("Sentiment analysis")){
			sentimentAnalysis();
		}
		if(analisi.equals("Tweet più popolari")){
			popularTweets();
		}
		
		
	}

	private static void popularTweets() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, TwitterException, IOException {
		
		JTextField topic = new JTextField();
		JTextField oraInizio = new JTextField();
		JTextField oraFine = new JTextField();
		JLabel label = new JLabel();
		label.setText("Inserire i parametri: ");
		label.setFont(new Font("Calibri", Font.BOLD, 20));
		Object[] message = {label, "Inserire l'argomento di interesse:", topic, "Inserire ora di inizio analisi (opzionale):", oraInizio, "Inserire ora di fine analisi (opzionale):", oraFine};
		Dimension size = UIManager.getDimension("OptionPane.minimumSize");
		size.width = 450;
		size.height= 300;
		UIManager.put("OptionPane.background", new ColorUIResource(214,227,249));
		UIManager.put("Panel.background",new ColorUIResource(214,227,249));
		String [] options = {"OK", "Indietro", "Esci"};
		int option = JOptionPane.showOptionDialog(null, message, "Tweet popolari", 0, 0, icon, options, options[0]);
		if(option==1)
			mainMenu();
		else
			if(option==2)
				System.exit(-1);
		while(topic.getText().length()==0){
			label.setText("Parametri errati. Riprovare: ");
			option = JOptionPane.showOptionDialog(null, message, "Tweet popolari", 0, 0, icon, options, options[0]);
			if(option==1){
				mainMenu();
				break;
			}
			else
				if(option==2)
					System.exit(-1);
		}


		if(oraInizio.getText().length()>0){
			if(oraFine.getText().length()>0)
				CassandraManager.getTweetsWithTimeManager(topic.getText(), oraInizio.getText(), oraFine.getText());
		}
			
		
		else
			CassandraManager.getTweetsManager(topic.getText());
		
	}

	private static void sentimentAnalysis() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, TwitterException, IOException {
		JTextField topic = new JTextField();
		JTextField oraInizio = new JTextField();
		JTextField oraFine = new JTextField();
		JLabel label = new JLabel();
		label.setText("Inserire i parametri: ");
		label.setFont(new Font("Calibri", Font.BOLD, 20));
		Object[] message = {label, "Inserire l'argomento di interesse:", topic, "Inserire ora di inizio analisi (opzionale):", oraInizio, "Inserire ora di fine analisi (opzionale):", oraFine};
		Dimension size = UIManager.getDimension("OptionPane.minimumSize");
		size.width = 450;
		size.height= 300;
		UIManager.put("OptionPane.background", new ColorUIResource(214,227,249));
		UIManager.put("Panel.background",new ColorUIResource(214,227,249));
		String [] options = {"OK", "Indietro", "Esci"};
		int option = JOptionPane.showOptionDialog(null, message, "Sentiment Analysis", 0, 0, icon, options, options[0]);
		if(option==1)
			mainMenu();
		else
			if(option==2)
				System.exit(-1);
		while(topic.getText().length()==0){
			label.setText("Parametri errati. Riprovare: ");
			option = JOptionPane.showOptionDialog(null, message, "Sentiment Analysis", 0, 0, icon, options, options[0]);
			if(option==1){
				mainMenu();
				break;
			}
			else
				if(option==2)
					System.exit(-1);
		}
		
	}

	private static void topUsersAnalysis() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, TwitterException, IOException {
		JTextField topic = new JTextField();
		JTextField oraInizio = new JTextField();
		JTextField oraFine = new JTextField();
		JLabel label = new JLabel();
		label.setText("Inserire i parametri: ");
		label.setFont(new Font("Calibri", Font.BOLD, 20));
		Object[] message = {label, "Inserire l'argomento di interesse:", topic, "Inserire ora di inizio analisi (opzionale):", oraInizio, "Inserire ora di fine analisi (opzionale):", oraFine};
		Dimension size = UIManager.getDimension("OptionPane.minimumSize");
		size.width = 450;
		size.height= 300;
		UIManager.put("OptionPane.background", new ColorUIResource(214,227,249));
		UIManager.put("Panel.background",new ColorUIResource(214,227,249));
		String [] options = {"OK", "Indietro", "Esci"};
		int option = JOptionPane.showOptionDialog(null, message, "Top Users", 0, 0, icon, options, options[0]);
		if(option==1)
			mainMenu();
		else
			if(option==2)
				System.exit(-1);
		while(topic.getText().length()==0){
			label.setText("Parametri errati. Riprovare: ");
			option = JOptionPane.showOptionDialog(null, message, "Top Users", 0, 0, icon, options, options[0]);
			if(option==1){
				mainMenu();
				break;
			}
			else
				if(option==2)
					System.exit(-1);
		}
		
		if(oraInizio.getText().length()>0){
			if(oraFine.getText().length()>0)
				CassandraManager.getTopUserWithTimeManager(topic.getText(), oraInizio.getText(), oraFine.getText());
		}
			
		
		else
			CassandraManager.getTopUserManager(topic.getText());
		
		
	}

	private static void topwordsAnalysis() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, TwitterException, IOException {
		JTextField topic = new JTextField();
		JTextField oraInizio = new JTextField();
		JTextField oraFine = new JTextField();
		JLabel label = new JLabel();
		label.setText("Inserire i parametri: ");
		label.setFont(new Font("Calibri", Font.BOLD, 20));
		Object[] message = {label, "Inserire l'argomento di interesse:", topic, "Inserire ora di inizio analisi (opzionale):", oraInizio, "Inserire ora di fine analisi (opzionale):", oraFine};
		Dimension size = UIManager.getDimension("OptionPane.minimumSize");
		size.width = 450;
		size.height= 300;
		UIManager.put("OptionPane.background", new ColorUIResource(214,227,249));
		UIManager.put("Panel.background",new ColorUIResource(214,227,249));
		String [] options = {"OK", "Indietro", "Esci"};
		int option = JOptionPane.showOptionDialog(null, message, "Top words", 0, 0, icon, options, options[0]);
		if(option==1)
			mainMenu();
		else
			if(option==2)
				System.exit(-1);
		while(topic.getText().length()==0){
			label.setText("Parametri errati. Riprovare: ");
			option = JOptionPane.showOptionDialog(null, message, "Top words", 0, 0, icon, options, options[0]);
			if(option==1){
				mainMenu();
				break;
			}
			else
				if(option==2)
					System.exit(-1);
		}
		
		if(oraInizio.getText().length()>0){
			if(oraFine.getText().length()>0)
				CassandraManager.getTopWordsManagerWithTime(topic.getText(), oraInizio.getText(), oraFine.getText());
		}
			
		
		else
			CassandraManager.getTopWordsManager(topic.getText());
		
		
	}

	private static void topHashtagsAnalysis() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, TwitterException, IOException {
		JTextField topic = new JTextField();
		JTextField oraInizio = new JTextField();
		JTextField oraFine = new JTextField();
		JLabel label = new JLabel();
		label.setText("Inserire i parametri: ");
		label.setFont(new Font("Calibri", Font.BOLD, 20));
		Object[] message = {label, "Inserire l'argomento di interesse:", topic, "Inserire ora di inizio analisi (opzionale):", oraInizio, "Inserire ora di fine analisi (opzionale):", oraFine};
		Dimension size = UIManager.getDimension("OptionPane.minimumSize");
		size.width = 450;
		size.height= 300;
		UIManager.put("OptionPane.background", new ColorUIResource(214,227,249));
		UIManager.put("Panel.background",new ColorUIResource(214,227,249));
		String [] options = {"OK", "Indietro", "Esci"};
		int option = JOptionPane.showOptionDialog(null, message, "Top Hashtags", 0, 0, icon, options, options[0]);
		if(option==1)
			mainMenu();
		else
			if(option==2)
				System.exit(-1);
		while(topic.getText().length()==0){
			label.setText("Parametri errati. Riprovare: ");
			option = JOptionPane.showOptionDialog(null, message, "Top Hashtags", 0, 0, icon, options, options[0]);
			if(option==1){
				mainMenu();
				break;
			}
			else
				if(option==2)
					System.exit(-1);
		}
		
		if(oraInizio.getText().length()>0){
			if(oraFine.getText().length()>0)
				CassandraManager.getHashtagsManagerWithTime(topic.getText(), oraInizio.getText(), oraFine.getText());
		}
			
		
		else
			CassandraManager.getHashtagsManager(topic.getText());
		
	}

	private static void tweetCountAnalysis() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, TwitterException, IOException {

		JTextField topic = new JTextField();
		JTextField oraInizio = new JTextField();
		JTextField oraFine = new JTextField();
		JLabel label = new JLabel();
		label.setText("Inserire i parametri: ");
		label.setFont(new Font("Calibri", Font.BOLD, 20));
		Object[] message = {label, "Inserire l'argomento di interesse:", topic, "Inserire ora di inizio analisi (opzionale):", oraInizio, "Inserire ora di fine analisi (opzionale):", oraFine};
		Dimension size = UIManager.getDimension("OptionPane.minimumSize");
		size.width = 450;
		size.height= 300;
		UIManager.put("OptionPane.background", new ColorUIResource(214,227,249));
		UIManager.put("Panel.background",new ColorUIResource(214,227,249));
		String [] options = {"OK", "Indietro", "Esci"};
		int option = JOptionPane.showOptionDialog(null, message, "Tweet Count", 0, 0, icon, options, options[0]);
		if(option==1)
			mainMenu();
		else
			if(option==2)
				System.exit(-1);
		while(topic.getText().length()==0){
			label.setText("Parametri errati. Riprovare: ");
			option = JOptionPane.showOptionDialog(null, message, "Tweet Count", 0, 0, icon, options, options[0]);
			if(option==1){
				mainMenu();
				break;
			}
			else
				if(option==2)
					System.exit(-1);
		}
		
	}
	
	
	
	public static void topTweetWindow(String text, String user, Integer likeAndRetweet) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, TwitterException, IOException{
		JPanel panel = new JPanel(null);
		JLabel titolo = new JLabel("Il tweet più popolare:"); JLabel testo = new JLabel(text); JLabel username = new JLabel("by @"+user); JLabel label_num_likes = new JLabel("Like + Retweet ricevuti:"); JLabel logoRetweet = new JLabel(new ImageIcon("config/retweet.png")); JLabel logoLike = new JLabel(new ImageIcon("config/heart.png")); JLabel num_likes = new JLabel(likeAndRetweet.toString());
		titolo.setFont(new Font("Verdana", Font.BOLD, 35)); titolo.setSize(500, 100); titolo.setLocation(450, 0); titolo.setForeground(Color.RED);
		testo.setFont(new Font("Verdana", Font.PLAIN, 20)); testo.setSize(1300, 80); testo.setLocation(0, 180);
		username.setFont(new Font("Verdana", Font.ITALIC, 20)); username.setSize(300, 100); username.setLocation(0, 220);
		label_num_likes.setFont(new Font("Verdana", Font.BOLD, 20)); label_num_likes.setSize(300, 100); label_num_likes.setLocation(160, 165);
		num_likes.setFont(new Font("Verdana", Font.ITALIC, 20)); num_likes.setSize(130, 100); num_likes.setLocation(85, 118);
		logoLike.setLocation(0, 150); logoRetweet.setLocation(40, 150); logoRetweet.setVisible(true); logoLike.setVisible(true);
		logoLike.setSize(35, 35); logoRetweet.setSize(35, 35);
		panel.add(num_likes); panel.add(username); panel.add(testo); panel.add(titolo); //panel.add(label_num_likes);
		panel.add(logoLike); panel.add(logoRetweet);
		Graphics.setLF("Windows");
		UIManager.put("OptionPane.background", new ColorUIResource(214,227,249));
		UIManager.put("Panel.background",new ColorUIResource(214,227,249));
		Dimension size = UIManager.getDimension("OptionPane.minimumSize");
		size.width = 1600;
		size.height= 500;
		UIManager.put("OptionPane.minimumSize", size);
		panel.setSize(700, 700);
        String [] options = {"Menù principale", "Esci"};
		int result = JOptionPane.showOptionDialog(null, panel, "Top words", 1, 0, icon, options, options[0]);
		if(result==0)
			mainMenu();
		else
			System.exit(-1);
		
	}

	public static void topwordsWindow(List<Tuple2<Integer, String>> top10Words) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, TwitterException, IOException {

		//Creo il pannello che ospiterà le componenti grafiche (null ci permette di impostare la grafica di default, che modificheremo in seguito)
		JPanel panel = new JPanel(null);
		/*
		 * Creo le label per le 10 parole
		 */
		JLabel top1 = new JLabel(top10Words.get(0)._2);
		JLabel top2 = new JLabel(top10Words.get(1)._2);
		JLabel top3 = new JLabel(top10Words.get(2)._2);
		JLabel top4 = new JLabel(top10Words.get(3)._2);
		JLabel top5 = new JLabel(top10Words.get(4)._2);
		JLabel top6 = new JLabel(top10Words.get(5)._2);
		JLabel top7 = new JLabel(top10Words.get(6)._2);
		JLabel top8 = new JLabel(top10Words.get(7)._2);
		JLabel top9 = new JLabel(top10Words.get(8)._2);
		JLabel top10 = new JLabel(top10Words.get(9)._2);
		
		/*
		 * Creo le label per l'indicazione accanto alle parole
		 */
		JLabel top1L = new JLabel("#1");
		JLabel top2L = new JLabel("#2");
		JLabel top3L = new JLabel("#3");
		JLabel top4L = new JLabel("#4");
		JLabel top5L = new JLabel("#5");
		JLabel top6L = new JLabel("#6");
		JLabel top7L = new JLabel("#7");
		JLabel top8L = new JLabel("#8");
		JLabel top9L = new JLabel("#9");
		JLabel top10L = new JLabel("#10");
		
		/*
		 * Creo la label per il titolo
		 */
		JLabel titolo = new JLabel("I 10 termini più citati");
		
		/*
		 * Per ognuna, imposto la posizione, la grandezza, il font
		 */
		
		top1L.setFont(new Font("Verdana", Font.ITALIC, 17)); top1L.setSize(40, 40); top1L.setLocation(75, 80);
		top2L.setFont(new Font("Verdana", Font.ITALIC, 17)); top2L.setSize(40, 40); top2L.setLocation(75, 100);
		top3L.setFont(new Font("Verdana", Font.ITALIC, 17)); top3L.setSize(40, 40); top3L.setLocation(75, 120);
		top4L.setFont(new Font("Verdana", Font.ITALIC, 17)); top4L.setSize(40, 40); top4L.setLocation(75, 140);
		top5L.setFont(new Font("Verdana", Font.ITALIC, 17)); top5L.setSize(40, 40); top5L.setLocation(75, 160);
		top6L.setFont(new Font("Verdana", Font.ITALIC, 17)); top6L.setSize(40, 40); top6L.setLocation(75, 180);
		top7L.setFont(new Font("Verdana", Font.ITALIC, 17)); top7L.setSize(40, 40); top7L.setLocation(75, 200);
		top8L.setFont(new Font("Verdana", Font.ITALIC, 17)); top8L.setSize(40, 40); top8L.setLocation(75, 220);
		top9L.setFont(new Font("Verdana", Font.ITALIC, 17)); top9L.setSize(40, 40); top9L.setLocation(75, 240);
		top10L.setFont(new Font("Verdana", Font.ITALIC, 17)); top10L.setSize(40, 40); top10L.setLocation(75, 260);
		
		top1.setFont(new Font("Verdana", Font.BOLD, 17)); top1.setSize(200, 40); top1.setLocation(140, 80);
		top2.setFont(new Font("Verdana", Font.BOLD, 17)); top2.setSize(200, 40); top2.setLocation(140, 100);
		top3.setFont(new Font("Verdana", Font.BOLD, 17)); top3.setSize(200, 40); top3.setLocation(140, 120);
		top4.setFont(new Font("Verdana", Font.BOLD, 17)); top4.setSize(200, 40); top4.setLocation(140, 140);
		top5.setFont(new Font("Verdana", Font.BOLD, 17)); top5.setSize(200, 40); top5.setLocation(140, 160);
		top6.setFont(new Font("Verdana", Font.BOLD, 17)); top6.setSize(200, 40); top6.setLocation(140, 180);
		top7.setFont(new Font("Verdana", Font.BOLD, 17)); top7.setSize(200, 40); top7.setLocation(140, 200);
		top8.setFont(new Font("Verdana", Font.BOLD, 17)); top8.setSize(200, 40); top8.setLocation(140, 220);
		top9.setFont(new Font("Verdana", Font.BOLD, 17)); top9.setSize(200, 40); top9.setLocation(140, 240);
		top10.setFont(new Font("Verdana", Font.BOLD, 17)); top10.setSize(200, 40); top10.setLocation(140, 260);
		
		titolo.setFont(new Font("Verdana", Font.BOLD, 20)); titolo.setSize(300, 50); titolo.setLocation(35, 0); titolo.setForeground(Color.RED);
		
		panel.add(top1L); panel.add(top2L); panel.add(top3L); panel.add(top4L); panel.add(top5L); panel.add(top6L); panel.add(top7L); panel.add(top8L); panel.add(top9L); panel.add(top10L);
		panel.add(top1); panel.add(top2); panel.add(top3); panel.add(top4); panel.add(top5); panel.add(top6); panel.add(top7); panel.add(top8); panel.add(top9); panel.add(top10);
		panel.add(titolo);// panel.add(mainMenu);
		
		/*
		 * Imposto alcune configurazioni grafiche (come le dimensioni ed il colore della finestra di output)
		 */
		
		Graphics.setLF("Windows");
		UIManager.put("OptionPane.background", new ColorUIResource(214,227,249));
		UIManager.put("Panel.background",new ColorUIResource(214,227,249));
		Dimension size = UIManager.getDimension("OptionPane.minimumSize");
		size.width = 470;
		size.height= 400;
		UIManager.put("OptionPane.minimumSize", size);
		String [] options = {"Menù principale", "Esci"};
		int result = JOptionPane.showOptionDialog(null, panel, "Top words", 1, 0, icon, options, options[0]);
		if(result==0)
			mainMenu();
			
		else
			System.exit(-1);
		
		
	}

	public static void topHTWindow(List<Tuple2<Integer, String>> top10HT) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, TwitterException, IOException {
		//Creo il pannello che ospiterà le componenti grafiche (null ci permette di impostare la grafica di default, che modificheremo in seguito)
				JPanel panel = new JPanel(null);
				/*
				 * Creo le label per le 10 parole
				 */
				JLabel top1 = new JLabel(top10HT.get(0)._2);
				JLabel top2 = new JLabel(top10HT.get(1)._2);
				JLabel top3 = new JLabel(top10HT.get(2)._2);
				JLabel top4 = new JLabel(top10HT.get(3)._2);
				JLabel top5 = new JLabel(top10HT.get(4)._2);
				JLabel top6 = new JLabel(top10HT.get(5)._2);
				JLabel top7 = new JLabel(top10HT.get(6)._2);
				JLabel top8 = new JLabel(top10HT.get(7)._2);
				JLabel top9 = new JLabel(top10HT.get(8)._2);
				JLabel top10 = new JLabel(top10HT.get(9)._2);
				
				/*
				 * Creo le label per l'indicazione accanto alle parole
				 */
				JLabel top1L = new JLabel("#1");
				JLabel top2L = new JLabel("#2");
				JLabel top3L = new JLabel("#3");
				JLabel top4L = new JLabel("#4");
				JLabel top5L = new JLabel("#5");
				JLabel top6L = new JLabel("#6");
				JLabel top7L = new JLabel("#7");
				JLabel top8L = new JLabel("#8");
				JLabel top9L = new JLabel("#9");
				JLabel top10L = new JLabel("#10");
				
				/*
				 * Creo la label per il titolo
				 */
				JLabel titolo = new JLabel("I 10 termini più citati");
				
				/*
				 * Per ognuna, imposto la posizione, la grandezza, il font
				 */
				
				top1L.setFont(new Font("Verdana", Font.ITALIC, 17)); top1L.setSize(40, 40); top1L.setLocation(75, 80);
				top2L.setFont(new Font("Verdana", Font.ITALIC, 17)); top2L.setSize(40, 40); top2L.setLocation(75, 100);
				top3L.setFont(new Font("Verdana", Font.ITALIC, 17)); top3L.setSize(40, 40); top3L.setLocation(75, 120);
				top4L.setFont(new Font("Verdana", Font.ITALIC, 17)); top4L.setSize(40, 40); top4L.setLocation(75, 140);
				top5L.setFont(new Font("Verdana", Font.ITALIC, 17)); top5L.setSize(40, 40); top5L.setLocation(75, 160);
				top6L.setFont(new Font("Verdana", Font.ITALIC, 17)); top6L.setSize(40, 40); top6L.setLocation(75, 180);
				top7L.setFont(new Font("Verdana", Font.ITALIC, 17)); top7L.setSize(40, 40); top7L.setLocation(75, 200);
				top8L.setFont(new Font("Verdana", Font.ITALIC, 17)); top8L.setSize(40, 40); top8L.setLocation(75, 220);
				top9L.setFont(new Font("Verdana", Font.ITALIC, 17)); top9L.setSize(40, 40); top9L.setLocation(75, 240);
				top10L.setFont(new Font("Verdana", Font.ITALIC, 17)); top10L.setSize(40, 40); top10L.setLocation(75, 260);
				
				top1.setFont(new Font("Verdana", Font.BOLD, 17)); top1.setSize(200, 40); top1.setLocation(140, 80);
				top2.setFont(new Font("Verdana", Font.BOLD, 17)); top2.setSize(200, 40); top2.setLocation(140, 100);
				top3.setFont(new Font("Verdana", Font.BOLD, 17)); top3.setSize(200, 40); top3.setLocation(140, 120);
				top4.setFont(new Font("Verdana", Font.BOLD, 17)); top4.setSize(200, 40); top4.setLocation(140, 140);
				top5.setFont(new Font("Verdana", Font.BOLD, 17)); top5.setSize(200, 40); top5.setLocation(140, 160);
				top6.setFont(new Font("Verdana", Font.BOLD, 17)); top6.setSize(200, 40); top6.setLocation(140, 180);
				top7.setFont(new Font("Verdana", Font.BOLD, 17)); top7.setSize(200, 40); top7.setLocation(140, 200);
				top8.setFont(new Font("Verdana", Font.BOLD, 17)); top8.setSize(200, 40); top8.setLocation(140, 220);
				top9.setFont(new Font("Verdana", Font.BOLD, 17)); top9.setSize(200, 40); top9.setLocation(140, 240);
				top10.setFont(new Font("Verdana", Font.BOLD, 17)); top10.setSize(200, 40); top10.setLocation(140, 260);
				
				titolo.setFont(new Font("Verdana", Font.BOLD, 20)); titolo.setSize(300, 50); titolo.setLocation(35, 0); titolo.setForeground(Color.RED);
				
		        
				panel.add(top1L); panel.add(top2L); panel.add(top3L); panel.add(top4L); panel.add(top5L); panel.add(top6L); panel.add(top7L); panel.add(top8L); panel.add(top9L); panel.add(top10L);
				panel.add(top1); panel.add(top2); panel.add(top3); panel.add(top4); panel.add(top5); panel.add(top6); panel.add(top7); panel.add(top8); panel.add(top9); panel.add(top10);
				panel.add(titolo);
				
				/*
				 * Imposto alcune configurazioni grafiche (come le dimensioni ed il colore della finestra di output)
				 */
				
				Graphics.setLF("Windows");
				UIManager.put("OptionPane.background", new ColorUIResource(214,227,249));
				UIManager.put("Panel.background",new ColorUIResource(214,227,249));
				Dimension size = UIManager.getDimension("OptionPane.minimumSize");
				size.width = 470;
				size.height= 400;
				UIManager.put("OptionPane.minimumSize", size);
				String [] options = {"Menù principale", "Esci"};
				int result = JOptionPane.showOptionDialog(null, panel, "Top words", 1, 0, icon, options, options[0]);
				if(result==0)
					mainMenu();
				else
					System.exit(-1);
				
				
			}
		
	}

	

