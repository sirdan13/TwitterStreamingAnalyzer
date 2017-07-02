# TwitterStreamingAnalyzer
Analyzes streams of tweets in real time.

This package provides three main products which allow you to:
  - Stream Twitter data
  - Store and pre-analyze Twitter data
  - Retrieve data and show output of the statistics computed
  
This entire system can be distributed at all steps. In order to do so, you can edit the configuration files, placed in the config folder.
  
# TwitterProducer
This application connects directly to Twitter in order to fetch the posts according to some parameters.
Once the streaming is on, a Kafka Producer sends the data to several Kafka topics.

# SparkConsumer
This application works as a Kafka Consumer, reading the streaming data from the topics with Spark Streaming.
Once the reading is on, the different kinds of data are processed and stored to a Cassandra db. In order to save the content of all the topics, you may need to launch multiple parallel runs of this application.

# Analytics
This application shows the output of the analysis stored in the db, or computed "on-demand" while this app runs.
It's composed of menus, which allow you to move from one analysis to another without killing and restarting the app.
