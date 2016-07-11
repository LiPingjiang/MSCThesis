package com.pli.RDFManager;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class TurtleToEnSchema {
	
	public static String finalMessage = null;  // the EN schema message for communication
	public static Entities entities;
	public static String pathPrefix = "//home//pli//Desktop//";
	List<String> itemList = new ArrayList<String>();
	String ontInfo = "";

	
	public static void main( String[] args )
    {
		TurtleToEnSchema parser = new TurtleToEnSchema();
		entities = new Entities();

		
		parser.readTurtle(parser.getTextTurtle(1));
		
		System.out.println("finish");


		entities.deduplication();
//		parser.printToFile(pathPrefix + "ontology.enSchema", finalMessage);
		parser.printToFile(pathPrefix + "ontology.enSchema", entities.toENSchema() );
//		parser.printToFile(pathPrefix + "ontology2.enSchema", entities.toRDFXML() );
		System.out.println("enschema");
		parser.printToFile(pathPrefix + "ontology.turtle", entities.toTurtle( parser.ontInfo) );
		
	}
	private void readTurtle(String path) {
		Boolean isEntities = false;
		ontInfo = "";
		try ( BufferedReader br = new BufferedReader( new FileReader( path ))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	// process the line.
		    	if(line.replace(" ", "").replace("\t", "").length()==0){
		    		continue;
		    	}
		    	if(!isEntities){
		    		if(line.subSequence(0, 1).equals(":") || line.subSequence(0, 2).equals("[]")){
		    			isEntities = true;
		    		}else{
		    			ontInfo = ontInfo + line + "\n";
		    		}
		    		
		    	}
		    	if(isEntities){
		    		
		    		String[] strings = line.split("\\s+");
		    		Boolean isString=false;
		    		String stringSubject = "";
		    		
		    		for (String string : strings) {
		    			if(string.length()==0){
		    				continue;
		    			}
		    			if(!isString){
		    				if(string.length()>1 && string.contains(";")){
								itemList.add(string.substring(0, string.length()-2));
								itemList.add(";");
							}else if(string.contains("\"")){

								if(StringUtils.countMatches(string,"\"") == 2){

									itemList.add(string);
								}else{
									isString =true;
									stringSubject = string;
								}
								

							}else if(string.substring(string.length()-1).equals(".") && string.length()>1){
								itemList.add(string.substring(0, string.length()-2));
								itemList.add(".");
							}else{
								itemList.add(string);
							}
		    			}else{
		    				stringSubject = stringSubject + " " + string;
		    				if(string.contains("\"")){
		    					itemList.add(stringSubject);
		    					isString = false;
		    					stringSubject = "";
		    				}
		    			}
						
					}
		    	}
		    }
		} catch (IOException e) {
			
			e.printStackTrace();
		} 
		
		
		//convert
		
		for(int i =0; i<itemList.size();){
			String item = itemList.get(i);
			
			i = processNewEntity( i, item);
		}
		
	}
	private int processNewEntity(int index, String item) {
		//int index = i;
		Boolean objectIsAnonymou = false;
		String objectName = item;
		String objectType = "en:Entity";
		
		//if(itemList.get(index).equals(".")){
		//	return index+1;
		//}
		
		//get predicate
		index++;//skip object
		
		do {
			String predicate = itemList.get(index);
			
			String subjectName = itemList.get(++index);
			
			//System.out.println("index: "+index+ " Object: " + objectName + " Predicate: "+ predicate + " Subject: "+ subjectName);
			
			//check Object
			if(objectName.equals("[]")){
				objectName = entities.AddSystemAnon("en:Entity");
				objectIsAnonymou = true;
			}
			
			//process subject
			if(subjectName.equals("[")){
				subjectName = entities.AddAnon("en:Entity");
				entities.addToEntity(objectType, objectName, predicate, subjectName);
				
				index = processAnonymousSubject(index, "en:Entity",objectName, subjectName);
			}else if(subjectName.equals("(")){
				subjectName = entities.AddCollection();
				index = processCollectionSubject(index, subjectName);
				entities.addToEntity(  objectType, objectName, predicate, subjectName);
			}else{
				entities.addToEntity(  objectType, objectName, predicate, subjectName);
				index ++;
			}
			
			if(itemList.get(index).equals(";")){
				index ++;
				
			}else if(itemList.get(index).equals(".")){
				
			}else{
				//System.out.println(itemList.get(index));
				//System.out.println("End Error");
			}
			//index++;//move to next
		}while(!itemList.get(index).equals("."));
		
		index++;
		
		//if(objectIsAnonymou){
			//index = processAnonymousObject(index,"en:Entity", objectName);
		//}
		
		
		
		return index;
		
	}
	private int processAnonymousObject(int index, String objectType, String objectName) {

		System.out.println("processAnonymousObject " + itemList.get(index));
		
		
		
		do{
			String predicate = itemList.get(index++);
			String subjectName = itemList.get(index);
			
			if(subjectName.equals("[")){
				subjectName = entities.AddSystemAnon("en:Entity");
				entities.addToEntity(objectType, objectName, predicate, subjectName);
				index = processAnonymousSubject(index, "en:Entity",objectName, subjectName);
			} else if(subjectName.equals("(")){
				subjectName = entities.AddCollection();
				index = processCollectionSubject(index, subjectName);
				entities.addToEntity(  objectType, objectName, predicate, subjectName);
			}else {
				entities.addToEntity(  objectType, objectName, predicate, subjectName);
			}
			
			if(itemList.get(index).equals(";")){
				index ++; //skip ;
			}
		}
		while( !itemList.get(index).equals(".") );
		
		
		
		return index;
	}
	private int processAnonymousSubject(int index, String objectType, String objectName,  String subjectName) {
		//System.out.println("index: "+index + " " + itemList.get(index));
		System.out.println("processAnonymousSubject " + subjectName + " object:" + objectName);
		
		index++;//skip "["
		while( !itemList.get(index).equals("]") ){
			String predicate = itemList.get(index);
			String subject = itemList.get(++index);
			
//			System.out.println("index: "+index+ " Object: " + objectName + " Predicate: "+ predicate + " Subject: "+ subjectName);
			//System.out.println("index: "+index+ " Object: " + objectName + " Predicate: "+ predicate + " Subject: "+ subject);
			
			
			if(subject.equals("[")){
				subject = entities.AddAnon("en:Entity");
				entities.addToEntity("en:Entity", subjectName, predicate, subject);
				index = processAnonymousSubject(index, "en:Entity",subjectName,subject);
			}else if(subject.equals("(")){
				subject = entities.AddCollection();
				index = processCollectionSubject(index, subject);
				entities.addToEntity(  "en:Entity", subjectName, predicate, subject);
			}else {
				entities.addToEntity("en:Entity", subjectName, predicate, subject);
				index ++;
			}
			
			if(itemList.get(index).equals(";") || itemList.get(index).equals(".") ){
				index++;
			}
		}
		index ++;
		System.out.println("finishi anonmyous, Index: " + index );
		
		return index;
	}
	private int processCollectionSubject(int index,  String objectName ) {
		System.out.println("processCollectionSubject " + itemList.get(index));

		index++;//skip "("
		
		while( !itemList.get(index).equals(")") ){
			String subjectName = itemList.get(index);

			if(subjectName.equals("[")){
				subjectName = entities.AddAnon("en:Entity");
				entities.addToEntity("en:Collection", objectName, "en:hasItem", subjectName);
				index = processAnonymousSubject(index, "en:Entity",objectName, subjectName);
				//System.out.println("index: "+index + " " + itemList.get(index));
			}else if(subjectName.equals("(")){
				//index = processCollectionSubject(index, "en:Entity",subjectName,predicate);
			}else {
				entities.addToEntity("en:Collection", objectName, "en:hasItem", subjectName);
				index ++;
			}

		}
		index ++ ;
		System.out.println("finishi collection, Index: "+ index );
		
		return index;
	}
	private String getTextTurtle(int i) {		
		return getClass().getClassLoader().getResource("test.turtle").getPath();
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
	

}
