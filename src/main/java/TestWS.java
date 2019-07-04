
/***********************************************************************************************************************
 Copyright (c) Damak Mahdi.
 Github.com/damakmahdi
 damakmahdi2012@gmail.com
 linkedin.com/in/mahdi-damak-400a3b14a/
 **********************************************************************************************************************/

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.mqtt.MQTTUtils;
import org.junit.jupiter.api.Test;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

@ClientEndpoint
public class TestWS {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(SparkStreamingMQTT.class);

    //This is a global static variable so that we can stop the stream
    //from the stored procedure
    private JavaStreamingContext jssc = null;

    String checkpointDirectory = "/checkpoint/";
    @Test
    public void processMQTT() throws InterruptedException{
        Logger.getLogger("org").setLevel(Level.OFF);
        Logger.getLogger("akka").setLevel(Level.OFF);
        LOG.info("************ Spark MQTT Streaming Starts now");
        // Create the spark application and set the name to MQTT
        SparkConf sparkConf = new SparkConf().setAppName("MQTT").setMaster("local[*]");

        // Create the spark streaming context with a 'numSeconds' second batch size
        jssc = new JavaStreamingContext(sparkConf, Durations.seconds(2));
        jssc.checkpoint(checkpointDirectory);

        LOG.info("************ Subscribing to ICAM's Brocker : 'tcp://app.icam.fr:1883' starts now");
        LOG.info("************ Reading data from the Brocker's Topic : 'ardgetti/1/power' starts now");
        //2. MQTTUtils to collect MQTT messages
        JavaReceiverInputDStream<String> messages = MQTTUtils.createStream(jssc,
                "tcp://app.icam.fr:1883",
                "ardgetti/1/power");
        LOG.info("************ Data Processing starts now");
        //process the messages on the queue and save them to the database
        //messages.print();
        messages.print();


        //JavaDStream<Float> words = messages.flatMap(x -> Arrays.asList(Float.valueOf(x)).iterator());
        //words.foreachRDD(e-> System.out.println());
        //r->r.foreach(s->JavaEsSparkSQL.saveToEs(,"test"))
        //                 rdd-> JavaEsSpark.saveToEs(rdd, "test")
        //messages.foreachRDD(rdd -> System.out.printf("value =: %d\n", rdd.collect().get(0)));
        //JavaEsSparkStreaming.saveToEs(messages, "test");
        // Start the context

        LOG.info("************ Starting the context");
        jssc.start();


        jssc.awaitTermination();
    }

    public void sendmsg(String s) throws IOException, DeploymentException {
        final WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();


        Session session = webSocketContainer.connectToServer(new Endpoint() {
            @Override
            public void onOpen(Session session, EndpointConfig config) {
                try {
                    session.getBasicRemote().sendObject(s);
                } catch (IOException | EncodeException e) {
                    e.printStackTrace();
                }
            }
        }, URI.create("ws://localhost:8080/SimpleServlet_WAR/socket"));
    }
    public static void main(String[] args) throws IOException, DeploymentException {
        final WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();

        try {
            Session session = webSocketContainer.connectToServer(new Endpoint() {
                @Override
                public void onOpen(Session session, EndpointConfig config) {
                    try {
                        for(int i=0;i<5;i++){
                            session.getBasicRemote().sendText("test");

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, URI.create("ws://localhost:8080/SimpleServlet_WAR/socket"));

        } catch (DeploymentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}