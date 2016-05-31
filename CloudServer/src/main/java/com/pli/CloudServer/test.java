package com.pli.CloudServer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;



public class test {
	public static void main(String[] args) {

		reasoningSesame();
    }
	public void subscribe(){
		String topic        = "test";
        String content      = "Message from MqttPublishSample";
        int qos             = 2;
        String broker       = "tcp://ec2-52-58-177-76.eu-central-1.compute.amazonaws.com:1883";
        String clientId     = "JavaSample";
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting to broker: "+broker);
            sampleClient.connect(connOpts);
            System.out.println("Connected");
            System.out.println("Publishing message: "+content);
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            sampleClient.publish(topic, message);
            System.out.println("Message published");
            sampleClient.disconnect();
            System.out.println("Disconnected");
            System.exit(0);
        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
	}
	public static void reasoningTest(){
		now();
		String data ="<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:obs=\"http://localhost/SensorSchema/ontology#\"><rdf:Description rdf:about=\"http://localhost/SensorSchema/ontology#Observation_51709293_2_9c74ecb7-bdea-4c02-8b08-3a395f612217\">    <obs:hasDateTime>2013-04-05T13:00:07</obs:hasDateTime>    <obs:hasLatitude rdf:datatype=\"http://www.w3.org/2001/XMLSchema#double\">65.00949999999999</obs:hasLatitude>    <obs:hasAcceleration rdf:datatype=\"http://www.w3.org/2001/XMLSchema#double\">0.25902810147633043</obs:hasAcceleration>    <obs:hasSender rdf:datatype=\"http://www.w3.org/2001/XMLSchema#int\">51709293</obs:hasSender>    <obs:hasLongitude rdf:datatype=\"http://www.w3.org/2001/XMLSchema#double\">25.467033333333333</obs:hasLongitude>    <rdf:type rdf:resource=\"http://localhost/SensorSchema/ontology#Observation\"/>    <obs:hasID rdf:datatype=\"http://www.w3.org/2001/XMLSchema#int\">2</obs:hasID>    <obs:hasVelocity rdf:datatype=\"http://www.w3.org/2001/XMLSchema#double\">35.0</obs:hasVelocity>    <obs:hasArea rdf:datatype=\"http://www.w3.org/2001/XMLSchema#int\">90264</obs:hasArea>    <obs:hasDistance rdf:datatype=\"http://www.w3.org/2001/XMLSchema#double\">65.68356402999615</obs:hasDistance>    <obs:hasDirection rdf:datatype=\"http://www.w3.org/2001/XMLSchema#int\">212</obs:hasDirection>    <obs:hasDate rdf:datatype=\"http://www.w3.org/2001/XMLSchema#long\">1365156007506</obs:hasDate>  </rdf:Description></rdf:RDF>";
		System.out.println(data);
		// TODO Auto-generated method stub
		// create an empty model
        Model rdfModel =  ModelFactory.createDefaultModel();
        // read the RDF/XML file
        
        try {
			rdfModel.read(IOUtils.toInputStream(data, "UTF-8"), null, "RDF/XML");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("run crash.");
			e.printStackTrace();
		}
        System.out.println("finish.");
        now();
	}
	public static void now(){
    	Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        System.out.println( sdf.format(cal.getTime()) );
    }
	
	public static void reasoningSesame(){
		RepositoryConnection connection = null;
	    String repositoryID = "iot";
	    //private String sesameServerAddress = "http://cse-cn0004.oulu.fi:10010/";
	    //private String sesameServerAddress = "http://localhost:10010/openrdf-sesame";
	    String sesameServerAddress = "http://ec2-52-58-177-76.eu-central-1.compute.amazonaws.com:10010/openrdf-sesame";
		Repository repository = new HTTPRepository(sesameServerAddress, repositoryID);
        System.out.println("create Connection 2");
        try {
            repository.initialize();
            connection = repository.getConnection();
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        System.out.println("connected");
	}
}
