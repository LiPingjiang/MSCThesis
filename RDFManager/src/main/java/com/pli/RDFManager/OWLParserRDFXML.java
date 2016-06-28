package com.pli.RDFManager;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class OWLParserRDFXML {
	
	public static Logger logger = LoggerFactory.getLogger(OWLParserRDFXML.class);
	public static String finalMessage = null;  // the EN schema message for communication
	public static Entities entities;
	public static String pathPrefix = "//home//pli//Desktop//";

	
	public static void main( String[] args )
    {
		OWLParserRDFXML parser = new OWLParserRDFXML();
		Document doc = parser.loadRDFXML(pathPrefix + "prime.owl");
		finalMessage ="";
		entities = new Entities();
		
		NodeList nodeList = doc.getChildNodes();
		parser.printNode(doc.getDocumentElement(), null);
		
//		parser.printToFile(pathPrefix + "ontology.enSchema", finalMessage);
		parser.printToFile(pathPrefix + "ontology.enSchema", entities.toENSchema() );
//		parser.printToFile(pathPrefix + "ontology2.enSchema", entities.toRDFXML() );
		
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
	
	public void printNode(Node node, Node superNode){
		
		String URI = getURI(node);
		String type = getType(node);
		Boolean isAnonymous=false;
//	    logger.debug( node.getNodeName() );
	    //String content = node.getTextContent().replace(" ", "").replace("\n", "");
		String content = node.getTextContent();
	    

	    if( URI == null ){
    		isAnonymous=true;
    		URI = entities.AddAnon(type);
			((Element)node).setAttribute("rdf:about",URI);
    	}else{
    		entities.addEntity(type, URI);
    	}
	    if(content != "" && content.length() != 0 && type != "rdf:RDF" && !content.contains(" ")){
//	    	logger.debug("target size "+ content.length() + " content: "+ content);
	    	entities.addToEntity(type, URI, "owl:targetValue", content, false );
	    }
	    
	    if(superNode != null && getType(superNode)!= "rdf:RDF" ){
	    	
//	    	logger.debug( type + " " + URI );
	    	entities.addToEntity(getType(superNode), getURI(superNode), type, URI, false );
    	}
	    
	    //finalMessage+= node.getNodeName()+ "  " + content + "\n";
	    
	    printAttr( node );

	    NodeList nodeList = node.getChildNodes();
	    for (int i = 0; i < nodeList.getLength(); i++) {
	        Node currentNode = nodeList.item(i);
	        if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
	            //calls this method for all the children which is Element
	        	
	        	printNode(currentNode, node);
	        }
	    }
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
	public String printAttr(Node node){

		String URI="";
		String type = getType(node);
		Boolean isAnonymous=true;
		NamedNodeMap attributesList = node.getAttributes();
		for (int j = 0; j < attributesList.getLength(); j++) {
//		        logger.debug("Attribute: "
//		                + attributesList.item(j).getNodeName() + " = "
//		                + attributesList.item(j).getNodeValue());
//		        finalMessage+= "Attribute: "
//		                + attributesList.item(j).getNodeName() + " = "
//		                + attributesList.item(j).getNodeValue()+"\n";
		        switch(attributesList.item(j).getNodeName() ){
		        	case "rdf:about":{
		        		isAnonymous=false;
		        		URI=attributesList.item(j).getNodeValue();
		        		break;
		        	}
		       }
		}
		if(isAnonymous){
			//Add to Anonymous entities
			URI = entities.AddAnon(type);
			((Element)node).setAttribute("rdf:about",URI);
		 }
		 
		 for (int j = 0; j < attributesList.getLength(); j++) {
			//logger.debug("getNodeName"+attributesList.item(j).getNodeName());
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
					entities.addToEntity(type, URI, attributesList.item(j).getNodeName(), attributesList.item(j).getNodeValue(), isAnonymous);
					break;
				}
			}
		}

		return URI;
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
