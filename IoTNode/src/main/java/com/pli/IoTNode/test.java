package com.pli.IoTNode;

import java.util.Scanner;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import com.pli.IoTNode.App.CoapHandlerTimer;

public class test {

	public static void main( String[] args ){
		
		//IoTCoapClient.get();
		//CoapClient_pub.put("coap://192.168.0.107:5683/IoTReasoner","The test message.");
//		IoTCoapClient coapClient = new IoTCoapClient("coap://192.168.0.107:5683/IoTReasoner", new CoapHandlerTimer());
//		coapClient.put("The test message.");
		
		String uri="coap://192.168.0.107:5683/IoTReasoner";
		CoapClient client = new CoapClient(uri);
		
		//client.useEarlyNegotiation(82);
		client.useLateNegotiation();

		//CoapResponse response = client.put(data, MediaTypeRegistry.TEXT_PLAIN);
//		CoapHandler	coapHandler = new CoapHandler() {
//			
//			@Override public void onLoad(CoapResponse response) {
//				String content = response.getResponseText();
//				//System.out.println("Total request: "+ (++totalRequest)+" ,RESPONSE : " + content);
//				//counter.countDown();
//			}
//			
//			@Override public void onError() {
//				System.err.println("FAILED");
//				//counter.countDown();
//			}
//		};
		CoapHandler	coapHandler = new CoapHandlerTimer();
		//asynchronous
		client.put( coapHandler ,"test", MediaTypeRegistry.APPLICATION_RDF_XML);
		
		//System.out.println("send!");
		Scanner keyboard = new Scanner(System.in);
		keyboard.next();
	}
}
