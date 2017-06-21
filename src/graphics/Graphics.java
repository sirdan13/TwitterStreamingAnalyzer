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
	static String menuTweetPopolare;
	
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
		size.height= 340;
		UIManager.put("OptionPane.minimumSize", size);
		panel.setForeground(new ColorUIResource(214,227,249));
		panel.setBackground(new ColorUIResource(214,227,249));
		JOptionPane.showMessageDialog(null, panel, "Twitter", 0, icon);
        
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
		username.setFont(new Font("Verdana", Font.ITALIC, 20)); username.setSize(150, 100); username.setLocation(0, 220);
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
		size.width = 1400;
		size.height= 500;
		UIManager.put("OptionPane.minimumSize", size);
		panel.setSize(700, 700);
	//	JButton refresh = new JButton(); 
		JButton mainMenu = new JButton();
	//	refresh.setText("Ricarica"); refresh.setSize(165, 65); refresh.setLocation(850, 310); refresh.setFont(new Font("Verdana", Font.PLAIN, 15));
		mainMenu.setText("Menù principale"); mainMenu.setSize(165, 65); mainMenu.setLocation(1020, 310); mainMenu.setFont(new Font("Verdana", Font.PLAIN, 15));
		
		ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JButton source = (JButton) e.getSource();
                menuTweetPopolare = source.getText();
            }
        };
    //    refresh.addActionListener(listener);
        mainMenu.addActionListener(listener);
    //    panel.add(refresh);
        panel.add(mainMenu);
		int result = JOptionPane.showOptionDialog(null, panel, "Tweet più popolare", 1, 0, icon, null, null);
		if(result==0){
			if(menuTweetPopolare.equals("Menù principale"))
				mainMenu();
			else
				if(menuTweetPopolare.equals("Ricarica"))
					popularTweets();
		}
		else
			System.exit(-1);
		
	
		
	}

	
}
