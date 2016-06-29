package com.pli.RDFManager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Struct;

public class EnSchemaParser {
	
	public static String pathPrefix = "//home//pli//Desktop//";
	public static Entities entities = null;
	
	
	public static void main( String[] args )
    {
		entities = new Entities();
		String path = pathPrefix + "ontology.enSchema";
		EnSchemaParser parser = new EnSchemaParser();
		parser.readFromEnSchema(path);
		parser.printToFile(pathPrefix + "ontologyEN.owl", entities.toRDFXML() );
//		parser.printToFile(pathPrefix + "ontologyEN.owl", parser.entities.toENSchema() );
    }
	
	public void readFromEnSchema(String path){
		
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		       // process the line.
//		    	System.out.println(line);
		    	addToEntityConstructor(line);
		    }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	public void printToFile(String path, String data){
		PrintWriter out = null;
		try {
			out = new PrintWriter( path );
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//out.println(finalMessage);
		out.println( data );
		out.close();
	}
	
	public class EntityConstructor{
		public String type="";
	    public String name="";
	    public String lastRelationType="";
	    
	    
	}

	EntityConstructor eConstructor = null;
	private void addToEntityConstructor(String line) {
		
//		System.out.println("text:" + line);
		if(line.equals(""))
			return;
		if(line.charAt(0)=='<'){
			
			String[] strings = line.substring(1,line.length()).split(" ");
			
			eConstructor = new EntityConstructor();
			eConstructor.type=strings[0].replace("\t", "").replace(" ", "");
			eConstructor.name=strings[1].replace("\t", "").replace(" ", "");
//			System.out.println("-----------     :" + eConstructor.type + "  " + eConstructor.name + " size:"+entities.getSize());
					
			entities.addEntity(eConstructor.type, eConstructor.name);
		}else if(line.charAt(0)=='>'){
			
			eConstructor = null;

		}else{
	
			
			if(line.length()>15 && line.substring(0,13).replace("\t", "").equals("rdfs:comment") ){

				eConstructor.lastRelationType = "rdfs:comment";
				entities.addToEntity(eConstructor.type, eConstructor.name, eConstructor.lastRelationType, line.substring(13,line.length()));
//				System.out.println("strings: " + line.substring(13,line.length()));
				
			}else{
//				System.out.println("text:" + line);
				String[] strings = line.substring(0,line.length()).split(" ");
//				System.out.println("********************************    string: " + line.replace("\t", "") +" strings.length: " + strings.length);
//				System.out.println("type: " + strings[0].replace("\t", ""));
				
				if(strings.length==1){
					entities.addToEntity(eConstructor.type, eConstructor.name, eConstructor.lastRelationType, strings[0].replace("\t", "").replace(" ", ""));
				}else if(strings.length==2){
					eConstructor.lastRelationType = strings[0].replace("\t", "").replace(" ", "");
					entities.addToEntity(eConstructor.type, eConstructor.name, eConstructor.lastRelationType, strings[1].replace("\t", "").replace(" ", ""));
//					System.out.println("length 2 "+strings[0].replace("\t", ""));
				}
			}
				
			
			
		}
		
	}
}
