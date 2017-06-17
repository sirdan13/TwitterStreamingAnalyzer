package graphics;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.ColorUIResource;

public class Graphics {
	
	public static void main(String[] args){
		String [] key = insertKeywords();
		for(String s : key)
			System.out.println(s);
	}
	
	
	static Icon icon =  new ImageIcon("config/icon.png");
	
	public Graphics(String fileIcon){
		Graphics.icon = new ImageIcon(fileIcon);
	}
	
	
	public static String [] insertKeywords(){
		 
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
	
	
	private static void setLF(String lf) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException{
		for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
 	        if (lf.equals(info.getName())) {
 	            UIManager.setLookAndFeel(info.getClassName());
 	            break;
 	        }
 	    }
	}

}
