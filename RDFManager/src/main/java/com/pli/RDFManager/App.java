package com.pli.RDFManager;

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
import org.apache.jena.vocabulary.VCARD;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	
    	
    	//createFolder(500);
    	
    	//transferRDFFiles();
    	
    	generateArtificialData();
 

    	
    	
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
		}
		
		//total 
		//30 * 2400 = 72000
    	for(int index = 0; index < carNumbers*eachNode ; index ++){
    		Model model = ModelFactory.createDefaultModel() ;
        	model.read("C:\\Users\\pli\\Desktop\\obs_data_individuals_rdf\\incident_"+ (15000+index%72000 ) +".rdf") ;
        	
        	NodeIterator objs = model.listObjects();
        	//System.out.println(objs.hasNext() );
        	
        	
        	
        	StmtIterator  stas = model.listStatements();
        	while(stas.hasNext()){
        		
        		Statement sta = stas.next();
        		
        		if( sta.getObject().isLiteral() ){
        			if( sta.getPredicate().getURI().toString().equals( "http://localhost/SensorSchema/ontology#hasID" ) ){
        				//System.out.println("-------------------------------------------------------------");
            			//System.out.print("No. "+index+"\tCarID : " + sta.getString() + "\t");
        				//sta.changeLiteralObject( index/2400+1 );
        				//String subject = sta.getSubject().getURI();

        				model.remove(sta);
        				//System.out.println(sta.getPredicate().getLocalName());
        				//sta.changeObject("Sim");
        				//System.out.println(sta.toString());
        				//model.createResource(sta.getSubject().getURI()).addProperty( ResourceFactory.createProperty( "http://localhost/SensorSchema/ontology", "hasID") , (index/2400+1)+"", "obs:hasID" );
        				model.createResource(sta.getSubject().getURI()).addLiteral( ResourceFactory.createProperty( "http://localhost/SensorSchema/ontology#", "hasID") , (index/eachNode+1) );
        				
        				
        				//sta.changeLiteralObject( 100 );
        				//System.out.println(sta.toString());
        				//System.out.print("No. "+index+"\tCarID : " + sta.getInt() + "\t");
        				
            			// Wriet Turtle to the blocks variant
            		    
            		    FileWriter out = null;
            		    
            		    try {
            		        out = new FileWriter( "C:\\Users\\pli\\Documents\\MSCThesis\\ArtificalData\\"+ ( index/eachNode +1) +"\\incident_"+ (index%eachNode) +".rdf" );
            		        System.out.println("Write to C:\\Users\\pli\\Documents\\MSCThesis\\ArtificalData\\"+ ( index/eachNode+1 ) +"\\incident_"+ (index%eachNode) +".rdf");
            		    } catch (IOException e) {
            		        // TODO Auto-generated catch block
            		    	System.out.println("Fail to write! ");
            		        e.printStackTrace();
            		    }
            		    model.write(out);
            		    break;
        			}
        		}
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
