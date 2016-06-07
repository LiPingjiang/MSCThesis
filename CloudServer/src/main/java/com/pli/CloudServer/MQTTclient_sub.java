package com.pli.CloudServer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
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
	
	IoTReasoner ioTReasoner;
	
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
		ioTReasoner = new IoTReasoner();
		ioTReasoner.setDataFormat("RDF/XML");
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
        	Date startTime = new Date();
        	String data =new String(message.getPayload());
            //System.out.println("topic: " + topic +" message: " + data);
        	System.out.println("messageArrived");
//        	System.out.println(App.rdfFormat);
//        	System.out.println(data);
            //Reasoning
        	now();
        	
           
        	Model rdfModel =  ModelFactory.createDefaultModel();
        	
        	if(App.rdfFormat.equals("RDF/XML")){
        		try {
        			rdfModel.read(IOUtils.toInputStream(data, "UTF-8"), null, App.rdfFormat);
        		} catch (IOException e) {
        			// TODO Auto-generated catch block
        			System.out.println("run crash.");
        			e.printStackTrace();
        		}
        	}else if( App.rdfFormat.equals("JSON-LD") ){
        		try {
        			RDFDataMgr.read(rdfModel, IOUtils.toInputStream(data, "UTF-8"),Lang.JSONLD);
        		} catch (IOException e) {
        			// TODO Auto-generated catch block
        			System.out.println("run crash.");
        			e.printStackTrace();
        		}
        	}else if( App.rdfFormat.equals("N-TRIPLE") ){
        		try {
        			RDFDataMgr.read(rdfModel, IOUtils.toInputStream(data, "UTF-8"),Lang.N3);
        		} catch (IOException e) {
        			// TODO Auto-generated catch block
        			System.out.println("run crash.");
        			e.printStackTrace();
        		}
        	}
        	else if( App.rdfFormat.equals("EN") ){
        		rdfModel=enToDataModel(data);
        	}
        	
        	
        	//System.out.println("read");
        	//IoTReasoner ioTReasoner = new IoTReasoner();
            ioTReasoner.setDataFormat(App.rdfFormat);
            ioTReasoner.setDataModel(rdfModel);
            ioTReasoner.inferModel(true);
            
            System.out.println("Finish Reasoning.");
            now();
            Date finishTime = new java.util.Date();
    		App.timerIncrease( finishTime.getTime()-startTime.getTime() );
        }
        public void now(){
        	Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            System.out.println( sdf.format(cal.getTime()) );
        }
    }
    
    private String ontologyURI = "http://localhost/SensorSchema/ontology#";
    public Model enToDataModel(String enData) {
        long d = (new Date()).getTime();
        Model dataModel = ModelFactory.createDefaultModel();

        String[] enLines = enData.split("\n");

        //Statement typeStatement = factory.createStatement(obsURI, RDF.TYPE, obsType);
        //myGraph.add(typeStatement);
        dataModel.setNsPrefix("obs", ontologyURI);

        for(int i = 1; i < enLines.length; i++){

            String[] obs = enLines[i].split(" ");

            Resource obsInstance = dataModel.createResource();
            dataModel.add(obsInstance, RDF.type, dataModel.createResource(this.getTemplate()[0]));

            /*for(int j = 0; j < obs.length; j++){
                dataModel.add(obsInstance, dataModel.createProperty(this.getTemplate()[j+1]), dataModel.createLiteral(obs[j]));
            }*/

            if(obs.length==11){
                dataModel.add(obsInstance, dataModel.createProperty(this.getTemplate()[1]), dataModel.createTypedLiteral(Integer.valueOf(obs[0])));
                dataModel.add(obsInstance, dataModel.createProperty(this.getTemplate()[2]), dataModel.createTypedLiteral(Integer.valueOf(obs[1])));
                dataModel.add(obsInstance, dataModel.createProperty(this.getTemplate()[3]), dataModel.createTypedLiteral(Double.valueOf(obs[2])));
                dataModel.add(obsInstance, dataModel.createProperty(this.getTemplate()[4]), dataModel.createTypedLiteral(Double.valueOf(obs[3])));
                dataModel.add(obsInstance, dataModel.createProperty(this.getTemplate()[5]), dataModel.createTypedLiteral(Double.valueOf(obs[4])));
                dataModel.add(obsInstance, dataModel.createProperty(this.getTemplate()[6]), dataModel.createTypedLiteral(Integer.valueOf(obs[5])));
                dataModel.add(obsInstance, dataModel.createProperty(this.getTemplate()[7]), dataModel.createTypedLiteral(Integer.valueOf(obs[6])));
                dataModel.add(obsInstance, dataModel.createProperty(this.getTemplate()[8]), dataModel.createTypedLiteral(Double.valueOf(obs[7])));
                dataModel.add(obsInstance, dataModel.createProperty(this.getTemplate()[9]), dataModel.createTypedLiteral(Double.valueOf(obs[8])));
                dataModel.add(obsInstance, dataModel.createProperty(this.getTemplate()[10]), dataModel.createTypedLiteral(Long.valueOf(obs[9])));
                dataModel.add(obsInstance, dataModel.createProperty(this.getTemplate()[11]), dataModel.createLiteral(obs[10]));
            }
        }

        return dataModel;
    }

    private String[] getTemplate(){

       String[] obsTemplate = {ontologyURI+"Observation", ontologyURI + "hasID", ontologyURI + "hasArea", ontologyURI +
               "hasLatitude", ontologyURI + "hasLongitude", ontologyURI + "hasVelocity", ontologyURI + "hasDirection", ontologyURI +
               "hasSender", ontologyURI + "hasDistance", ontologyURI + "hasAcceleration", ontologyURI + "hasDate", ontologyURI + "hasDateTime"};

       return obsTemplate;
   }

    
}
