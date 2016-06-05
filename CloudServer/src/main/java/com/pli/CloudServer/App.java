package com.pli.CloudServer;

import java.util.Scanner;

/**
 * Auther Pingjiang Li
 * Data 2016.05.19 
 *
 */
public class App
{
	public static MQTTclient_sub client;
	public static long reasoningTimer;
	public static String rdfFormat="RDF/XML";
	
    public static void main( String[] args )
    {
    	String command="";
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
    public static synchronized void timerIncrease( long time){
    	reasoningTimer += time;
    	System.out.println( "Reasoning time: " + reasoningTimer + "("+ reasoningTimer/1000+" seconds)" );
    	
    }
    
}
