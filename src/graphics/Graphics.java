package graphics;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
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

import producer.TwitterProducer;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

public class Graphics {
	
	static Icon icon =  new ImageIcon("config/icon.png");
	
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
		   Object[] message = {
				label,
		       "Keywords:", key,
		       "Languages:", lang,
		       "Topic:", topic
		   };
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
	
	
	public static void mostMentionedUser(String screen_name) throws TwitterException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException{
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
        JLabel titolo = new JLabel(), nome = new JLabel(), account = new JLabel(), immagine = new JLabel(userIcon), descrizione = new JLabel();
        titolo.setSize(800, 80); titolo.setLocation(0, 0); titolo.setFont(new Font("Verdana", Font.PLAIN, 20));
        nome.setSize(200, 28); nome.setLocation(100, 65); nome.setFont(new Font("Verdana", Font.BOLD, 20));
        account.setSize(200, 15); account.setLocation(100, 95); account.setFont(new Font("Verdana", Font.ITALIC, 15));
        descrizione.setSize(800, 200); descrizione.setLocation(0, 75); descrizione.setFont(new Font("Verdana", Font.ITALIC, 18));
        immagine.setSize(80, 80); immagine.setLocation(0, 65);
        nome.setText(user.getName()); account.setText("@"+user.getScreenName()); titolo.setText("L'utente più citato:"); descrizione.setText(user.getDescription());
        panel.add(descrizione); panel.add(immagine); panel.add(account); panel.add(nome); panel.add(titolo);
        
        Graphics.setLF("Windows");
		UIManager.put("OptionPane.background", new ColorUIResource(214,227,249));
		UIManager.put("Panel.background",new ColorUIResource(214,227,249));
		Dimension size = UIManager.getDimension("OptionPane.minimumSize");
		size.width = 1000;
		size.height= 300;
		UIManager.put("OptionPane.minimumSize", size);
		panel.setForeground(new ColorUIResource(214,227,249));
		panel.setBackground(new ColorUIResource(214,227,249));
		JOptionPane.showMessageDialog(null, panel, "Twitter", 0, icon);
        
	}
	
	public static void mainMenu() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException{
		JPanel panel = new JPanel(null);
		JButton button1 = new JButton();
		button1.setText("Conteggio Tweet"); button1.setSize(300, 80); button1.setLocation(100, 200); button1.setFont(new Font("Verdana", Font.ITALIC, 20));
		JButton button2 = new JButton();
		button2.setText("Top #Hashtag"); button2.setSize(300, 80); button2.setLocation(100, 350); button2.setFont(new Font("Verdana", Font.ITALIC, 20));
		panel.add(button2); panel.add(button1);
		
		
		Graphics.setLF("Windows");
		UIManager.put("OptionPane.background", new ColorUIResource(214,227,249));
		UIManager.put("Panel.background",new ColorUIResource(214,227,249));
		Dimension size = UIManager.getDimension("OptionPane.minimumSize");
		size.width = 1000;
		size.height= 700;
		UIManager.put("OptionPane.minimumSize", size);
		panel.setForeground(new ColorUIResource(214,227,249));
		panel.setBackground(new ColorUIResource(214,227,249));
		
		
		
		JOptionPane.showMessageDialog(null, panel, "Twitter", 0, icon);
		
		
		
		
		
	}

}
