package com.pli.IoTNode;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;

import com.pli.IoTNode.App.CoapHandlerTimer;

/**
 * Hello world!
 *
 */
public class App 
{
	static int incidentStartNumber = 15000;
	static int NumberOfThreads = 1;		//how many threads(IoTNodes)
	static int DataSize = 1; 	//how many data send in total
	static int DataPackageSize = 50; 	//how many data send together
	static String prefix ;
	static String suffix ;
	static String httpURI ="tcp://ec2-52-58-177-76.eu-central-1.compute.amazonaws.com:1883";
	static String gatewayURI ="coap://10.20.220.188:5683/IoTResource/";
	static String path ;
//	static String rdfData;
	static DESTINATION destination;
	static CoapHandler coapHandler;
	static int totalRequest=0;
	static TimeCounter counter;
	static long timer;//used for accumulate the transformation time from each nodes
	static int finishNumber;//count for how  many nodes has been finished transforming

	
	static public enum DESTINATION{
		TO_CLOUD_SERVER,
		TO_GATEWAY
	}
	
	//ps: by doing this, the size is fix
	static List<String> IoTReasonerAddress = Arrays.asList(
			"192.168.0.107"
			//"10.20.218.148", 
			//"10.20.210.145"
			);
    public static void main( String[] args )
    {

    	System.out.println( System.getProperty("os.name") );
		switch( System.getProperty("os.name") ){
			case "Windows 7":{
				path = "C:/Users/"+System.getProperty("user.name")+"/Documents";
				break;
			}
			case "Windows 10":{
				path = "C:/Users/"+System.getProperty("user.name")+"/Documents";
				break;
			}
			case "Linux":{
				path = "/home/"+System.getProperty("user.name");
				break;
			}
		}
		
		prefix = path+"/MSCThesis/ArtificalData/";
		suffix = ".rdf";
		
		
		String command="";
		//do{
//		rdfData = "";
		Scanner keyboard = new Scanner(System.in);
		System.out.println("This is IoTNode, command:");
		System.out.println("        1. Send RDF data to the Cloud Server");
		System.out.println("        2. Send RDF data to the Gateway");
		System.out.println("        exit. To exit the system");
		command = keyboard.next();
		
//			if(command == "exit"){
//				break;
//			}
		switch(command){
			case "1":{
				destination=DESTINATION.TO_CLOUD_SERVER;
				break;
			}
			case "2":{
				destination=DESTINATION.TO_GATEWAY;
				break;
			}
		} 
		
		//set up timer
		timer =0;
		finishNumber =0;
		
		//using threads
    	counter = new TimeCounter(NumberOfThreads);
    	for (int i = 1; i <= NumberOfThreads; i++) {
    		Thread t = new JobSlice(counter,i,destination);
    		t.start();
    	}
	//}while(command != "exit");
    	
    	//without using threads
//	    	for (int i = 0; i < NumberOfThreads; i++) {
//	    		snedRDFFromCoAP(i);
//	    	}
	
    	keyboard.next();
    }
    static class JobSlice extends Thread {
    	
		private TimeCounter latch;
		private int n;
		private int clientId;
		private DESTINATION destination;
		int dataProcessed;//how many rdf data has processed
		public JobSlice(TimeCounter l, int i, DESTINATION destination) {
			this.n=i;
			this.clientId = i;
			this.latch = l;
			this.destination = destination;
			dataProcessed = 0;

		}
		public void run() {
			List<String> rdfList= new ArrayList<String>();
			String rdfData = null;
			try {
				FileInputStream inputStream = null;

				try {

					while(dataProcessed < App.DataSize){
						rdfData = "";
						for (int j = 1; j <= DataPackageSize && (dataProcessed < App.DataSize) ; j++) {
							dataProcessed++;
							


							inputStream 	= new FileInputStream( prefix+clientId+ "\\incident_"+ dataProcessed + suffix);
							String result	= getStringFromInputStream(inputStream);
							//result			= result.substring(127,result.length()-10); //remove "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:obs=\"http://localhost/SensorSchema/ontology#\">" and "</rdf:RDF>"
							result			= result.substring(125,result.length()-10);
							rdfData 		+= result;


						}
						rdfData = "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:obs=\"http://localhost/SensorSchema/ontology#\">"+rdfData+"</rdf:RDF>";
						rdfList.add(rdfData);
						//System.out.println(rdfData);
					}

				} catch (FileNotFoundException e) {
					System.out.println("File Not Found. "+ prefix);
				}
				finally {
					
					try {
						inputStream.close();
					} catch (IOException e) {
					}
				}
				
				switch(destination){
					case TO_CLOUD_SERVER:{
//						MQTTclient_pub client = new MQTTclient_pub( httpURI,"IoTReasoning",clientId+"" );
//						client.publish(rdfData);
						IoTMqttClient Mclient = new IoTMqttClient( httpURI,"IoTReasoning" , clientId+"");
						for(String rdf : rdfList){
							Mclient.publish(rdf);
//							System.out.println(rdf);
						}
						//Mclient.close();
						
						
						break;
					}
					case TO_GATEWAY:{
						//MQTTclient_pub client = new MQTTclient_pub( httpURI,"IoTReasoning",clientId );
						//client.publish(rdfData);
						//CoapClient_pub.put(coapHandler,"coap://192.168.43.162:5683/IoTReasoner/"+clientId ,rdfData);
						//CoapClient_pub.put(coapHandler,gatewayURI+clientId ,rdfData);
						String address =IoTReasonerAddress.get(clientId%IoTReasonerAddress.size());
						System.out.println("coap://"+address+":5683/IoTReasoner/"+clientId);
						IoTCoapClient coapClient = new IoTCoapClient("coap://"+address+":5683/IoTReasoner/"+clientId, new CoapHandlerTimer());
						for(String rdf : rdfList){
//							System.out.println(rdf);
							coapClient.put(rdf);
							
						}
						coapClient.close();
						
						
						
						break;
					
					}
				}
				
			} finally {
				//latch.countDown();
			}
		}
	}
    private static String getStringFromInputStream(FileInputStream inputStream) {
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {

			br = new BufferedReader(new InputStreamReader(inputStream));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return sb.toString();

	}
    public static void timerIncrease(long time){
    	App.timer+=time;
    	App.finishNumber++;
    	if(finishNumber == NumberOfThreads*(DataSize/DataPackageSize)){
    		System.out.println("Finish transmission. Transmission time: " + timer +" ("+timer/1000 +" seconds) "+timer/NumberOfThreads+" for each node." );
    	}
    }

    public static class TimeCounter {
		private final Object synchObj = new Object();
		private int count;
		private long startTime;

		public TimeCounter(int NumberOfThreads) {
			startTime =  (new Date()).getTime();
			synchronized (synchObj) {
				this.count = NumberOfThreads;
			}
		}
		public void awaitZero() throws InterruptedException {
			synchronized (synchObj) {
				while (count > 0) {
					synchObj.wait();
				}
			}
		}
		public void countDown() {
			synchronized (synchObj) {
				if (--count <= 0) {
					synchObj.notifyAll();

					System.out.println((new Date()).getTime() - startTime);
					//TODO: post reasoning can be done here, get triples from database and reason
				}
			}
		}
	}
    public static class CoapHandlerTimer implements CoapHandler{
    	
    	Date startTime;
    	
    	public CoapHandlerTimer( ){
    		startTime= new Date();;
    		//System.out.println("create handler.");
    	}

    	@Override public void onLoad(CoapResponse response) {
			String content = response.getResponseText();
			System.out.println("Total request: "+ (++totalRequest)+" ,RESPONSE : " + content);
			long time = (new Date()).getTime()-startTime.getTime();
			App.timerIncrease(time);
			//counter.countDown();
		}
		
		@Override public void onError() {
			System.err.println("FAILED");
			//counter.countDown();
		}
    	
    }
}
