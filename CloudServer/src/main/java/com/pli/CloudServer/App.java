package com.pli.CloudServer;

import java.io.IOException;
import java.util.Date;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

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
        		"tcp://localhost:1883",
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
    public static synchronized void timerIncrease( long time){
    	reasoningTimer += time;
    	requestNumber++;
    	System.out.println( "Reasoning time: " + reasoningTimer + "("+ reasoningTimer/1000+" seconds) request:"+requestNumber );
    	
    }
    
}
