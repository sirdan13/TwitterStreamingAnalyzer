package graphics;

import java.awt.Dimension;
import java.awt.Font;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.ColorUIResource;

import producer.TwitterProducer;

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

}
