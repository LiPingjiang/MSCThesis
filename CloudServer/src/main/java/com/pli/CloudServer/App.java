package com.pli.CloudServer;

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
    	reasoningTimer = 0;
        System.out.println( "IoT Reasoning is running!" );
        
		if(args.length==0)                     
			System.out.println("No command line arguments");
		else{
			  System.out.println("Reasoning Type"+args[1]);
			  rdfFormat = args[1];
		}
        
        
        client = new MQTTclient_sub(
        		//"tcp://ec2-52-58-177-76.eu-central-1.compute.amazonaws.com:1883",
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
