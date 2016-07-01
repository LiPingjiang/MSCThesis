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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;


public class Entities {

	HashMap< String, Entity> entities = new HashMap< String, Entity>();// key = Entity Name, value = Entity Object
	HashMap< String, Entity> anonEntities = new HashMap< String, Entity>();
	HashMap< String, Entity> collectionEntities = new HashMap< String, Entity>();
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
		
		if(isValidEntityType(EntityType)){
			entities.get(EntityName).setType(EntityType);
		}
	}
	public void addToEntity( String EntityType, String EntityName, String Type, String Value, boolean isAnonymous){
//		if(isAnonymous)
//			AddToAnon(EntityType,EntityName,Type,Value);
//		else 
		{
			addToEntity(EntityType,EntityName,Type,Value);
		}
	}
	
	public void addEntity( String EntityType, String EntityName){

		if (!entities.containsKey(EntityName)){
			//System.out.println(EntityType + "            ***************************************************************");
//			if(EntityType == null ){
//				EntityType = "en:Empty";
//			} else 
			if(!isValidEntityType(EntityName)){
					System.out.println("--------------------------------------------------------------------------------------------------   :" + EntityType);
					//System.out.println(EntityType.replace("\t", "").replace(" ","").equals("owl:Class"));
					EntityType = "en:Empty";
			}
			
			entities.put(EntityName, new Entity(EntityType, EntityName));
		}
		
	}
	
	public boolean isValidEntityType(String EntityType){
		if(!EntityType.equals("rdfs:Datatype") 
				&& !EntityType.equals("owl:ObjectProperty") 
				&& !EntityType.equals("owl:DatatypeProperty") 
				&& !EntityType.equals("owl:Class")
				&& !EntityType.equals("rdfs:seeAlso")
				&& !EntityType.equals("rdfs:isDefinedBy")
				&& !EntityType.equals("en:Collection")
				&& !EntityType.equals("rdf:Property")
				&& !EntityType.equals("owl:Restriction")
				&& !EntityType.equals("owl:minCardinality")
				&& !EntityType.equals("owl:maxCardinality")
				&& !EntityType.equals("rdf:resource")
				&& !EntityType.equals("owl:qualifiedCardinality") 
				&& !EntityType.equals("owl:AnnotationProperty")
				&& !EntityType.equals("owl:imports")
				&& !EntityType.equals("rdf:RDF")
				&& !EntityType.equals("owl:Ontology") ){
			if(EntityType.substring(0, 3).equals("owl")
					|| EntityType.substring(0, 3).equals("rdf")
					|| EntityType.substring(0, 2).equals("en")
					|| EntityType.substring(0, 4).equals("rdfs")){
				return false;
			}
		}
		return true;
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
		
		//System.out.println(EntityName);
		return EntityName;
	}
	public String AddCollection( ){
		String EntityType = "en:Collection";
		String EntityName = "Collection_"+collectionEntities.size();
		collectionEntities.put(EntityName, new Entity(EntityType, EntityName));
    
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
	
	public String getType( String EntityName) {
		return entities.get(EntityName).getType();
	}
	
	class Entity{
		private String EntityType;
		private String EntityName;
		HashMap<String, List<String>> container = new HashMap<String, List<String>>(); //Relation And Characteristics
		
		Entity( String Type, String Name){
			EntityType = Type;
			EntityName = Name;
		}
		
		public void setType(String type) {
			// TODO Auto-generated method stub
			EntityType = type;
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
				List<String> l = new ArrayList<>();
				l.add(Value);
				container.put( Type, l );
			}
			
			return true;
		}
		
		public HashMap<String, List<String>> get() {
			return  container;
		}
		
		public String getName(){
			return EntityName;
		}
		public String getType(){
			return EntityType;
		}
		public HashMap<String, List<String>> getContent(){
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
	    for(Entry<String, List<String>> info : entity.get().entrySet()) {//info means relations or characteritics
	    	
//	    	System.out.println("Entity: " + entity.getName()+ " attr Type: " + info.getKey());
	    	
	    	switch (info.getKey()) {
	    	case "rdf:about":{

	    		break;
	    	}
	    	case "rdf:resource":{
	    		Iterator<String> strings = info.getValue().iterator();
		    	while (strings.hasNext()) {
		    		
					String string= strings.next();
					
//					Element element = doc.createElement(entity.getType());
					
					superElement.setAttribute("rdf:resource", string);
					
				}
		    	break;
	    	}
	    	case "rdf:parseType":{
	    		//String string = info.getValue().get(0).toString();
	    		//Element element = doc.createElement(entity.getType());
	    		//element.setAttribute("rdf:parseType", string);
//	    		addCollectionToXMLNode(doc, element, entity, entities);
	    		//superElement.appendChild(element);
	    		
	    		break;
	    	}
	    	case "rdf:Description":{
	    		String string = info.getValue().get(0).toString();
	    		Element element = doc.createElement(entity.getType());
	    		
	    		if(string.substring(0, 5).equals("anon_")){
	    			Iterator<String> strings = info.getValue().iterator();
			    	while (strings.hasNext()) {
			    		
						String s= strings.next();
						
						Element e = doc.createElement(info.getKey());
						if(!string.substring(0, 5).equals("anon_")){
							e.setAttribute("rdf:about", s);
						}
							
						superElement.appendChild(e);
					    
						addEntityToXMLNode(doc, e, entities.get(s), entities);
					}
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
	    	case "en:Collection":{
//	    		String string = info.getValue().get(0).toString();
//	    		
//	    		superElement.setTextContent(string);
//	    		System.out.println("Collection: ");
	    		superElement.setAttribute("rdf:parseType", "Collection");
	    		Iterator<String> strings = info.getValue().iterator();
	    		while(strings.hasNext()){
	    			String EntityName = strings.next();
//	    			System.out.println( EntityName );
	    			//Element element = doc.createElement(entities.get(EntityName).getType());
	    			//superElement.appendChild(element);
//	    			System.out.println("superElement: " + getType(superElement));
	    			addEntityToXMLNode(doc, superElement, entities.get(EntityName), entities);
	    		}
	    		
	    		break;
	    	}
	    	case "en:hasItems":{
	    		Iterator<String> strings = info.getValue().iterator();
	    		while(strings.hasNext()){
	    			String EntityName = strings.next();
//	    			Element element = doc.createElement(entities.get(EntityName).getType());
//	    			superElement.appendChild(element);
//	    			System.out.println("superElement: " + getURI(superElement) +EntityName);
//	    			addEntityToXMLNode(doc, element, entities.get(EntityName), entities);
	    			Element element = doc.createElement("rdf:Description");
					if(!EntityName.substring(0, 5).equals("anon_")){ 
						element.setAttribute("rdf:about", EntityName);
					}
						
					superElement.appendChild(element);
				    
//					addEntityToXMLNode(doc, element, entities.get(EntityName), entities);
	    		}
	    		
	    		break;
	    	}
	    	case "rdfs:comment":{
//	    		System.out.println("comment");
	    		Iterator<String> strings = info.getValue().iterator();
	    		while(strings.hasNext()){
	    			String comment = strings.next();
	    			Element element = doc.createElement("rdfs:comment");
		    		element.setTextContent(comment);
		    		superElement.appendChild(element);
	    		}
	    		
	    		break;
	    	}
	    	case "rdfs:label":{
//	    		System.out.println("comment");
	    		Iterator<String> strings = info.getValue().iterator();
	    		while(strings.hasNext()){
	    			String comment = strings.next();
	    			Element element = doc.createElement("rdfs:label");
		    		element.setTextContent(comment);
		    		superElement.appendChild(element);
	    		}
	    		
	    		break;
	    	}
			default:
				Iterator<String> strings = info.getValue().iterator();
		    	while (strings.hasNext()) {
		    		
					String string= strings.next();
					
//					System.out.println("key = " + info.getKey());
//					System.out.println("key = " + info.getKey());
//					System.out.println("value = " + info.getValue());
					Element element = doc.createElement(info.getKey());
					if(!string.substring(0, 5).equals("anon_") 
							&& !string.substring(0, 11).equals("Collection_") ){ 
						element.setAttribute("rdf:about", string);
					}
						
					superElement.appendChild(element);
				    
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
			e.printStackTrace();
		}
	    Document doc = docBuilder.newDocument();
	    Element rootElement = doc.createElement("rdf:RDF");
	    doc.appendChild(rootElement);
		
	    //add xml prefix
	    Entity xml = entities.get("anon_0");
	    if(xml!=null){
	    	for(Entry<String, List<String>> entry : xml.getContent().entrySet()){
//	    		System.out.println(entry.getKey().toString() +"_"+ entry.getValue().toString());
		    	rootElement.setAttribute(entry.getKey().toString(), entry.getValue().get(0));
		    }
	    }else{
	    	System.out.println("XML is null");
	    }
	    
	    
	    //add entity
		
		for(Entry<String, Entity> entry : entities.entrySet()) {
//		    String entityName = entry.getKey();
		    Entity entity = entry.getValue();
		    if(!entity.getName().substring(0, 5).equals("anon_") ){
		    
		    	if( 	entity.getType().equals("rdfs:Datatype") ||
		    			entity.getType().equals("owl:ObjectProperty") ||
		    			entity.getType().equals("owl:DatatypeProperty") ||
		    			entity.getType().equals("owl:Class") ||
		    			entity.getType().equals("owl:NamedIndividual") ||
		    			entity.getType().equals("rdf:Description") ||
		    			entity.getType().equals("owl:Ontology")
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
		
		
		Transformer transformer = null;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
		} catch (TransformerConfigurationException e1) {
			e1.printStackTrace();
		} catch (TransformerFactoryConfigurationError e1) {
			e1.printStackTrace();
		}
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		//initialize StreamResult with File object to save to file
		StreamResult result = new StreamResult(new StringWriter());
		DOMSource source = new DOMSource(doc);
		try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		String xmlString = result.getWriter().toString();
		
//		String xmlString = new XMLDocument(doc).toString();
		
		/*DOMImplementationLS domImplLS = (DOMImplementationLS) doc.getImplementation();
		LSSerializer serializer = domImplLS.createLSSerializer();
		String xmlString = serializer.writeToString( doc.getDocumentElement() );*/
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

		    for(Entry<String, List<String>> info : entity.get().entrySet()) {//info means relations or characteritics
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
