package utilities;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Arrays;
import org.apache.spark.api.java.JavaRDD;
import analysis.Analytics;
import scala.Serializable;

public class Tweet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String text;
	private String processedText;
	private boolean isRetweet;
	
	

	public Tweet(){
		
	}
	
	public Tweet(String originalText,boolean isRetweet){
		this.text = originalText;
		processText();
	}
	
	
	public void processText(){
		//Rimuove il termine "RT" che indica il retweet
		if(isRetweet() && text.startsWith("RT @"))
			processedText = text.substring(3);
		else
			processedText=text;
		//Rimuove i ritorni a capo 
		processedText=processedText.replaceAll("(\\r|\\n)", "");
		//Rimuove i link
		processedText=processedText.replaceAll("https?://[^ ]*", "");
		//Rimuove gli accenti
		processedText = removeAccents(processedText);
		//Tutto minuscolo
		processedText=processedText.toLowerCase();
		//Rimuove caratteri non alfanumerici
		processedText=processedText.replaceAll("[^a-zA-Z0-9]", " ");
		//Rimuove gli spazi extra
		processedText=processedText.replaceAll("  *", " ");
		//Rimuove gli spazi all'inizio del testo
		processedText=processedText.replaceAll("^ ", "");
		//Rimuove termini indesiderati
		processedText=removeStopWords(processedText);
	}
	
		private String removeStopWords(String text) {
			/*
			 * Converto il testo in un array splittando la stringa per gli spazi
			 * converto l'array in lista
			 * distribuisco la lista in una JavaRDD
			 * filtro la JavaRDD verificando che le parole contenute non appartengano alla lista delle stopwords
			 * alla fine nella JavaRDD rimarranno solo le parole ammesse
			 */
			JavaRDD<String> pText = Analytics.getJsc().parallelize(Arrays.asList(text.split(" "))).filter((x)->!Analytics.getStopWordsSet().contains(x));
			String output = "";
			for(String s : pText.collect())
				output+=s+" ";
			return output;
		}
		
		public static String removeAccents(String text) {
		    return text == null ? null :
		        Normalizer.normalize(text, Form.NFD)
		            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
		}
	
	public String getProcessedText() {
		return processedText;
	}

	public void setProcessedText(String processedText) {
		this.processedText = processedText;
	}
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public boolean isRetweet() {
		return isRetweet;
	}

	public void setRetweet(boolean isRetweet) {
		this.isRetweet = isRetweet;
	}

		
}
