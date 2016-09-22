package com.pli.IoTNode;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;



/**
 * Hello world!
 *
 */
public class App 
{
	
	static int NumberOfThreads = 10;	//how many threads(IoTNodes)
	static int DataSize = 400;
	//static String httpURI ="tcp://52.58.195.130:1883";
	//static String httpURI ="tcp://vm0104.virtues.fi:1883";
	static String httpURI ="tcp://ec2-52-59-70-81.eu-central-1.compute.amazonaws.com:1883";
	//static String httpURI ="tcp://130.231.50.129:1883";
	//ps: by doing this, the size is fix
		static List<String> IoTReasonerAddress = Arrays.asList(
				"192.168.43.142"
				//"10.20.210.145"
//				"10.20.222.81",
				//"10.20.218.106",
				
				
//				"10.20.201.66",
//				"10.20.195.183"
				
//				"10.20.218.148",//pad
//				"10.20.195.183",//golden lg
//				"10.20.220.172",//whit lg
//				"10.20.196.220",//huawei
//				"10.20.223.118",
//				"10.20.217.137"
				
				);
	
	
	
	static int DataPackageSize = 50; 	//how many data send together
//	static int incidentStartNumber = 15000;
	static String prefix ;
	static String suffix ;

	static String gatewayURI ="coap://10.20.220.188:5683/IoTResource/";
	static String path ;
//	static String rdfData;
	static DESTINATION destination;
	static CoapHandler coapHandler;
	static int totalRequest=0;
//	static TimeCounter counter;
	static long timer;//used for accumulate the transformation time from each nodes
	static int finishNumber;//count for how  many nodes has been finished transforming
	
	static Date totalStartTime;

	
	static public enum DESTINATION{
		TO_CLOUD_SERVER,
		TO_GATEWAY,
	}
	
	
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
				path = "/home/"+System.getProperty("user.name")+"/Desktop";
				break;
			}
		}
		
		prefix = path+"/MSCThesis/ArtificalData/";
		suffix = ".rdf";
		
		
		String command="";
		//do{
//		rdfData = "";
//		@SuppressWarnings({ "resource", "resource" })
		Scanner keyboard = new Scanner(System.in);
		System.out.println("This is IoTNode, command:");
		System.out.println("        1. Send RDF data to the Cloud Server");
		System.out.println("        2. Send JSON-LD data to the Cloud Server");
		System.out.println("        3. Send N3 data to the Cloud Server");
		System.out.println("        4. Send EN data to the Cloud Server");
		System.out.println("        5. Send RDF data to the Gateway");
		System.out.println("        6. Send JSON-LD data to the Gateway");
		System.out.println("        7. Send N3 data to the Gateway");
		System.out.println("        8. Send EN data to the Gateway");
		System.out.println("        exit. To exit the system");
		command = keyboard.next();
		
//			if(command == "exit"){
//				break;
//			}
		switch(command){
			case "1":{
				suffix = ".rdf";
				destination=DESTINATION.TO_CLOUD_SERVER;
				break;
			}
			case "2":{
				suffix = ".json";
				destination=DESTINATION.TO_CLOUD_SERVER;
				break;
			}
			case "3":{
				suffix = ".n3";
				destination=DESTINATION.TO_CLOUD_SERVER;
				break;
			}
			case "4":{
				suffix = ".en";
				destination=DESTINATION.TO_CLOUD_SERVER;
				break;
			}
			case "5":{
				suffix = ".rdf";
				destination=DESTINATION.TO_GATEWAY;
				break;
			}
			case "6":{
				suffix = ".json";
				destination=DESTINATION.TO_GATEWAY;
				break;
			}
			case "7":{
				suffix = ".n3";
				destination=DESTINATION.TO_GATEWAY;
				break;
			}
			case "8":{
				suffix = ".en";
				destination=DESTINATION.TO_GATEWAY;
				break;
			}
		} 
		
		//System.out.println("Input IoTNode number:");
		//keyboard = new Scanner(System.in);
		//NumberOfThreads = Integer.parseInt (keyboard.next());
		
		//System.out.println("Input datasize per node:");
		//keyboard = new Scanner(System.in);
		//DataSize = Integer.parseInt (keyboard.next());
		
		System.out.println("Input experiment ID:");
		keyboard = new Scanner(System.in);
		int[] nt={0, 20,  40,  60,  80,  100,  40,  60,  80,  100, 60,  90,  120, 150, 60,  60,  60,  60};
		int[] ds={0, 400, 400, 400, 400, 400,  800, 533, 400, 320, 533, 355, 266, 213, 200, 400, 600, 800};
		int task = keyboard.nextInt();
		NumberOfThreads = nt[task];
		DataSize = ds[task];
		
		
		//set up timer
		timer =0;
		finishNumber =0;
		
		totalStartTime =new Date();
		//using threads
//    	counter = new TimeCounter(NumberOfThreads);
    	for (int i = 1; i <= NumberOfThreads; i++) {
    		Thread t = new JobSlice(i,destination);
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
    	
		//private TimeCounter latch;
		//private int n;
		private int clientId;
		private DESTINATION destination;
		int dataProcessed;//how many rdf data has processed
		public JobSlice(int i, DESTINATION destination) {
			//this.n=i;
			this.clientId = i;
			//this.latch = l;
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
							

							if (suffix.equals(".en")){
								inputStream 	= new FileInputStream( prefix+clientId+ "/incident_"+ (dataProcessed-1) + ".en");
								
								String result	= getStringFromInputStream(inputStream);
								String backup = result;

								//result = result.substring(1,result.length()-1);
								//System.out.println("result"+result);
								
								if(j==1)
									rdfData = result;
								else
									rdfData += "__" + result; 
							}else{
								//inputStream 	= new FileInputStream( prefix+clientId+ "/incident_"+ (dataProcessed-1) + suffix);
								inputStream 	= new FileInputStream( prefix+clientId+ "/incident_"+ (dataProcessed-1) + ".rdf");
								
								String result	= getStringFromInputStream(inputStream);
								String backup = result;

								result = result.substring(125,result.length()-10);

								rdfData += result;
							}
							


						}
						if (suffix.equals(".en")){
							
						}else{
							rdfData = "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:obs=\"http://localhost/SensorSchema/ontology#\">"+rdfData+"</rdf:RDF>";
						}
						
						if( suffix.equals(".json") ){
//							JenaJSONLD.init();
							Model model = ModelFactory.createDefaultModel();
							try {
								model.read(IOUtils.toInputStream(rdfData, "UTF-8"), null, "RDF/XML");
				    		} catch (IOException e) {
				    			// TODO Auto-generated catch block
				    			System.out.println("run crash.");
				    			e.printStackTrace();
				    		}
			
							OutputStream os = new ByteArrayOutputStream();
							model.write(os, "JSON-LD");
							rdfData = os.toString();
						}else if(suffix.equals(".n3")){
							Model model = ModelFactory.createDefaultModel();
							try {
								model.read(IOUtils.toInputStream(rdfData, "UTF-8"), null, "RDF/XML");
				    		} catch (IOException e) {
				    			// TODO Auto-generated catch block
				    			System.out.println("run crash.");
				    			e.printStackTrace();
				    		}
			
							OutputStream os = new ByteArrayOutputStream();
							model.write(os, "N-TRIPLE");
							rdfData = os.toString();
						}
//						System.out.println(rdfData);
						rdfList.add(rdfData);
												
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

						IoTMqttClient Mclient = new IoTMqttClient( httpURI,"IoTReasoning" , clientId+"");
						for(String rdf : rdfList){

							Mclient.publish(rdf);
//							timerIncrease(2);//test
						}
						
						break;
					}
					case TO_GATEWAY:{
						//MQTTclient_pub client = new MQTTclient_pub( httpURI,"IoTReasoning",clientId );
						//client.publish(rdfData);
						//CoapClient_pub.put(coapHandler,"coap://192.168.43.162:5683/IoTReasoner/"+clientId ,rdfData);
						//CoapClient_pub.put(coapHandler,gatewayURI+clientId ,rdfData);
						String address =IoTReasonerAddress.get(clientId%IoTReasonerAddress.size());
						Date startTime = new Date();
						Socket socket=null;
						try {
							System.out.println(address);
							socket = new Socket(address, 6000);
						} catch (UnknownHostException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						PrintWriter out = null;
						try {

							out = new PrintWriter(new BufferedWriter(
									new OutputStreamWriter(socket.getOutputStream())),
									true);
							
						} catch (UnknownHostException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
						int index = 1;
						for(String rdf : rdfList){
							rdf = rdf.replace("\n", "").replace("\r", "");
//							System.out.println(rdf);
							//IoTCoapClient coapClient = new IoTCoapClient("coap://"+address+":5683/IoTReasoner/"+clientId, new CoapHandlerTimer());
							//System.out.println("coap://"+address+":5683/IoTReasoner/"+clientId+"/"+index);
							//coapClient.put(rdf);
							//index ++;//contact to different resource so no "Wrong block number" error
							
							//socket synchronize
							//https://examples.javacodegeeks.com/android/core/socket-core/android-socket-example/
							
							out.println(rdf);
							Date endTime = new Date();
							//System.out.println("time: "+(endTime.getTime()-startTime.getTime()));
							App.timerIncrease( endTime.getTime()-startTime.getTime() );
					        
					        
					        
					        
					        index++;
						}
						if(socket!=null){
				        	try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
				        	try {
								socket.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
				        }
						//coapClient.close();
						
						
						
						break;
					
					}
					
				}
				
			} finally {
				
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
    public static synchronized void timerIncrease(long time){
    	
    	App.timer+=time;
    	App.finishNumber++;
    	
    	//System.out.println( "finishNumber"+finishNumber + "  NumberOfThreads*(DataSize/DataPackageSize): "+  NumberOfThreads*Math.ceil(( (float)DataSize/(float)DataPackageSize)) );
    	
    	if(finishNumber == NumberOfThreads*Math.ceil(( (float)DataSize/(float)DataPackageSize))){
    		System.out.println("Finish transmission. Transmission time: " + timer +" ("+timer/1000 +" seconds) "+timer/NumberOfThreads+" for each node." );
    		System.out.println("Transmision time in total: " + ((new Date()).getTime()-totalStartTime.getTime()) + " finish number: "+ finishNumber );
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
