package utilities;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.UnsupportedLookAndFeelException;

import graphics.Graphics;
import twitter4j.TwitterException;

public class DateManager {
	
	public static String getDate(){
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Date date = new Date();
		return dateFormat.format(date);
		
	}
	
	public static void main(String [] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, TwitterException, IOException, UnsupportedLookAndFeelException{
	//	Graphics.mostMentionedUser("robi_fall");
		Graphics.mainMenu();
	}
	

}
