package com.pli.CloudServer;

import java.io.IOException;
import java.util.Date;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;

import com.pli.IoTReasoner.IoTReasoner;

/**
 * Auther Pingjiang Li
 * Data 2016.05.19 
 *
 */
public class App
{
	public static MQTTclient_sub client;
	public static long reasoningTimer;
	public static int requestNumber;
	public static String rdfFormat="RDF/XML";
	public static IoTReasoner ioTReasoner;
	public static String ontologyURI = "http://localhost/SensorSchema/ontology#";
	
    public static void main( String[] args )
    {
    	String command="";
    	requestNumber = 0;
    	ioTReasoner = new IoTReasoner();
		ioTReasoner.setDataFormat("RDF/XML");
    	Scanner keyboard = new Scanner(System.in);
		System.out.println("IoT Cloud Reasoner, command:");
		System.out.println("        1. RDF");
		System.out.println("        2. JSON-LD");
		System.out.println("        3. N-TRIPLE");
		System.out.println("        4. EN");
		command = keyboard.next();
		switch (command) {
		case "1":
			rdfFormat="RDF/XML";
			break;
		case "2":
			rdfFormat="JSON-LD";
			break;
		case "3":
			rdfFormat="N-TRIPLE";
			break;
		case "4":
			rdfFormat="EN";
			break;

		default:
			break;
		}
        System.out.println("Data Format: " + rdfFormat);
        
        client = new MQTTclient_sub(
        		//"tcp://52.58.92.249:1883",
        		"tcp://vm0104.virtues.fi:1883",
        		//"tcp://localhost:1883",
        		"IoTReasoning/+"
        		);
        client.start();
    }
    public static synchronized void reasoning( String data){
    	Date startTime = new Date();
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
    	}else if( App.rdfFormat.equals("EN") ){
    		rdfModel = enToDataModel(data);
    	}
    	
    	
    	//System.out.println("read");
    	//IoTReasoner ioTReasoner = new IoTReasoner();
        ioTReasoner.setDataFormat(App.rdfFormat);
        ioTReasoner.setDataModel(rdfModel);
        ioTReasoner.inferModel(true);
        
        System.out.println("Finish Reasoning.");
        
        Date finishTime = new Date();
		App.timerIncrease( finishTime.getTime()-startTime.getTime() );
    }
    private static Model enToDataModel(String enData) {
        //long d = (new Date()).getTime();

    	
        Model dataModel = ModelFactory.createDefaultModel();
        //Log.d("EnReasoner","run en reasoner 1");
        String[] enLines = enData.split("__");

        //Statement typeStatement = factory.createStatement(obsURI, RDF.TYPE, obsType);
        //myGraph.add(typeStatement);
        dataModel.setNsPrefix("obs", ontologyURI);

        for(int i = 0; i < enLines.length; i++){

            enLines[i] = enLines[i].substring(17,enLines[i].length()-1);
            String[] obs = enLines[i].split(" ");
            //Log.d("EnReasoner","OBS[0]:"+obs[0]);
            //Log.d("EnReasoner","OBS[1]:"+obs[1]);
            //Log.d("EnReasoner","OBS[2]:"+obs[2]);
            //Log.d("EnReasoner","OBS[3]:"+obs[3]);

            Resource obsInstance = dataModel.createResource();
            dataModel.add(obsInstance, RDF.type, dataModel.createResource(getTemplate()[0]));

            if(obs.length==11){
                dataModel.add(obsInstance, dataModel.createProperty(getTemplate()[1]), dataModel.createTypedLiteral(Integer.valueOf(obs[0])));
                dataModel.add(obsInstance, dataModel.createProperty(getTemplate()[2]), dataModel.createTypedLiteral(Integer.valueOf(obs[1])));
                dataModel.add(obsInstance, dataModel.createProperty(getTemplate()[3]), dataModel.createTypedLiteral(Double.valueOf(obs[2])));
                dataModel.add(obsInstance, dataModel.createProperty(getTemplate()[4]), dataModel.createTypedLiteral(Double.valueOf(obs[3])));
                dataModel.add(obsInstance, dataModel.createProperty(getTemplate()[5]), dataModel.createTypedLiteral(Double.valueOf(obs[4])));
                dataModel.add(obsInstance, dataModel.createProperty(getTemplate()[6]), dataModel.createTypedLiteral(Integer.valueOf(obs[5])));
                dataModel.add(obsInstance, dataModel.createProperty(getTemplate()[7]), dataModel.createTypedLiteral(Integer.valueOf(obs[6])));
                dataModel.add(obsInstance, dataModel.createProperty(getTemplate()[8]), dataModel.createTypedLiteral(Double.valueOf(obs[7])));
                dataModel.add(obsInstance, dataModel.createProperty(getTemplate()[9]), dataModel.createTypedLiteral(Double.valueOf(obs[8])));
                dataModel.add(obsInstance, dataModel.createProperty(getTemplate()[10]), dataModel.createTypedLiteral(Long.valueOf(obs[9])));
                dataModel.add(obsInstance, dataModel.createProperty(getTemplate()[11]), dataModel.createLiteral(obs[10]));
            }
        }

        //reasoningLatency = reasoningLatency + ((new Date()).getTime())-d;
        return dataModel;
    }
    
    public static String[] getTemplate(){


        return new String[]{ontologyURI+"Observation", ontologyURI + "hasID", ontologyURI + "hasArea", ontologyURI +
                "hasLatitude", ontologyURI + "hasLongitude", ontologyURI + "hasVelocity", ontologyURI + "hasDirection", ontologyURI +
                "hasSender", ontologyURI + "hasDistance", ontologyURI + "hasAcceleration", ontologyURI + "hasDate", ontologyURI + "hasDateTime"};
    }
    
	public static synchronized void timerIncrease( long time){
    	reasoningTimer += time;
    	requestNumber++;
    	System.out.println( "Reasoning time: " + reasoningTimer + "("+ reasoningTimer/1000+" seconds) request:"+requestNumber );
    	
    }
    
}
