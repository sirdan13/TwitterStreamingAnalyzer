package graphics;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;

public class JOptionPaneMultiInput {
   public static void main(String[] args) {
	   JTextField key = new JTextField();
	   JTextField lang = new JTextField();
	   JTextField topic = new JTextField();
	   JLabel label = new JLabel();
	   label.setText("Inserire valori multipli: ");
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
		 Icon icon = new ImageIcon("config/icon.png");
		 UIManager.put("OptionPane.background", new ColorUIResource(214,227,249));
		 UIManager.put("Panel.background",new ColorUIResource(214,227,249));
		 int option = JOptionPane.showConfirmDialog(null, message, "Prova", JOptionPane.OK_CANCEL_OPTION, 0, icon);
	   if (option == JOptionPane.OK_OPTION) {
		   String [] keywords = key.getText().split(",");
		   String [] languages = lang.getText().split(",");
		   System.out.println("Keywords: ");
		   for(String k : keywords)
			   System.out.println(k);
		   System.out.println("Languages: ");
		   for(String k : languages)
			   System.out.println(k);
		   
	   }
   }
}