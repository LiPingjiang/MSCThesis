package com.pli.RDFManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.expr.E_StrLength;
import org.apache.jena.tdb.solver.stats.Stats;
import org.apache.jena.vocabulary.VCARD;

/***
 * 
 * @author pli
 *
 * This is to generate and extend RDF data 
 */
public class App 
{
	public static String ontologyURI = "http://localhost/SensorSchema/ontology#";
    public static void main( String[] args )
    {
    	
    	
    	//createFolder(500);
    	
    	//transferRDFFiles();
    	
    	//generateArtificialData_json();
    	
//    	generateArtificialData_n3();
 
    	generateArtificialData();
//    	generateArtificialData_en();
    	

    	
    	
    }



	private static void changeProperty(){
    	String personURI    = "http://somewhere/JohnSmith";
    	String fullName     = "John Smith";
    	Model model = ModelFactory.createDefaultModel();
    	Resource johnSmith =
    	        model.createResource(personURI)
    	             .addProperty(VCARD.FN, fullName);
    	
    	model.write(System.out);
    	
    	StmtIterator  stas = model.listStatements();
    	while(stas.hasNext()){
    		Statement sta = stas.next();
    		if( sta.getObject().isLiteral() ){

    			if( sta.getPredicate().getURI().toString().equals( VCARD.FN.getURI() ) ){
    				model.remove(sta);
    				//sta.changeObject("Sim");
    				//System.out.println(sta.toString());
    				model.createResource(personURI).addProperty(VCARD.FN, "Simo");
    			}
    			break;
    		}
    	}
    	model.write(System.out);
    }
    
    /**
     * Generate artificial data
     * carNumbers defines how many car intotal
     * eachNode defines how many RDF data each car has
     * 
     * 
     * */
	private static void generateArtificialData() {
		
		int carNumbers = 160 ;
		int eachNode = 800; //how many RDF for each node
		
		for(int index = 1; index <= carNumbers ; index ++){
			new File("C:\\Users\\pli\\Documents\\MSCThesis\\ArtificalData\\"+index ).mkdirs();
			//new File("home/"+System.getProperty("user.name")+"/Desktop/MSCThesis/ArtificalData/"+index ).mkdirs();
		}
		
		//total 
		//30 * 2400 = 72000
		long timestamp=1365156007506L;
		long ID=185600000L;
    	for(int index = 0; index < carNumbers*eachNode ; index ++){
    		Model model = ModelFactory.createDefaultModel() ;
        	model.read("C:\\Users\\pli\\Desktop\\obs_data_individuals_rdf\\incident_"+ (15000+index%72000 ) +".rdf") ;
        	
        	NodeIterator objs = model.listObjects();
        	//System.out.println(objs.hasNext() );
        	
        	
        	
        	StmtIterator  stas = model.listStatements();
        	Model tempModel = ModelFactory.createDefaultModel() ;
        	tempModel.setNsPrefix("obs", ontologyURI);
        	
        	Resource subject=null;
        	while(stas.hasNext()){
        		
        		Statement sta = stas.next();
        		
        		if( sta.getObject().isLiteral() ){
        			if( sta.getPredicate().getURI().toString().equals( "http://localhost/SensorSchema/ontology#hasID" ) ){
        				//System.out.println("-------------------------------------------------------------");
            			//System.out.print("No. "+index+"\tCarID : " + sta.getString() + "\t");
        				//sta.changeLiteralObject( index/2400+1 );
        				//String subject = sta.getSubject().getURI();

        				//model.remove(sta);
        				//System.out.println(sta.getPredicate().getLocalName());
        				//sta.changeObject("Sim");
        				//System.out.println(sta.toString());
        				//model.createResource(sta.getSubject().getURI()).addProperty( ResourceFactory.createProperty( "http://localhost/SensorSchema/ontology", "hasID") , (index/2400+1)+"", "obs:hasID" );
        				//model.createResource(sta.getSubject().getURI()).addLiteral( ResourceFactory.createProperty( "http://localhost/SensorSchema/ontology#", "hasID") , (index/eachNode+1) );
        				//tempModel.createResource(sta.getSubject().getURI()).addLiteral( ResourceFactory.createProperty( "http://localhost/SensorSchema/ontology#", "hasID") , (index/eachNode+1) );
        				tempModel.createResource(sta.getSubject().getURI()).addLiteral( ResourceFactory.createProperty( "http://localhost/SensorSchema/ontology#", "hasID") , ID++ );
        				
        				
        				//sta.changeLiteralObject( 100 );
        				//System.out.println(sta.toString());
        				//System.out.print("No. "+index+"\tCarID : " + sta.getInt() + "\t");
        				
            			// Wriet Turtle to the blocks variant
            		    
            		    subject = sta.getSubject();
            		    
            		    //break;
        			}
        			
        			else if( sta.getPredicate().getURI().toString().equals( "http://localhost/SensorSchema/ontology#hasSender" ) ){
        				//model.remove(sta);
        				//model.createResource(sta.getSubject().getURI()).addLiteral( ResourceFactory.createProperty( "http://localhost/SensorSchema/ontology#", "hasSender") , 51709000+(index/eachNode+1) );
        				tempModel.createResource(sta.getSubject().getURI()).addLiteral( ResourceFactory.createProperty( "http://localhost/SensorSchema/ontology#", "hasSender") , 51709000+(index/eachNode+1) );
        				
        			}
        			else if( sta.getPredicate().getURI().toString().equals( "http://localhost/SensorSchema/ontology#hasDate" ) ){
        				//model.remove(sta);
        				//model.createResource(sta.getSubject().getURI()).addLiteral( ResourceFactory.createProperty( "http://localhost/SensorSchema/ontology#", "hasDate") , (timestamp++) );
        				tempModel.createResource(sta.getSubject().getURI()).addLiteral( ResourceFactory.createProperty( "http://localhost/SensorSchema/ontology#", "hasDate") , (timestamp++) );
        				
        			}else{
        				tempModel.add(sta);
        			}
        			
        		}
        		
        	}
        	
        	tempModel.add(ResourceFactory.createStatement( subject , ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#","type") , ResourceFactory.createProperty("http://localhost/SensorSchema/ontology#", "Observation")) );
			

        	FileWriter out = null;
		    
		    try {
		        //out = new FileWriter( "C:\\Users\\pli\\Documents\\MSCThesis\\ArtificalData\\"+ ( index/eachNode +1) +"\\incident_"+ (index%eachNode) +".rdf" );
		    	out = new FileWriter( "P:\\Desktop\\MSCThesis\\ArtificalData\\"+ ( index/eachNode +1) +"\\incident_"+ (index%eachNode) +".rdf" );
		    	
		    	System.out.println("Write to C:\\Users\\pli\\Documents\\MSCThesis\\ArtificalData\\"+ ( index/eachNode+1 ) +"\\incident_"+ (index%eachNode) +".rdf");
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		    	System.out.println("Fail to write! ");
		        e.printStackTrace();
		    }
		    tempModel.write(out);
    	}
		
	}
	
	private static void generateArtificialData_json() {
		
		int carNumbers = 160 ;
		int eachNode = 800; //how many RDF for each node
		
		for(int index = 1; index <= carNumbers ; index ++){
			new File("C:\\Users\\pli\\Documents\\MSCThesis\\ArtificalData\\"+index ).mkdirs();
		}
		
    	for(int index = 0; index < carNumbers*eachNode ; index ++){
    		Model model = ModelFactory.createDefaultModel() ;
        	model.read("C:\\Users\\pli\\Desktop\\obs_data_individuals_rdf\\incident_"+ (15000+index%72000 ) +".rdf") ;
        	
        	NodeIterator objs = model.listObjects();   	
        	
        	StmtIterator  stas = model.listStatements();
        	while(stas.hasNext()){
        		
        		Statement sta = stas.next();
        		
        		if( sta.getObject().isLiteral() ){
        			if( sta.getPredicate().getURI().toString().equals( "http://localhost/SensorSchema/ontology#hasID" ) ){

        				model.remove(sta);
        				
        				model.createResource(sta.getSubject().getURI()).addLiteral( ResourceFactory.createProperty( "http://localhost/SensorSchema/ontology#", "hasID") , (index/eachNode+1) );
        				
        				FileWriter out = null;
            		    
            		    try {
            		    	out = new FileWriter( "C:\\Users\\pli\\Documents\\MSCThesis\\ArtificalData\\"+ ( index/eachNode +1) +"\\incident_"+ (index%eachNode) +".json" );
            		        System.out.println("Write to C:\\Users\\pli\\Documents\\MSCThesis\\ArtificalData\\"+ ( index/eachNode+1 ) +"\\incident_"+ (index%eachNode) +".json");
            		    
            		    } catch (IOException e) {
            		        // TODO Auto-generated catch block
            		    	System.out.println("Fail to write! ");
            		        e.printStackTrace();
            		    }
            		    model.write(out,"JSON-LD");
            		    break;
        			}
        		}
        	}
        	
    	}
		
	}
	
	private static void generateArtificialData_n3() {
		
		int carNumbers = 160 ;
		int eachNode = 800; //how many RDF for each node
		
		for(int index = 1; index <= carNumbers ; index ++){
			new File("C:\\Users\\pli\\Documents\\MSCThesis\\ArtificalData\\"+index ).mkdirs();
		}
		
    	for(int index = 0; index < carNumbers*eachNode ; index ++){
    		Model model = ModelFactory.createDefaultModel() ;
        	model.read("C:\\Users\\pli\\Desktop\\obs_data_individuals_rdf\\incident_"+ (15000+index%72000 ) +".rdf") ;
        	
        	NodeIterator objs = model.listObjects();   	
        	
        	StmtIterator  stas = model.listStatements();
        	while(stas.hasNext()){
        		
        		Statement sta = stas.next();
        		
        		if( sta.getObject().isLiteral() ){
        			if( sta.getPredicate().getURI().toString().equals( "http://localhost/SensorSchema/ontology#hasID" ) ){

        				model.remove(sta);
        				
        				model.createResource(sta.getSubject().getURI()).addLiteral( ResourceFactory.createProperty( "http://localhost/SensorSchema/ontology#", "hasID") , (index/eachNode+1) );
        				
        				FileWriter out = null;
            		    
            		    try {
            		    	out = new FileWriter( "C:\\Users\\pli\\Documents\\MSCThesis\\ArtificalData\\"+ ( index/eachNode +1) +"\\incident_"+ (index%eachNode) +".n3" );
            		        System.out.println("Write to C:\\Users\\pli\\Documents\\MSCThesis\\ArtificalData\\"+ ( index/eachNode+1 ) +"\\incident_"+ (index%eachNode) +".n3");
            		    
            		    } catch (IOException e) {
            		        // TODO Auto-generated catch block
            		    	System.out.println("Fail to write! ");
            		        e.printStackTrace();
            		    }
            		    model.write(out,"N-TRIPLE");
            		    break;
        			}
        		}
        	}
        	
    	}
		
	}

	private static void generateArtificialData_en() {
		
		String ID = null;
		String hasArea = null;
		String hasLatitude = null;
		String hasLongitude = null;
		String hasVelocity = null;
		String hasDirection = null;
		String hasSender = null;
		String hasDistance = null;
		String hasAcceleration = null;
		String hasDateTime = null;
		String hasDate = null;
		
		int carNumbers = 10 ;
		int eachNode = 400; //how many RDF for each node
		
		for(int index = 1; index <= carNumbers ; index ++){
			new File("C:\\Users\\pli\\Documents\\MSCThesis\\ArtificalData\\"+index ).mkdirs();
		}
		
    	for(int index = 0; index < carNumbers*eachNode ; index ++){
    		Model model = ModelFactory.createDefaultModel() ;
        	model.read("C:\\Users\\pli\\Desktop\\obs_data_individuals_rdf\\incident_"+ (15000+index%72000 ) +".rdf") ;
        	
        	NodeIterator objs = model.listObjects();   	
        	
        	StmtIterator  stas = model.listStatements();
        	while(stas.hasNext()){
        		
        		Statement sta = stas.next();
        		
        		if( sta.getObject().isLiteral() ){
        			ID = (index/eachNode+1)+"";
        			
        			if( sta.getPredicate().getURI().toString().equals( "http://localhost/SensorSchema/ontology#hasArea" ) ){
        				hasArea =sta.getObject().asLiteral().getString();	;	
        			}
        			else if( sta.getPredicate().getURI().toString().equals( "http://localhost/SensorSchema/ontology#hasArea" ) ){
        				hasArea =sta.getObject().asLiteral().getString();	;	
        			}
        			else if( sta.getPredicate().getURI().toString().equals( "http://localhost/SensorSchema/ontology#hasLatitude" ) ){
        				hasLatitude =sta.getObject().asLiteral().getString();	;	
        			}
        			else if( sta.getPredicate().getURI().toString().equals( "http://localhost/SensorSchema/ontology#hasLongitude" ) ){
        				hasLongitude = sta.getObject().asLiteral().getString();	
        			}
        			else if( sta.getPredicate().getURI().toString().equals( "http://localhost/SensorSchema/ontology#hasVelocity" ) ){
        				hasVelocity = sta.getObject().asLiteral().getString();	
        			}
        			else if( sta.getPredicate().getURI().toString().equals( "http://localhost/SensorSchema/ontology#hasDirection" ) ){
        				hasDirection = sta.getObject().asLiteral().getString();	
        			}
        			else if( sta.getPredicate().getURI().toString().equals( "http://localhost/SensorSchema/ontology#hasSender" ) ){
        				hasSender = sta.getObject().asLiteral().getString();	
        			}
        			else if( sta.getPredicate().getURI().toString().equals( "http://localhost/SensorSchema/ontology#hasDistance" ) ){
        				hasDistance = sta.getObject().asLiteral().getString();	
        			}
        			else if( sta.getPredicate().getURI().toString().equals( "http://localhost/SensorSchema/ontology#hasAcceleration" ) ){
        				hasAcceleration = sta.getObject().asLiteral().getString();	
        			}
        			else if( sta.getPredicate().getURI().toString().equals( "http://localhost/SensorSchema/ontology#hasDateTime" ) ){
        				hasDateTime = sta.getObject().asLiteral().getString();	
        			}
        			else if( sta.getPredicate().getURI().toString().equals( "http://localhost/SensorSchema/ontology#hasDate" ) ){
        				hasDate = sta.getObject().asLiteral().getString();	
        			}
        			
        			
        			

        		}
        	}
        	FileWriter out = null;
        	BufferedWriter bw = null;
		    try {
		    	out = new FileWriter( "C:\\Users\\pli\\Documents\\MSCThesis\\ArtificalData\\"+ ( index/eachNode +1) +"\\incident_"+ (index%eachNode) +".en" );
		        System.out.println("Write to C:\\Users\\pli\\Documents\\MSCThesis\\ArtificalData\\"+ ( index/eachNode+1 ) +"\\incident_"+ (index%eachNode) +".en");
		        bw = new BufferedWriter( out );
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		    	System.out.println("Fail to write! ");
		        e.printStackTrace();
		    }
		    //model.write(out,"N-TRIPLE");
		    String enPacket = "[urn:uuid:7bcf39 ";
            enPacket += ID+" "+ hasArea +" "+ hasLatitude +" "+ hasLongitude +
                    " "+ hasVelocity +" "+  hasDirection +" "+ hasSender +" "+ hasDistance +" "
                    + hasAcceleration +" "+ hasDate +" "+ hasDateTime ;

            enPacket += "]";
            
            //System.out.println(enPacket);
            try {
            	bw.write( enPacket );
                bw.close( );
//				out.write(enPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
    	}
		
	}

	
	private static void transferRDFFiles() {
		//total 8859
    	for(int index = 0; index <= 73859 ; index ++){
    		Model model = ModelFactory.createDefaultModel() ;
        	model.read("C:\\Users\\pli\\Desktop\\obs_data_individuals_rdf\\incident_"+ (15000+index) +".rdf") ;
        	
        	NodeIterator objs = model.listObjects();
        	//System.out.println(objs.hasNext() );
        	
        	
        	StmtIterator  stas = model.listStatements();
        	while(stas.hasNext()){
        		
        		Statement sta = stas.next();
        		
        		if( sta.getObject().isLiteral() ){
        			if( sta.getPredicate().getURI().toString().equals( "http://localhost/SensorSchema/ontology#hasID" ) ){
        				//System.out.println("-------------------------------------------------------------");
            			System.out.print("No. "+index+"\tCarID : " + sta.getString() + "\t");
            			 // Wriet Turtle to the blocks variant
            		    
            		    FileWriter out = null;
            		    
            		    try {
            		        out = new FileWriter( "C:\\Users\\pli\\Desktop\\obs_by_car_ID\\"+sta.getString()+"\\incident_"+ (15000+index) +".rdf" );
            		        System.out.println("Write to C:\\Users\\pli\\Desktop\\obs_by_car_ID\\"+sta.getString()+"\\incident_"+ (15000+index) +".rdf");
            		    } catch (IOException e) {
            		    	System.out.println("Fail to write! ");
            		        e.printStackTrace();
            		    }
            		    model.write(out);
        			}
        		}
        	}
    	}

    	
    	System.out.println("Finish.");
		
	}

	private static void createFolder(int end) {


		for(int index = 1; index <= end ; index ++){
			new File("C:\\Users\\pli\\Desktop\\obs_by_car_ID\\"+index ).mkdirs();
		}
		System.out.println("Folders created!.");
		
	}
	
}
