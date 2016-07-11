package com.pli.RDFManager;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.xerces.dom.DocumentImpl;
import org.omg.CORBA.INTERNAL;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

import com.pli.RDFManager.Entities.Entity;

public class AnalyzerRDFXML {

	public static String pathPrefix = "//home//pli//Desktop//";
	public static Entities entities = null;
	public HashMap< String, Integer > staticCounter = new HashMap< String, Integer>();
	
	public static void main( String[] args )
    {
		AnalyzerRDFXML analyzer = new AnalyzerRDFXML();
		Document doc = analyzer.loadRDFXML(pathPrefix + "prime.owl");
		//Document doc = analyzer.loadRDFXML(pathPrefix + "ontologyEN.owl");
		
		entities = new Entities();
		
		analyzer.analyzeNode(doc.getDocumentElement());
		
		for(Entry<String, Integer > counter : analyzer.staticCounter.entrySet()) {
			System.out.println( counter.getKey()+ " : " + counter.getValue() );
		}
		

	}
	
	
	public void analyzeNode(Node node){
		
		String ontologyTag = node.getNodeName();
		// OWL 2 NEW
		if(ontologyTag.equals("owl:AllDisjointClasses")
				|| ontologyTag.equals("owl:NegativePropertyAssertion")
				|| ontologyTag.equals("owl:propertyChainAxiom")
				|| ontologyTag.equals("owl:hasKey")
				|| ontologyTag.equals("owl:propertyDisjointWith")
				|| ontologyTag.equals("owl:ReflexiveProperty")
				|| ontologyTag.equals("owl:IrreflexiveProperty")
				|| ontologyTag.equals("owl:AsymmetricProperty")
				|| ontologyTag.equals("owl:Restriction")
				|| ontologyTag.equals("owl:minQualifiedCardinality")
				|| ontologyTag.equals("owl:maxQualifiedCardinality")
				|| ontologyTag.equals("owl:qualifiedCardinality")
				|| ontologyTag.equals("owl:withRestrictions")
				|| ontologyTag.equals("xsd:minInclusive")
				|| ontologyTag.equals("xsd:maxInclusive")
				|| ontologyTag.equals("owl:intersectionOf")
				|| ontologyTag.equals("owl:datatypeComplementOf")
				||true
				){
			if(staticCounter.containsKey( ontologyTag )){
				staticCounter.put(ontologyTag,staticCounter.get(node.getNodeName()).intValue()+1);
			}else{
				staticCounter.put(ontologyTag, 1);
			}
			
			
		    NodeList nodeList = node.getChildNodes();
		    for (int i = 0; i < nodeList.getLength(); i++) {
		        Node currentNode = nodeList.item(i);
		        if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
		        	analyzeNode(currentNode);
		        }
		    }
		}
	}

	public Document loadRDFXML(String path){
		
		/*DOMImplementationLS domImplLS = (DOMImplementationLS) doc.getImplementation();
		LSSerializer serializer = domImplLS.createLSSerializer();
		String str = serializer.writeToString( doc.getDocumentElement() );*/
		
		
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
	
	public static String prettyFormat(String input, int indent) {
	    try {
	        Source xmlInput = new StreamSource(new StringReader(input));
	        StringWriter stringWriter = new StringWriter();
	        StreamResult xmlOutput = new StreamResult(stringWriter);
	        TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        transformerFactory.setAttribute("indent-number", indent);
	        Transformer transformer = transformerFactory.newTransformer(); 
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.transform(xmlInput, xmlOutput);
	        return xmlOutput.getWriter().toString();
	    } catch (Exception e) {
	        throw new RuntimeException(e); // simple exception handling, please review it
	    }
	}
}
