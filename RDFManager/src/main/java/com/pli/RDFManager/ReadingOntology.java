package com.pli.RDFManager;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;


/**
 * 
 * @author pli
 *
 * This is for measuring the Jena Model reading time
 */
public class ReadingOntology {
	

	public static void main( String[] args )
    {
		
		//readOntology("ontology.rdfxml","RDF/XML");
		
		//readOntology("ontology.jsonld","JSON-LD");
		readOntology("ontology.turtle","Turtle");
		//readOntology("ontology.nt","NT");
		
		//readENSchema("ontology.ENSchema");//not use
		//readENSchema2("ontology.ENSchema");

    }
	
	private static void readENSchema2(String ontology) {
		System.out.println("....................NEW....................");
		Date dateStart = new Date();
		
		OWLPaserEnSchemaToTurtle parser = new OWLPaserEnSchemaToTurtle();
		//parser.readFromEnSchema( ReadingOntology.class.getResource("/"+ontology ).toString());
		parser.readFromEnSchema("//home//pli//Desktop//"+"ontology.enSchema");
		parser.printToFile("//home//pli//MSCThesis//RDFManager//src//main//resources//ontologyEN.owl", parser.toTURTLE() );
		readOntology("ontologyEN.owl","Turtle");
		
		
		Date dateEnd = new Date();
		
		System.out.println(ontology + " Time(ms): "+ (dateEnd.getTime()-dateStart.getTime()) );
		
	}

	public static void readOntology(String ontology, String type){
		
		System.out.println("....................NEW....................");
		Date dateStart = new Date();
		
		
		OntModel m = ModelFactory.createOntologyModel();
		InputStream in = FileManager.get().open(ontology);
		
		if (in == null)
			throw new IllegalArgumentException("File: "+ontology+" not found");
		// Read the RDF/XML file
		
		m.read(in,null, type);
		
		Date dateEnd = new Date();
		
		System.out.println(ontology + " Time(ms): "+ (dateEnd.getTime()-dateStart.getTime()) );
	}
	public static void readENSchema(String ontology){
		
		System.out.println("....................NEW....................");
		Date dateStart = new Date();
		
		OWLPaserEnSchemaToTurtle parser = new OWLPaserEnSchemaToTurtle();
		//parser.readFromEnSchema( ReadingOntology.class.getResource("/"+ontology ).toString());
		parser.readFromEnSchema("//home//pli//MSCThesis//RDFManager//src//main//resources//"+ontology);
		
		
		
		
		
		OntModel m = ModelFactory.createOntologyModel();
		//InputStream in = FileManager.get().open(ontology);
		InputStream in = new ByteArrayInputStream(parser.toTURTLE().getBytes(StandardCharsets.UTF_8));
		
		if (in == null)
			throw new IllegalArgumentException("File: "+ontology+" not found");
		// Read the RDF/XML file
		
		m.read(in, null, "Turtle");
		
		Date dateEnd = new Date();
		
		System.out.println(ontology + " Time(ms): "+ (dateEnd.getTime()-dateStart.getTime()) );
	}
}
