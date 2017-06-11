package consumer;

import java.awt.Component;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;

import scala.Tuple2;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.examples.streaming.StreamingExamples;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
public class SparkConsumer {

	public static void main(String[] args) {
		
		String [] options = {"hashtags", "mentions", "original-text", "processed-text"};
		Icon icon = new ImageIcon("config/icon.png");
		int topicIndex = JOptionPane.showOptionDialog(null, "Scegliere quale topic analizzare", "Topic", 0, 3, icon, options, null);
		//TODO 
		/*
		 * Da qui ramificare l'esecuzione. A seconda del topic scelto, verrà chiamato il corrispondente metodo di analisi.
		 */
	}

}
