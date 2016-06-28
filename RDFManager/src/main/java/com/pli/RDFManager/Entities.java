package com.pli.RDFManager;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.jena.sparql.pfunction.library.container;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.jcabi.xml.XMLDocument;


public class Entities {

	HashMap< String, Entity> entities = new HashMap< String, Entity>();// key = Entity Name, value = Entity Object
	HashMap< String, Entity> anonEntities = new HashMap< String, Entity>();
	HashMap< String, Entity> anotations = new HashMap< String, Entity>();
	Entities(){
		
	}
	
	//Type equals to Relation Type or Characteristics Type
	//Value equals to Relation Name or Characteristics Name
	public void addToEntity( String EntityType, String EntityName, String Type, String Value){
		//check whether exist
		//if so, just add relation and characteristic
		//if not, add as new one
		if (!entities.containsKey(EntityName)){
			entities.put(EntityName, new Entity(EntityType, EntityName));
		}
		
		entities.get(EntityName).Add(Type, Value);
		
	}
	public void addToEntity( String EntityType, String EntityName, String Type, String Value, boolean isAnonymous){
		if(isAnonymous)
			AddToAnon(EntityType,EntityName,Type,Value);
		else {
			addToEntity(EntityType,EntityName,Type,Value);
		}
	}
	
	public void addEntity( String EntityType, String EntityName){

		if (!entities.containsKey(EntityName)){
			entities.put(EntityName, new Entity(EntityType, EntityName));
		}
		
	}
	
	//Anonymous Class or instance
	public String AddToAnon( String EntityType, String Type, String Value){
		
		//TODO
		//check whether the anonymous entity exist
//		if (!anonEntities.containsKey(EntityName)){
//			anonEntities.put(EntityName, new Entity(EntityType, EntityName));
//		}
		String EntityName = "anon_"+anonEntities.size();
		anonEntities.put(EntityName, new Entity(EntityType, EntityName));

		anonEntities.get(EntityName).Add(Type, Value);
		
		return EntityName;
	}
	public boolean AddToAnon( String EntityType, String EntityName, String Type, String Value){
		
		//TODO
		//check whether the anonymous entity exist
		if (!anonEntities.containsKey(EntityName)){
			anonEntities.put(EntityName, new Entity(EntityType, EntityName));
		}
//		anonEntities.put(EntityName, new Entity(EntityType, EntityName));

		anonEntities.get(EntityName).Add(Type, Value);
		
		return true;
	}
	public String AddAnon( String EntityType){
		
		String EntityName = "anon_"+anonEntities.size();
		anonEntities.put(EntityName, new Entity(EntityType, EntityName));
    
		addEntity(EntityType, EntityName);
		
		return EntityName;
	}
	public void addAnotations( String EntityType, String EntityName, String Type, String Value){
		//check whether exist
		//if so, just add relation and characteristic
		//if not, add as new one
		if (!anotations.containsKey(EntityName)){
			anotations.put(EntityName, new Entity(EntityType, EntityName));
		}
		anotations.get(EntityName).Add(Type, Value);
		
	}
	public int getSize(){
		return entities.size();
	}
	
	class Entity{
		private String EntityType;
		private String EntityName;
		HashMap<String, List> container = new HashMap<String, List>(); //Relation And Characteristics
		
		Entity( String Type, String Name){
			EntityType = Type;
			EntityName = Name;
		}
		
		//Add relation or characteristic to the Entity
		public boolean Add( String Type, String Value){
			if(container.containsKey(Type)){
				//check whether it is the same value
//				if(container.get(Type).equals(Value))
//					return true;
//				else
//					return false;
				if(!container.get(Type).contains(Value))
					container.get(Type).add(Value);
			}else {
				List l = new ArrayList<>();
				l.add(Value);
				container.put( Type, l );
			}
			
			return true;
		}
		
		public HashMap<String, List> get() {
			return  container;
		}
		
		public String getName(){
			return EntityName;
		}
		public String getType(){
			return EntityType;
		}
		public HashMap<String, List> getContent(){
			return container;
		}
	}

	public String toENSchema() {
		String enString="";
		
		//Anotation
//		enString += "<!--Anotation-->\n";
//		enString += printEntitiesHashMap(anotations);
		//ENTITY
//		enString += "<!--Entities-->\n";
		enString += printEntitiesHashMap(entities);
		//Anonymous Entity
//		if(printAnonymous){
//			enString += "<!--Anonymous Entities-->\n";
//			enString += printEntitiesHashMap(anonEntities);
//		}
		
		
		return enString;
	}
	public String toRDFXML() {
		String rdfString="";
		

		//ENTITY
//		rdfString += "<!--Entities-->\n";
		rdfString += printEntitiesHashMapToRDFXML(entities);

		
		
		return rdfString;
	}
	private void addEntityToXMLNode(Document doc, Element superElement, Entity entity, HashMap<String, Entity> entities) {

		if(entity == null || superElement==null)
			return;
//		System.out.println("superenlient: "+ getURI(superElement) + " entity: " + entity.getName());
	    for(Entry<String, List> info : entity.get().entrySet()) {//info means relations or characteritics
	    	
	    	System.out.println(info.getKey());
	    	
	    	switch (info.getKey()) {
	    	case "rdf:about":{
//	    		String string = info.getValue().get(0).toString();
//	    		Element element = doc.createElement(entity.getType());
//	    		System.out.println("1. " + string);
//	    		if(!string.substring(0, 5).equals("anon_")){
//	    			System.out.println("2. " +string);
//		    		element.setAttribute("rdf:about", string);
//		    		superElement.appendChild(element);
//	    		}
//	    		Iterator<String> strings = info.getValue().iterator();
//		    	while (strings.hasNext()) {
//		    		
//					String string= strings.next();
//					Element element = doc.createElement(entity.getType());
//					//String[] attr = string.split("=");
//					element.setAttribute(info.getKey(), string);
//					superElement.appendChild(element);
////					addEntityToXMLNode(doc, element, entities.get(info.getKey()), entities);
//				}
	    		break;
	    	}
	    	case "rdf:resource":{
	    		Iterator<String> strings = info.getValue().iterator();
		    	while (strings.hasNext()) {
		    		
					String string= strings.next();
					
//					System.out.println("Type: " + info.getKey() + "  Value: "+ string);
					
					Element element = doc.createElement(entity.getType());
					//String[] attr = string.split("=");
					
					
//					element.setAttribute("rdf:resource", string);
//					superElement.appendChild(element);
					superElement.setAttribute("rdf:resource", string);
					
//					addEntityToXMLNode(doc, element, entities.get(string), entities);
				}
		    	break;
	    	}
	    	case "rdf:parseType":{
	    		String string = info.getValue().get(0).toString();
	    		Element element = doc.createElement(entity.getType());
	    		element.setAttribute("rdf:parseType", string);
	    		superElement.appendChild(element);
	    		
	    		break;
	    	}
	    	case "rdf:Description":{
	    		String string = info.getValue().get(0).toString();
	    		Element element = doc.createElement(entity.getType());
	    		
	    		if(string.substring(0, 5).equals("anon_")){
//	    			System.out.println(string+" "+entities.get(string).getName() +" "+ entities.get(string).getType() );
	    			Iterator<String> strings = info.getValue().iterator();
			    	while (strings.hasNext()) {
			    		
						String s= strings.next();
						
//						System.out.println("Type: " + info.getKey() + "  Value: "+ string);
						
						Element e = doc.createElement(info.getKey());
						//String[] attr = string.split("=");
						if(!string.substring(0, 5).equals("anon_")){
							e.setAttribute("rdf:about", s);
						}
							
//						element.setAttribute(info.getKey(), string);
						superElement.appendChild(e);
//						System.out.println("super element URI: "+ getURI(element) + " this entity URI: " + entity.getName() + " next entity URI:"+ string);
					    
						addEntityToXMLNode(doc, e, entities.get(s), entities);
					}
//	    			addEntityToXMLNode(doc, element, entities.get(string), entities);
	    		}else{
		    		element.setAttribute("rdf:Description", string);
		    		superElement.appendChild(element);
	    		}
	    		
	    		
	    		break;
	    	}
	    	case "owl:targetValue":{
	    		String string = info.getValue().get(0).toString();
	    		
	    		superElement.setTextContent(string);
	    		break;
	    	}

			default:
				Iterator<String> strings = info.getValue().iterator();
		    	while (strings.hasNext()) {
		    		
					String string= strings.next();
					
//					System.out.println("Type: " + info.getKey() + "  Value: "+ string);
					
					Element element = doc.createElement(info.getKey());
					//String[] attr = string.split("=");
					if(!string.substring(0, 5).equals("anon_")){
						element.setAttribute("rdf:about", string);
					}
						
//					element.setAttribute(info.getKey(), string);
					superElement.appendChild(element);
//					System.out.println("super element URI: "+ getURI(element) + " this entity URI: " + entity.getName() + " next entity URI:"+ string);
				    
					addEntityToXMLNode(doc, element, entities.get(string), entities);
				}
				break;
			}
	    	

	    }
	}
	private String getURI(Element element) {
		if(element.hasAttribute("rdf:about"))
			return element.getAttribute("rdf:about");
		return null;
	}

	private String printEntitiesHashMapToRDFXML(HashMap<String, Entity> entities) {

		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder docBuilder=null;
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    Document doc = docBuilder.newDocument();
	    Element rootElement = doc.createElement("rdf:RDF");
	    doc.appendChild(rootElement);
		
	    //add xml prefix
	    Entity xml = entities.get("anon_0");
	    if(xml!=null){
	    	for(Entry<String, List> entry : xml.getContent().entrySet()){
//	    		System.out.println(entry.getKey().toString() +"_"+ entry.getValue().toString());
		    	rootElement.setAttribute(entry.getKey().toString(), entry.getValue().toString());
		    }
	    }else{
	    	System.out.println("XML is null");
	    }
	    
	    
	    //add entity
		
		for(Entry<String, Entity> entry : entities.entrySet()) {
		    String entityName = entry.getKey();
		    Entity entity = entry.getValue();
		    if(!entity.getName().substring(0, 5).equals("anon_") ){
		    
		    	if( 	entity.getType().equals("rdfs:Datatype") ||
		    			entity.getType().equals("owl:ObjectProperty") ||
		    			entity.getType().equals("owl:DatatypeProperty") ||
		    			entity.getType().equals("owl:Class") ||
		    			entity.getType().equals("owl:NamedIndividual") ||
		    			entity.getType().equals("rdf:Description")
		    			){
//		    		System.out.println("main node: "+entity.getName() + " " +entity.getName().substring(0, 5));
		    		Element element = doc.createElement(entity.getType());
					element.setAttribute("rdf:about", entity.getName());
					rootElement.appendChild(element);
			    	addEntityToXMLNode(doc, element, entity, entities);
	//		    	System.out.println( rootElement.getChildNodes().getLength() );
		    	}else{
//		    		System.out.println( "not main node: "+ entity.getName()+" type:"+entity.getType());
//		    		System.out.println( entity.getType() == "owl:NamedIndividual");
//		    		System.out.println( entity.getType().equals("owl:NamedIndividual"));
		    	}
		    		
		    }
		    
		}
		
//		TransformerFactory tf = TransformerFactory.newInstance();
//		Transformer transformer = null;
//		try {
//			transformer = tf.newTransformer();
//		} catch (TransformerConfigurationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		
//		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
//		StringWriter writer = new StringWriter();
//		try {
//			transformer.transform(new DOMSource(doc), new StreamResult(writer));
//		} catch (TransformerException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
////		String output = writer.getBuffer().toString();
		
		
		
		Transformer transformer = null;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
		} catch (TransformerConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformerFactoryConfigurationError e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		//initialize StreamResult with File object to save to file
		StreamResult result = new StreamResult(new StringWriter());
		DOMSource source = new DOMSource(doc);
		try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String xmlString = result.getWriter().toString();
		
//		String xmlString = new XMLDocument(doc).toString();
		return xmlString;
	}

	public String printEntitiesHashMap( HashMap< String, Entity> entities ) {
		String result="";
		String indentation ="";
		
		for(Entry<String, Entity> entry : entities.entrySet()) {
		    String entityName = entry.getKey();
		    Entity entity = entry.getValue();
		    String body="";
		    body +="<" + entity.getType() + " " + entityName + "\n";
		    // do what you have to do here
		    // In your case, an other loop.

		    for(Entry<String, List> info : entity.get().entrySet()) {//info means relations or characteritics
		    	body +="\t" + info.getKey() + " ";// + info.getValue() + "\n";
		    	Iterator<String> strings = info.getValue().iterator();
		    	if(info.getValue().size()>1){
		    		indentation="\t\t";
		    		String string= strings.next();
					body += string + "\n";
		    	}
		    	while (strings.hasNext()) {
					String string= strings.next();
					body += indentation + string + "\n";
					
				}
		    	if(info.getValue().size()>1)
		    		indentation="\t";
		    }
		    body+=">" + "\n";
		    result += body;
		}
		result+= "\n";
		
		return result;
		
	}
	
}
