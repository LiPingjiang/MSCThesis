package com.pli.CloudServer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


import com.pli.IoTReasoner.IoTReasoner;

public class MQTTclient_sub{
	
	
	public static int requestNumber;
	
	private MqttAsyncClient client;
	private String topic = "test";
	//private String broker = "tcp://ec2-52-58-177-76.eu-central-1.compute.amazonaws.com:1883";
	private String broker;
	int qos = 2;
    String clientId = "Pingjiang";
    MemoryPersistence persistence = new MemoryPersistence();
    //Date startTime;
    boolean isFirst;//is first message come?
    
	MQTTclient_sub(String broker,String topic){
		
		this.topic = topic;
		this.broker = broker;
		requestNumber = 0;
		
		//isFirst=false;
	}

	public boolean start(){
		try {
			client = new MqttAsyncClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(false);
            client.setCallback(new callbackConfig());
            System.out.println("Connecting to broker: " + broker);
            client.connect(connOpts);
            System.out.println("Connected");
            Thread.sleep(1000);
            client.subscribe(topic, qos);
            System.out.println("Subscribed the topic: "+topic);
            return true;
        } catch (Exception me) {
            if (me instanceof MqttException) {
                System.out.println("reason " + ((MqttException) me).getReasonCode());
            }
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }
		return false;
	}
	
    public boolean publish( String content){
    	MqttMessage message = new MqttMessage(content.getBytes());
    	
        message.setQos(qos);
        try {
			client.publish(topic, message);
		} catch (MqttPersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return true;
    }
    public boolean stop(){
    	try {
			client.disconnect();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return true;
    }
    public class callbackConfig implements MqttCallback{

    	public void connectionLost(Throwable arg0) {
            System.err.println("connection lost");

        }

        public void deliveryComplete(IMqttDeliveryToken arg0) {
            System.err.println("delivery complete");
        }

        public void messageArrived(String topic, MqttMessage message) throws Exception {
//        	if(isFirst==false){
//        		isFirst=true;
//        		startTime= new Date();
//        	}
        	requestNumber++;
        	
        	String data =new String(message.getPayload());
            //System.out.println("topic: " + topic +" message: " + data);
        	System.out.println("messageArrived request number: "+requestNumber);
        	App.reasoning(data);
//        	System.out.println(App.rdfFormat);
//        	System.out.println(data);
            //Reasoning
        	now();
        	
           
        	
        }
        public void now(){
        	Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            System.out.println( sdf.format(cal.getTime()) );
        }
    }

    
}
