package com.mscthesis.pli.iotgateway;

/**
 * Created by pli on 2016/5/26.
 */
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MQTTclient_pub{

    private MqttAsyncClient client;
    private String clientId;
    private String topic;
    private String broker = "tcp://ec2-52-58-177-76.eu-central-1.compute.amazonaws.com:1883";
    int qos = 2;

    MemoryPersistence persistence = new MemoryPersistence();

    MQTTclient_pub(String broker,String topic, String clientId){
        this.topic = topic;
        this.broker = broker;
        this.clientId = clientId;

    }

    public boolean publish(String content){
        try {
            MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            sampleClient.connect(connOpts);

            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            sampleClient.publish(topic+"/"+clientId, message);

            sampleClient.disconnect();


            return true;
        } catch (MqttException me) {
            System.out.println("reason " 	+ me.getReasonCode());
            System.out.println("msg " 		+ me.getMessage());
            System.out.println("loc " 		+ me.getLocalizedMessage());
            System.out.println("cause " 	+ me.getCause());
            System.out.println("excep " 	+ me);
            me.printStackTrace();
        }
        return false;
    }


}