package com.pli.IoTNode;

import java.util.Date;

import javax.naming.Context;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


//example from https://gist.github.com/m2mIO-gister/5275324
public class IoTMqttClient implements MqttCallback{

	String BROKER_URL = "tcp://ec2-52-58-177-76.eu-central-1.compute.amazonaws.com:1883";
	String clientID ;
	MqttClient myClient;
	MqttConnectOptions connOpt;
	MqttTopic topic ;
	long timer;
	Date startTime;
	Date finishTime;

	
	public IoTMqttClient(String url , String topic , String clientID){
		// setup MQTT Client
		BROKER_URL = url;

		this.clientID = clientID;
		connOpt = new MqttConnectOptions();
		
		connOpt.setCleanSession(true);
		connOpt.setKeepAliveInterval(30);
//		connOpt.setUserName(M2MIO_USERNAME);
//		connOpt.setPassword(M2MIO_PASSWORD_MD5.toCharArray());
		
		MemoryPersistence persistence = new MemoryPersistence();
		
		// Connect to Broker
		try {
			myClient = new MqttClient(BROKER_URL, clientID , persistence);
			myClient.setCallback(this);
			myClient.connect(connOpt);
		} catch (MqttException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		//System.out.println("Connected to " + BROKER_URL);

		// setup topic
		String myTopic = topic+"/"+clientID;
		this.topic = myClient.getTopic(myTopic);


	}
	
	public void publish(String pubMsg){

   		int pubQoS = 2;
		MqttMessage message = new MqttMessage(pubMsg.getBytes());
    	message.setQos(pubQoS);
    	message.setRetained(false);

    	// Publish the message
    	//System.out.println("Publishing to topic \"" + topic + "\" qos " + pubQoS);
    	MqttDeliveryToken token = null;
    	startTime=new Date();
    	try {
    		// publish message to broker
			token = topic.publish(message);
	    	// Wait until the message has been delivered to the broker
			token.waitForCompletion();
//			Thread.sleep(100);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void connectionLost(Throwable cause) {
		System.out.println("Connection lost!");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("-------------------------------------------------");
		System.out.println("| Topic:" + topic);
		System.out.println("| Message: " + new String(message.getPayload()));
		System.out.println("-------------------------------------------------");
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub
		//System.out.println("Pub complete!");
		finishTime = new Date();
		timer = finishTime.getTime()-startTime.getTime();
		System.out.println("Time: " + timer);
		App.timerIncrease(timer);
	}
	
	public void close(){
		try {
			myClient.close();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
