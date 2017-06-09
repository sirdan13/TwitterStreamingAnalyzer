/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package analysis;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;

import scala.Tuple2;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.examples.streaming.StreamingExamples;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;

/**
 * Consumes messages from one or more topics in Kafka and does wordcount.
 *
 * Usage: JavaKafkaWordCount <zkQuorum> <group> <topics> <numThreads>
 *   <zkQuorum> is a list of one or more zookeeper servers that make quorum
 *   <group> is the name of kafka consumer group
 *   <topics> is a list of one or more kafka topics to consume from
 *   <numThreads> is the number of threads the kafka consumer should use
 *
 * To run this example:
 *   `$ bin/run-example org.apache.spark.examples.streaming.JavaKafkaWordCount zoo01,zoo02, \
 *    zoo03 my-consumer-group topic1,topic2 1`
 */

public final class JavaKafkaWordCount {
  private static final Pattern SPACE = Pattern.compile(" ");

  private JavaKafkaWordCount() {
  }

  public static void main(String[] args) throws Exception {
    if (args.length < 4) {
      System.err.println("Usage: JavaKafkaWordCount <zkQuorum> <group> <topics> <numThreads>");
      System.exit(1);
    }
    
    /*
     * Settaggio di Spark
     */

    StreamingExamples.setStreamingLogLevels();
    SparkConf sparkConf = new SparkConf().setAppName("JavaKafkaWordCount").setMaster("local[*]");
    // Create the context with 2 seconds batch size
    JavaStreamingContext jssc = new JavaStreamingContext(sparkConf, new Duration(2000));

    /*
     * Settaggio dei topic, tramite una Map
     */
    
    int numThreads = Integer.parseInt(args[3]);
    Map<String, Integer> topicMap = new HashMap<>();
    String[] topics = args[2].split(",");
    for (String topic: topics) {
      topicMap.put(topic, numThreads);
    }
    
    /*
     * Creazione dello stream in base alla JavaStreamingSparkContext, 
     * al server Zookeeper, 
     * al nome del gruppo di consumer Kafka, 
     * alla Map dei topic
     */

    JavaPairDStream<String, String> messages =  KafkaUtils.createStream(jssc, args[0], args[1], topicMap);
    System.out.println(jssc.getState());
    System.out.println("111111111");

    /*
     * Leggo i messaggi in entrata, inserendoli in una JavaDStream contenente Stringhe
     */
    JavaDStream<String> lines = messages.map(new Function<Tuple2<String, String>, String>() {
      @Override
      public String call(Tuple2<String, String> tuple2) {
        return tuple2._2();
      }
    });
    
    /*
     * Estraggo dai messaggi le singole parole, sfruttando la funzione definita all'interno della flatMap
     */

    JavaDStream<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
      @Override
      public Iterator<String> call(String x) {
        return Arrays.asList(SPACE.split(x)).iterator();
      }
    });
    
    /*
     * Crea una JavaPair che conti le parole, prima creando una mapToPair con le parole (ridondanti) e la costante intera 1
     * Poi riduce con una reduceByKey in modo da accorpare le parole ridondanti e sommare il numero di occorrenze
     */

    JavaPairDStream<String, Integer> wordCounts = words.mapToPair(
      new PairFunction<String, String, Integer>() {
        @Override
        public Tuple2<String, Integer> call(String s) {
          return new Tuple2<>(s, 1);
        }
      }).reduceByKey(new Function2<Integer, Integer, Integer>() {
        @Override
        public Integer call(Integer i1, Integer i2) {
          return i1 + i2;
        }
      });
    
 
    System.out.println(jssc.getState());
    /*
     * A questo punto stampo il risultato
     */
    System.out.println("2222222222");
 //   wordCounts.print();
    System.out.println("333333333333");
    jssc.start();
    System.out.println(jssc.getState());
    jssc.awaitTermination();
  }
}
