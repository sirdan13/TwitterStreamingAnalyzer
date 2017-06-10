package producer;

import java.text.Normalizer;
import java.text.Normalizer.Form;

import utilities.Tweet;

public class TweetProcessing {
	
	private Tweet t;
	private String processedText;
	private String originalText;
	
	public Tweet getT() {
		return t;
	}

	public void setT(Tweet t) {
		this.t = t;
	}

	public String getProcessedText() {
		return processedText;
	}

	public void setProcessedText(String processedText) {
		this.processedText = processedText;
	}

	public String getOriginalText() {
		return originalText;
	}

	public void setOriginalText(String originalText) {
		this.originalText = originalText;
	}

	public TweetProcessing(Tweet t){
		this.t=t;
		this.originalText=t.getText();
	}
	
	public void processText(){
		//Rimuove i ritorni a capo 
		processedText=originalText.replaceAll("(\\r|\\n)", "");
		//Rimuove i link
		//processedText=processedText.replaceAll("https?://[^ ]*", "");
		//Rimuove gli accenti
		processedText = removeAccents(processedText);
		//Tutto minuscolo
		processedText=processedText.toLowerCase();
		//Rimuove caratteri non alfanumerici (# escluso)
		processedText=processedText.replaceAll("[^a-zA-Z0-9#]", " ");
		//Separa l'hashtag dalla parola precedente
		processedText=processedText.replaceAll("#", " #");
		//Rimuove gli spazi extra
		processedText=processedText.replaceAll("  *", " ");
		//Rimuove gli spazi all'inizio del testo
		processedText=processedText.replaceAll("^ ", "");
	}
	
	
	public static String removeAccents(String text) {
	    return text == null ? null :
	        Normalizer.normalize(text, Form.NFD)
	            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	}

}
