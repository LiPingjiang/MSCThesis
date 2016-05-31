package com.pli.IoTNode;

import java.util.Date;
import java.util.logging.Handler;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import com.pli.IoTNode.App.CoapHandlerTimer;

public class IoTCoapClient {
	String uri;
	Date startTime;
	CoapClient client = new CoapClient(uri);
	CoapHandler handler;
	
	IoTCoapClient(String uri, CoapHandler handler){
		
		this.handler = handler;
		client = new CoapClient(uri);
	}
	
	public void get(){
		
		//uri="coap://coap.me:5683/";
		uri="coap://10.20.220.188:5683/IoTResource";
		CoapClient client = new CoapClient(uri);
		//client.setTimeout(0);
		//client.useEarlyNegotiation(82);
		client.useLateNegotiation();

		CoapResponse response = client.get();
		
		if (response!=null) {
			
			System.out.println(response.getCode());
			System.out.println(response.getOptions());
			System.out.println("ResponseText: "+response.getResponseText());
			
			System.out.println("\nADVANCED\n");
			// access advanced API with access to more details through .advanced()
			System.out.println(Utils.prettyPrint(response));
			
		} else {
			System.out.println("No response received.");
		}
	}
	public void put(String data){
		
		//uri="coap://192.168.0.107:5683/IoTReasoner";
		
		

		//CoapResponse response = client.put(data, MediaTypeRegistry.TEXT_PLAIN);
		//Synchronize
		//CoapResponse response;

		//response = client.put(data, MediaTypeRegistry.APPLICATION_RDF_XML);

		
//		if (response!=null) {
//			
//			System.out.println(response.getCode());
//			System.out.println(response.getOptions());
//			System.out.println(response.getResponseText());
//			
//			System.out.println("\nADVANCED\n");
//			// access advanced API with access to more details through .advanced()
//			System.out.println(Utils.prettyPrint(response));
//			
//		} else {
//			System.out.println("No response received.");
//		}
		
		//asynchronous
		client.put( handler,data, MediaTypeRegistry.APPLICATION_RDF_XML);
		
		//System.out.println("send!");
	}

	public void close() {
		client.shutdown();
		
	}
}
