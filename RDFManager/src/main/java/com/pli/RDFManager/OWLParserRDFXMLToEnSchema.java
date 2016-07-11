package com.pli.RDFManager;


import java.awt.Checkbox;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.auth.ContextAwareAuthScheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.base.CaseFormat;

public class OWLParserRDFXMLToEnSchema {
	
	public static Logger logger = LoggerFactory.getLogger(OWLParserRDFXMLToEnSchema.class);
	public static String finalMessage = null;  // the EN schema message for communication
	public static Entities entities;
	public static String pathPrefix = "//home//pli//Desktop//";
	

	
	public static void main( String[] args )
    {
		OWLParserRDFXMLToEnSchema parser = new OWLParserRDFXMLToEnSchema();
		//Document doc = parser.loadRDFXML(pathPrefix + "prime.owl");
		Document doc = parser.loadRDFXML(parser.testOntology(1));
		finalMessage ="";
		entities = new Entities();
		
//		NodeList nodeList = doc.getChildNodes();
		parser.printNode(doc.getDocumentElement(), null,false);
		entities.deduplication();
//		parser.printToFile(pathPrefix + "ontology.enSchema", finalMessage);
		parser.printToFile(pathPrefix + "ontology.enSchema", entities.toENSchema() );
//		parser.printToFile(pathPrefix + "ontology2.enSchema", entities.toRDFXML() );
		
	}
	private String testOntology(int i) {
		switch (i) {
		case 1:
			return getClass().getClassLoader().getResource("ont_test_Advanced_Datatypes.owl").getPath();
			//return "ont_test_Advanced_Datatypes.owl";

		default:
			break;
		}
		return null;
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
	
	 public void printNode(Node node, Node superNode, Boolean isCollectionItem){
		

		String collectionEntity = "";
		String URI = getURI(node);
		String relationType = getType(node);
		String entityType = getEntityType(relationType);
		Boolean isAnonymous=false;
	    
	    //String content = node.getTextContent().replace(" ", "").replace("\n", "");
		String content = node.getTextContent();
//		logger.debug( "Node Name: " + node.getNodeName() + " content: " + content );
		logger.debug( "Node Name: " + node.getNodeName());
		
		Boolean hasCollection = isCollection(node);
		if( hasCollection ){
			logger.debug( "Is collection Node Name: " + node.getNodeName()  );
			isAnonymous=true;
			//URI = entities.AddAnon(type);
			//collectionEntity = entities.AddCollection();
			//URI = collectionEntity;
			URI = entities.AddCollection();
//			logger.debug( "URI: " + URI  );
//			logger.debug( "collectionEntity: " + collectionEntity  );
			
//			entities.addToEntity(getType(superNode), getURI(superNode), collectionEntity, URI, false );
			//entities.addToEntity(type, URI, "en:Collection", collectionEntity, false );
			
		}else if( URI == null ){
//			logger.debug("Node Name: " + node.getNodeName() + "  URI == NULL");
    		isAnonymous=true;
    		URI = entities.AddAnon(entityType);
//    		logger.debug("Anonymous URL: "+ URI);
			((Element)node).setAttribute("rdf:about",URI);
    	}else{
//    		logger.debug("Node Name: " + node.getNodeName() + "  URI != NULL");
    		entities.addEntity(entityType, URI);
    	}
		
	    if(content != "" && content.length() != 0 && relationType != "rdf:RDF" 
	    		&& !content.contains(" ") && !content.contains("\t")
	    		&& !hasCollection){
//	    	logger.debug("target size "+ content.length() + " content: "+ content);
	    	
	    	entities.addToEntity(entityType, URI, "owl:targetValue", content, false );
	    }
	    
	    
	    
	    if(superNode != null && getType(superNode)!= "rdf:RDF" ){

	    	if(isCollectionItem){
//	    		logger.debug( "type " + getType(superNode) + " URI " + getURI(superNode) );
//	    		logger.debug( "type " + getType(node) + " URI " + getURI(node) );
//		    	logger.debug( type + " " + URI );
//	    		entities.addToEntity(getType(superNode), getURI(superNode), "en:hasItems", URI, false );
	    	}else{
	    		entities.addToEntity(getType(superNode), getURI(superNode), relationType, URI, false );
	    	}
    	}
	    
	    //finalMessage+= node.getNodeName()+ "  " + content + "\n";
	    
	    addAttr( node, entityType, URI );
	    //logger.debug( "Node Name: " + node.getNodeName());
	    NodeList nodeList = node.getChildNodes();
	    for (int i = 0; i < nodeList.getLength(); i++) {
	        Node currentNode = nodeList.item(i);
	        if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
	            //calls this method for all the children which is Element
	        	if(hasCollection){
	        		printItemNodeToCollection(currentNode, URI);
	        	}else if(currentNode.getNodeName().equals("rdfs:comment")){
	    	    	entities.addToEntity(relationType, URI, "rdfs:comment", currentNode.getTextContent(), isAnonymous );
	    	    }else if(currentNode.getNodeName().equals("rdfs:label")){
	    	    	entities.addToEntity(relationType, URI, "rdfs:label", currentNode.getTextContent(), isAnonymous );
	    	    }
	        	else {
	        		printNode(currentNode, node, hasCollection);
	        	}
	        	
	        }
	    }
	}
	
	private String getEntityType(String relationType) {
		switch(relationType){
		case "rdfs:subClassOf":
			return "owl:Class";
		case "owl:inverseOf":
			return "owl:ObjectProperty";
		case "owl:datatypeComplementOf ":
			return "rdf:Property";
		case "rdfs:subPropertyOf":
			return "rdf:Property";
//		case "owl:inverseOf ":
//			return "owl:ObjectProperty";
//		case "owl:inverseOf ":
//			return "owl:ObjectProperty";
//		case "owl:inverseOf ":
//			return "owl:ObjectProperty";
//		case "owl:inverseOf ":
//			return "owl:ObjectProperty";
			
		case "owl:disjointWith":
			return "owl:Class";
		case "owl:onClass":
			return "owl:Class";
		case "owl:equivalentClass":
			return "owl:Class";
		case "owl:someValuesFrom":
			return "rdf:resource";
		case "owl:allValuesFrom":
			return "rdf:resource";
		case "owl:onProperty":
			return "rdf:Property";
		case "rdfs:Datatype":
			return "rdfs:Datatype";
		case "owl:ObjectProperty":
			return "owl:ObjectProperty";
		case "owl:DatatypeProperty":
			return "owl:DatatypeProperty";
		case "owl:Class":
			return "owl:Class";
		default:
			return relationType;
		}
	}
	private void printItemNodeToCollection(Node itemNode, String collectionEntity) {
		
		Boolean hasCollection = isCollection(itemNode);
		boolean isAnonymous;
		String URI = getURI(itemNode);
		String type = getType(itemNode);
		
		if( hasCollection ){
			isAnonymous=true;
			URI = entities.AddAnon(type);
			collectionEntity = entities.AddCollection();
			
		}else if( URI == null ){
    		isAnonymous=true;
    		URI = entities.AddAnon(type);
			((Element)itemNode).setAttribute("rdf:about",URI);
    	}else{
    		entities.addEntity(type, URI);
    	}
		
		addAttr(itemNode, type, URI);
		
		entities.addToEntity( entities.getType(collectionEntity) , collectionEntity , "en:hasItems", URI, false );
		
		NodeList nodeList = itemNode.getChildNodes();
	    for (int i = 0; i < nodeList.getLength(); i++) {
	        Node currentNode = nodeList.item(i);
	        if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
	            //calls this method for all the children which is Element
	        		printNode(currentNode, itemNode, hasCollection);
	        }
	    }
		
	}
	private boolean isCollection(Node node) {
		Boolean hasCollection =false;
		NamedNodeMap attributesList = node.getAttributes();
		for (int j = 0; j < attributesList.getLength(); j++) {
//		        logger.debug("Attribute: "
//		                + attributesList.item(j).getNodeName() + " = "
//		                + attributesList.item(j).getNodeValue());
//		        finalMessage+= "Attribute: "
//		                + attributesList.item(j).getNodeName() + " = "
//		                + attributesList.item(j).getNodeValue()+"\n";
		        switch(attributesList.item(j).getNodeName() ){
//		        	case "rdf:about":{
//		        		isAnonymous=false;
//		        		URI=attributesList.item(j).getNodeValue();
////		        		break;
//		        	}
		        	case "rdf:parseType":{
		        		if(attributesList.item(j).getNodeValue().equals("Collection")){
		        			hasCollection=true;
		        		}
		        	}
		       }
		}
		return hasCollection;
	}
	private String getType(Node node) {
		return node.getNodeName();
	}
	private String getURI(Node node) {
		NamedNodeMap attributesList = node.getAttributes();
		for (int j = 0; j < attributesList.getLength(); j++) {
	        if(attributesList.item(j).getNodeName() == "rdf:about"){
	        		return attributesList.item(j).getNodeValue();
	        }
	    }
		return null;
	}
	public String addAttr(Node node, String type, String URI){

//		String URI=EntityURI;
//		String type = getType(node);
		Boolean isAnonymous=false;
		Boolean hasCollection=false;
		
//		NamedNodeMap attributesList = node.getAttributes();
//		for (int j = 0; j < attributesList.getLength(); j++) {
////		        logger.debug("Attribute: "
////		                + attributesList.item(j).getNodeName() + " = "
////		                + attributesList.item(j).getNodeValue());
////		        finalMessage+= "Attribute: "
////		                + attributesList.item(j).getNodeName() + " = "
////		                + attributesList.item(j).getNodeValue()+"\n";
//		        switch(attributesList.item(j).getNodeName() ){
//		        	case "rdf:about":{
//		        		isAnonymous=false;
//		        		URI=attributesList.item(j).getNodeValue();
//		        		break;
//		        	}
//		       }
//		}
//		if(isAnonymous){
//			//Add to Anonymous entities
//			URI = entities.AddAnon(type);
//			((Element)node).setAttribute("rdf:about",URI);
//		 }
		NamedNodeMap attributesList = node.getAttributes();
		for (int j = 0; j < attributesList.getLength(); j++) {
//			logger.debug("getNodeName "+attributesList.item(j).getNodeName());
//			logger.debug("Attribute: " + attributesList.item(j).getNodeName() + " value: " + attributesList.item(j).getNodeValue() );
//			logger.debug("type: " + type + " URI: " + URI  );
			switch(attributesList.item(j).getNodeName() ){
			 	case "rdf:RDF":{
			 		
					entities.addAnotations(type, URI, attributesList.item(j).getNodeName(), attributesList.item(j).getNodeValue());
					break;
				}
			 	case "rdf:about":{
//					entities.addAnotations(type, URI, attributesList.item(j).getNodeName(), attributesList.item(j).getNodeValue());
					break;
				}
				default:{
//					logger.debug("run default is Anonymous "+ isAnonymous);
					entities.addToEntity(type, URI, attributesList.item(j).getNodeName(), attributesList.item(j).getNodeValue(), isAnonymous);
					break;
				}
			}
		}
		

		
		return null;
	}

	public Document loadRDFXML(String path){
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    factory.setValidating(true);
	    factory.setIgnoringElementContentWhitespace(true);
	    Document doc = null;
	    try {
	        DocumentBuilder builder = factory.newDocumentBuilder();
	        File file = new File(path);
	        doc = builder.parse(file);
	        // Do something with the document here.
	    } catch (ParserConfigurationException e) {
	    } catch (SAXException e) {
	    } catch (IOException e) { 
	    }
	    
	    return doc;
	}

}
