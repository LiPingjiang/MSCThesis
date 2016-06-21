package com.pli.RDFManager;


import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.jena.ontology.AnnotationProperty;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.ontology.Restriction;
import org.apache.jena.ontology.UnionClass;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.function.library.leviathan.log;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OWLPaser {
	
	public static Logger logger = LoggerFactory.getLogger(OWLPaser.class);
	public static String finalMessage = null;  // the EN schema message for communication
	public static Entities entities;
	public static String pathPrefix = "//home//pli//Desktop//";

	
	public static void main( String[] args )
    {
		//logger.info("Hello World");
		entities = new Entities();
		
		finalMessage = "";

//		OntModel ontoModel = ModelFactory.createOntologyModel( OntModelSpec.OWL_DL_MEM );
		OntModel ontoModel = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
		
		try 
	    {
//	        InputStream in = FileManager.get().open("C:\\Users\\pli\\Desktop\\prime.owl");
	        InputStream in = FileManager.get().open(pathPrefix + "prime.owl");
	        
	        try 
	        {
	            ontoModel.read(in, "RDF/XML-ABBREV" ,null);
	        } 
	        catch (Exception e) 
	        {
	            e.printStackTrace();
	        }

	    }
		catch (JenaException je) 
	    {
	        System.err.println("ERROR" + je.getMessage());
	        je.printStackTrace();
	        System.exit(0);
	    }
		
		//System.out.println( ontoModel.toString() );
		logger.debug("");logger.debug("");
		logger.debug( "Read the Ontology from file. ********************************************" );
		logger.debug("");logger.debug("");
		
		//AnnotationAssertion
		//Annotating Axioms and Entities
		ExtendedIterator<AnnotationProperty> api = ontoModel.listAnnotationProperties();
		while (api.hasNext()) {
			AnnotationProperty ap =  api.next();
			//entities.addAnotations("en:anotation", "", Type, Value);
			finalMessage += "< AnnotationAssertion " + ap.getURI() +  ">" +"\n";
			logger.debug( "< AnnotationAssertion " + ap.getURI() +  ">");

		}
		//ontology Management
		Map<String, String> prefix = ontoModel.getNsPrefixMap();
		for (Map.Entry<String, String> entry : prefix.entrySet())
		{
			if( entry.getKey()!=null && entry.getKey()!=""){
				entities.addAnotations("en:anotation", "NameSpacePrefixMap", entry.getKey(), entry.getValue());
				finalMessage += "< NameSpacePrefixMap " + entry.getKey() + " " + entry.getValue() + ">" +"\n";
				logger.debug( "< NameSpacePrefixMap " + entry.getKey() + " " + entry.getValue() + ">" );
			}

		}
		
		Set<String> imports = ontoModel.listImportedOntologyURIs();
		for (String entry : imports )
		{
			entities.addAnotations("en:anotation", "Imports", entry, "");
			finalMessage += "< imports" + entry+ ">" +"\n";
			logger.debug( "< imports" + entry+ ">" );
		}
		//Entity Declarations
		//TODO
		
		
		ExtendedIterator<OntClass> iter = ontoModel.listClasses();
		while (iter.hasNext()) {
			
			String EntityName ="";
			Boolean isAnonymous = false;//is anonymous ?
			
			OntClass ontClass = iter.next();
			ExtendedIterator<OntClass> classIter;
			ExtendedIterator<? extends OntClass> ontClassIter;
			ExtendedIterator<? extends OntResource> ontResourceIter;
			ExtendedIterator<? extends Resource> resourceIter;
			
			//Class
			if(ontClass.isAnon()) {
//				isAnonymous =true;
				//EntityName = entities.AddAnon("owl:Class");
				//ontClass.addProperty( ontoModel.createProperty( "rdf","about" ) , EntityName);
				
//				EntityName = entities.AddAnon(ontClass.getClass().getName());
				//logger.debug("isAnon: "+ontClass.isAnon() + " " );
				//entities.AddToAnon( "owl:Class", EntityName, Type, Value)
				
				//ignore anonymous class the first time
				//add it when use it as a object
				//logger.debug("\n class isAnon: "+ontClass.isAnon() + " " );
			}
			else {
				EntityName = ontClass.getURI();
				entities.addEntity("owl:Class", EntityName );
//				finalMessage += "< owl:Class " + EntityName + ">" + "\n";
				logger.debug( "< owl:Class " + EntityName + ">"  );
			}
			
			
			
			
			//if(ontClass.isClass()) logger.debug("isClass: "+ontClass.isClass());// all is class
			
			if(ontClass.isDataRange()) logger.debug("isAnon: "+ontClass.isDataRange()); //Answer true iff this RDFNode is an anonynous resource. 
			if(ontClass.isAnnotationProperty()) logger.debug("isAnnotationProperty: "+ontClass.isAnnotationProperty());
			if(ontClass.isIntersectionClass()) logger.debug("isIntersectionClass: "+ontClass.isIntersectionClass());
			if(ontClass.isComplementClass())logger.debug("isComplementClass: "+ontClass.isComplementClass());
			if(ontClass.isEnumeratedClass())logger.debug("isEnumeratedClass: "+ontClass.isEnumeratedClass());
			if(ontClass.isHierarchyRoot())logger.debug("isHierarchyRoot: "+ontClass.isHierarchyRoot());
			if(ontClass.isIndividual())logger.debug("isIndividual: "+ontClass.isIndividual());
			if(ontClass.isLiteral())logger.debug("isLiteral: "+ontClass.isLiteral());
			if(ontClass.isObjectProperty())logger.debug("isObjectProperty: "+ontClass.isObjectProperty());
			if(ontClass.isOntLanguageTerm())logger.debug("isOntLanguageTerm: "+ontClass.isOntLanguageTerm());
			if(ontClass.isProperty())logger.debug("isProperty: "+ontClass.isProperty());
			if(ontClass.isResource())logger.debug("isResource: "+ontClass.isResource());
			if(ontClass.isOntology())logger.debug("isOntology: "+ontClass.isOntology());
			if(ontClass.isRestriction())logger.debug("isRestriction: "+ontClass.isRestriction());
			if(ontClass.isUnionClass())logger.debug("isUnionClass: "+ontClass.isUnionClass());
			if(ontClass.isURIResource())logger.debug("isURIResource: "+ontClass.isURIResource());
			
			
			
			//Instance
			
//			ExtendedIterator<? extends OntResource> insIter = ontClass.listInstances();
//			while (insIter.hasNext()) {
//				//logger.debug("contain instances");
//				OntResource ors =insIter.next();
//				if(ors.isURIResource()){
//					entities.addToEntity("owl:Class", EntityName, "en:instance", ors.getURI(), isAnonymous);
//					entities.addEntity("owl:NamedIndividual",ors.getURI());
//					//logger.debug("owl:NamedIndividual" + ors.getURI());
//				}else{
//					//ors.
//				}
//				//finalMessage += "[ " + EntityName + " " + ors.getURI() + "]" + "\n";
//				//logger.debug( "[ " + EntityName + " " + ors.getURI() + "]" );
//			}
			
			
			
			//Class Hierarchies
//			if( ontClass.hasSubClass()){
//				classIter = ontClass.listSubClasses();
//				while( classIter.hasNext() ){
//					OntClass oc = classIter.next();
//					finalMessage += "< owl:Class " + oc.getURI() + "  rdfs:subClassOf " + EntityName + ">" + "\n";
//					logger.debug( "< owl:Class " + oc.getURI() + "  rdfs:subClassOf " + EntityName + ">");
//				
//				}
//			}
			if(ontClass.hasSuperClass()){
				ExtendedIterator<OntClass> supers = ontClass.listSuperClasses();
				while (supers.hasNext()) {
					OntClass sp = supers.next();
					if(ontClass.getSuperClass().getURI()!=null)
						entities.addToEntity("owl:Class", EntityName, "rdfs:subClassOf", sp.getURI(), isAnonymous);
					else{
						logger.debug( EntityName + " : the super class exist but don't have URI and local name." );
						logger.debug( EntityName + " : super isAnon " + sp.isAnon() );
					}
				}
				
//				finalMessage += "< owl:Class " + EntityName + "  rdfs:subClassOf " + ontClass.getSuperClass().getURI() + ">" + "\n";
//				logger.debug( "< owl:Class " + EntityName + "  rdfs:subClassOf " + ontClass.getSuperClass().getURI() + ">");
			
			}

			
			classIter = ontClass.listEquivalentClasses();
			while( classIter.hasNext() ){
//				logger.debug("contain EquivalentClasses");
				OntClass oc = classIter.next();
				if(oc.isURIResource())
					entities.addToEntity("owl:Class", EntityName, "owl:equivalentClass", oc.getURI(), isAnonymous);
//				finalMessage += "< owl:Class " + oc.getURI() + "  EquivalentWith " + EntityName + ">" + "\n";
				//logger.debug( "< owl:Class " + oc.getURI() + "  EquivalentWith " + EntityName + ">");
			
			}
			
			//Class Disjointness
			classIter = ontClass.listDisjointWith();
			while( classIter.hasNext() ){
				OntClass oc = classIter.next();
				entities.addToEntity("owl:Class", EntityName, "owl:disjointWith", oc.getURI(), isAnonymous);
//				finalMessage += "< owl:Class " + oc.getURI() + "  DisjointWith " + EntityName + ">" + "\n";
				//logger.debug( "< owl:Class " + oc.getURI() + "  DisjointWith " + EntityName + ">");
			
			}
			
			
			
			//Object Properties
			
			//property restriction
			if(ontClass.isRestriction() && ontClass.isURIResource()){
				//try to use pellet
//				logger.debug("restrict1");
//				ExtendedIterator<OntProperty> rops = ontClass.asRestriction().listDeclaredProperties();
//				while (rops.hasNext()) {
//					
//					OntProperty rop = rops.next();
//					logger.debug("restrict"+rop.toString());
//					if(rop.isURIResource()){
//						entities.addToEntity("owl:Class", EntityName, "owl:Restriction", rop.getURI(), isAnonymous);
//					}
//				}
				logger.debug("restrict");
				Restriction sup = ontClass.asRestriction();
				if (sup.isAllValuesFromRestriction()) {
		            displayRestriction( "all", sup.getOnProperty(), sup.asAllValuesFromRestriction().getAllValuesFrom() );
		        }
		        else if (sup.isSomeValuesFromRestriction()) {
		            displayRestriction( "some", sup.getOnProperty(), sup.asSomeValuesFromRestriction().getSomeValuesFrom() );
		        }
				 
			}
			
			
			
			
			//Equality and Inequality of Individuals
			ontResourceIter = ontClass.listInstances();
			while( ontResourceIter.hasNext() ){
				
				OntResource or = ontResourceIter.next();
				
				if(or.isClass()){
					//finalMessage += "< owl:Class " + oc.getURI() + "  rdfs:subClassOf " + EntityName + ">" + "\n";
					StmtIterator ps = or.listProperties();
					while( ps.hasNext() ){
						
						Statement statement = ps.next();
						
//						if(statement.is)
						
						entities.addToEntity("owl:Class", EntityName, statement.getString() , "", isAnonymous);
//						finalMessage += "< owl:Class " + EntityName + "  property " + ps.toString() + ">" + "\n";
//						logger.debug( "< owl:Class " + EntityName + "  property " + statement.getString() + ">");
					}
					
					//Equality and Inequality of Individuals
					resourceIter = or.listSameAs();
					while (resourceIter.hasNext()) {
						Resource rs = resourceIter.next();
						finalMessage += "< owl:Class " + or.getURI() + "  sameAs " + rs.toString() + ">" + "\n";
//						logger.debug( "< owl:Class " + or.getURI() + "  sameAs " + rs.toString() + ">");
					}
					resourceIter = or.listDifferentFrom();
					while (resourceIter.hasNext()) {
						Resource rs = resourceIter.next();
						finalMessage += "< owl:Class " + or.getURI() + "  differentFrom " + rs.toString() + ">" + "\n";
//						logger.debug( "< owl:Class " + or.getURI() + "  differentFrom " + rs.toString() + ">");
					}
					
				}else if(or.isDatatypeProperty()){
					//TO DO
					//Datatypes
					//DataPropertyAssertion( :hasAge :John "51"^^xsd:integer )
					//NegativeDataPropertyAssertion( :hasAge :Jack "53"^^xsd:integer )
					//DataPropertyDomain( :hasAge :Person )
					//DataPropertyRange( :hasAge xsd:nonNegativeInteger ) 
					//or.asDatatypeProperty().get
//					logger.debug("isDatatypeProperty");
					//logger.debug( "< owl:ObjecttypeProperty " + or.getURI() + "  domain " +   ">");
					//or.get
				}
				
				
				
				logger.debug( "" );
			}
			//NegativeObjectPropertyAssertion
			//TODO
			
					
			ExtendedIterator<Individual> indIter = ontoModel.listIndividuals();
			while (indIter.hasNext()) {
				Individual ors =indIter.next();
	//			logger.debug("contain Individuals " + ors.isURIResource());
				if(ors.isURIResource() && !ors.getOntClass().isAnon()){
					entities.addToEntity("owl:NamedIndividual", ors.getURI(), "rdf:type", ors.getOntClass().getURI() , false);
					entities.addEntity("owl:NamedIndividual",ors.getURI());
				}else if(ors.getOntClass().isAnon()){
	//				logger.debug("OntClass is Anon");
					//ors.
				}
				//finalMessage += "[ " + EntityName + " " + ors.getURI() + "]" + "\n";
				//logger.debug( "[ " + EntityName + " " + ors.getURI() + "]" );
			}
		}
		
		ExtendedIterator<DatatypeProperty> dps =ontoModel.listDatatypeProperties();
		while (dps.hasNext()) {
			DatatypeProperty dp = dps.next();
			if(dp.isURIResource()){
				entities.addEntity("owl:DatatypeProperty", dp.getURI());
				ExtendedIterator<? extends OntProperty> ops =dp.listEquivalentProperties();
				while(ops.hasNext()){
					OntProperty op = ops.next();
					if(op.isURIResource()){
						entities.addToEntity("owl:DatatypeProperty", dp.getURI(),"owl:equivalentProperty",op.getURI(),false );
					}else{
//						processAnonOntProperty(op);
					}
				}
				ExtendedIterator<? extends OntResource> ors =dp.listDomain();
				while(ors.hasNext()){
					OntResource or =  ors.next();
					if(or.isURIResource()){
						entities.addToEntity("owl:DatatypeProperty", dp.getURI(),"rdfs:domain",or.getURI(),false );
					}else{
//						processAnonOntResource(or);
					}
				}
				ors =dp.listRange();
				while(ors.hasNext()){
					OntResource or = ors.next();
					if(or.isURIResource()){
						entities.addToEntity("owl:DatatypeProperty", dp.getURI(),"rdfs:range",or.getURI(),false );
					}else{
//						processAnonOntResource(or);
					}
				}
				ExtendedIterator<Resource> rs = dp.listRDFTypes(true);
				while (rs.hasNext()) {
					Resource r = rs.next();
					entities.addToEntity("owl:DatatypeProperty", dp.getURI(),"rdf:type",r.getURI(),false );
				}
			}
		}
		
		
		
		ExtendedIterator<ObjectProperty> opsOnt =ontoModel.listObjectProperties();
		while (opsOnt.hasNext()) {
					
			ObjectProperty opOnt = opsOnt.next();
			
			logger.debug("listObjectProperties " + opOnt.getLocalName());
			
			if(opOnt.isURIResource()){
				entities.addEntity("owl:ObjectProperty", opOnt.getURI());
				ExtendedIterator<? extends OntProperty> ops =opOnt.listEquivalentProperties();
				while(ops.hasNext()){
					OntProperty op = ops.next();
					if(op.isURIResource()){
						entities.addToEntity("owl:ObjectProperty", opOnt.getURI(),"owl:equivalentProperty",op.getURI(),false );
					}else{
//						processAnonOntProperty(op);
					}
				}
				ExtendedIterator<? extends OntResource> ors =opOnt.listDomain();
				while(ors.hasNext()){
					OntResource or =  ors.next();
					if(or.isURIResource()){
						entities.addToEntity("owl:ObjectProperty", opOnt.getURI(),"rdfs:domain",or.getURI(),false );
					}else{
//						processAnonOntResource(or);
					}
				}
				ors =opOnt.listRange();
				while(ors.hasNext()){
					OntResource or = ors.next();
					if(or.isURIResource()){
						entities.addToEntity("owl:ObjectProperty", opOnt.getURI(),"rdfs:range",or.getURI(),false );
					}else{
//						processAnonOntResource(or);
					}
				}
				ExtendedIterator<Resource> rs = opOnt.listRDFTypes(true);
				while (rs.hasNext()) {
					Resource r = rs.next();
					entities.addToEntity("owl:ObjectProperty", opOnt.getURI(),"rdf:type",r.getURI(),false );
//					logger.debug("rdf:type " + r.getURI());
				}
			}
		}
		
		
		//Object property should be extract from class or individual
//		ontoModel.listAllOntProperties()();
//		ExtendedIterator<OntProperty> properties = ontoModel.listAllOntProperties();
//		//logger.debug("has Ont property ? " + properties.hasNext() );
//		while (properties.hasNext()) {
//			OntProperty op = properties.next();
//			//logger.debug("property " + op.getNameSpace() + " " +op.getLocalName());
//			
//			// declare the relation between class and property
//			//OntClass opClass = whoHasTheProperty(ontClass,op);
//			if(op.isURIResource() && op.getDomain()!=null ){
//				logger.debug("op.getURI() " + op.getURI());
//				entities.addToEntity( "owl:DatatypeProperty", op.getURI(), "rdfs:domain" , op.getDomain().getURI(), false );
//			}else{
//				logger.debug("property is anonymous or don't have domain" );
//			}
//			
//			ExtendedIterator<? extends OntResource> ds ;
////			ds = op.listDomain();
////			while( ds.hasNext()){
////				OntResource d =ds.next();
////				if(op.isURIResource() && d.isURIResource())
////					entities.addToEntity("owl:ObjectProperty", op.getURI(), "rdfs:domain" , d.getURI(), isAnonymous);
//////				finalMessage += "< owl:Class " + EntityName + "  property " + op.toString() + " domain " + d.getURI()+ ">" + "\n";
//////				logger.debug( "< owl:Class " + EntityName + "  property " + op.toString() + " domain " + d.getURI()+ ">");
////			}
//			ds = op.listRange();
//			while( ds.hasNext()){
//				OntResource d =ds.next();
//				if(op.isURIResource() && d.isURIResource()){
////					entities.addToEntity("owl:ObjectProperty", op.getURI(), "rdfs:range" , d.getURI(), isAnonymous);
////				finalMessage += "< owl:Class " + EntityName + "  property " + op.toString() + " range " + d.getURI()+ ">" + "\n";
////				logger.debug( "< owl:Class " + EntityName + "  property " + op.toString() + " range " + d.getURI()+ ">");
//				}
//			}
//		}
    	
		logger.debug( "Finish, Write to file." );
		
		PrintWriter out = null;
		try {
			out = new PrintWriter( pathPrefix + "ontology.enSchema");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//out.println(finalMessage);
		out.println(entities.toENSchema());
		out.close();

    }


	private static OntClass whoHasTheProperty(OntClass ontClass, OntProperty op) {
		OntClass oc = ontClass;
//		logger.debug("who has 1 " + oc.getURI() + " has property "+ oc.hasDeclaredProperty(op,false));
		while( oc.hasSuperClass()  ){
//			logger.debug("who has 2 " + oc.getSuperClass().getURI());
			if( oc.getSuperClass().hasDeclaredProperty(op,false) ){
//				logger.debug("who has 3 " + oc.getSuperClass().hasDeclaredProperty(op,false));
				oc = oc.getSuperClass();
			}else{
				break;
			}
		}
		
		return oc;
	}
	
	private static void processProperties( OntClass ontClass, boolean isAnonymous){
		//Property Hierarchies
		//SubObjectPropertyOf( :hasWife :hasSpouse )   is it sub equivalentclass??
		
		//Domain and Range Restrictions
		ExtendedIterator<OntProperty> properties = ontClass.listDeclaredProperties();
		while (properties.hasNext()) {
			OntProperty op = properties.next();
			String rdfType="owl:ObjectProperty";
			
			ExtendedIterator<Resource> rs =op.listRDFTypes(true);
			while (rs.hasNext()){
				Resource resource = rs.next();
				logger.debug("RDF TYPE " + resource.getLocalName());
//				if(resource.getLocalName() == "ObjectProperty"){
//					rdfType="owl:ObjectProperty";
//					entities.addEntity( rdfType, op.getURI());
//					
//					break;
//				}
//				else if(resource.getLocalName() == "DataTypeProperty"){
//					rdfType = "owl:DataTypeProperty";
//					entities.addEntity( rdfType , op.getURI());
//					break;
//				}
//				else if(resource.getLocalName() == "SymmetricProperty"){
//					rdfType = "owl:SymmetricProperty";
//					entities.addEntity( rdfType , op.getURI());
//					break;
//				}
				if(resource.getLocalName() == "ObjectProperty"){
					rdfType="owl:ObjectProperty";
					entities.addEntity( rdfType, op.getURI());
					
					break;
				}else{
					entities.addToEntity( "owl:ObjectProperty" , op.getURI(), "rdf:type", "owl:" + resource.getLocalName());
				}
			}
			
			logger.debug("DeclaredProperty: " + op.getLocalName());
			
			// declare the relation between class and property
			OntClass opClass = whoHasTheProperty(ontClass,op);
			if(opClass.isURIResource() && op.isURIResource()){
//				logger.debug("opClass " + opClass.getURI());
				//entities.addToEntity( "owl:DatatypeProperty", op.getURI(), "rdfs:domain" , opClass.getURI(), isAnonymous );
				entities.addToEntity( rdfType , op.getURI(), "rdfs:domain" , opClass.getURI(), isAnonymous );
				
			}
			
			ExtendedIterator<? extends OntResource> ds ;
//			ds = op.listDomain();
//			while( ds.hasNext()){
//				OntResource d =ds.next();
//				if(op.isURIResource() && d.isURIResource())
//					entities.addToEntity("owl:ObjectProperty", op.getURI(), "rdfs:domain" , d.getURI(), isAnonymous);
////				finalMessage += "< owl:Class " + EntityName + "  property " + op.toString() + " domain " + d.getURI()+ ">" + "\n";
////				logger.debug( "< owl:Class " + EntityName + "  property " + op.toString() + " domain " + d.getURI()+ ">");
//			}
			ds = op.listRange();
			while( ds.hasNext()){
				OntResource d =ds.next();
				if(op.isURIResource() && d.isURIResource()){
					entities.addToEntity(rdfType, op.getURI(), "rdfs:range" , d.getURI(), isAnonymous);
//				finalMessage += "< owl:Class " + EntityName + "  property " + op.toString() + " range " + d.getURI()+ ">" + "\n";
//				logger.debug( "< owl:Class " + EntityName + "  property " + op.toString() + " range " + d.getURI()+ ">");
				}
			}
			
			ExtendedIterator<? extends OntProperty> sp = op.listSuperProperties();
			while(sp.hasNext()){
//				logger.debug("has sup");
				OntProperty top = sp.next();
				if(top.isURIResource()){
					entities.addToEntity(rdfType, op.getURI(), "rdfs:subPropertyOf" , top.getURI(), isAnonymous);
				}
			}
			
			sp = op.listEquivalentProperties();
			
			while(sp.hasNext()){

				OntProperty top = sp.next();
				if(top.isURIResource()){
					entities.addToEntity(rdfType, op.getURI(), "owl:equivalentProperty" , top.getURI(), isAnonymous);
				}
			}
			
			sp = op.listInverseOf();
			while(sp.hasNext()){

				OntProperty top = sp.next();
				if(top.isURIResource()){
					entities.addToEntity(rdfType, op.getURI(), "owl:inverseOf" , top.getURI(), isAnonymous);
				}
			}
			
			ExtendedIterator<? extends Resource> dr = op.listDifferentFrom();
			while(dr.hasNext()){

				logger.debug("hasdisjoint");
				Resource drs = dr.next();
				if(drs.isURIResource()){
					entities.addToEntity(rdfType, op.getURI(), "owl:propertyDisjointWith" , drs.getURI(), isAnonymous);
				}
			}
			

		
		//TODO
		//Advanced Use of Datatypes
		
		
		}
			
	}

	public static void displayRestriction( Restriction sup ) {
        if (sup.isAllValuesFromRestriction()) {
            displayRestriction( "all", sup.getOnProperty(), sup.asAllValuesFromRestriction().getAllValuesFrom() );
        }
        else if (sup.isSomeValuesFromRestriction()) {
            displayRestriction( "some", sup.getOnProperty(), sup.asSomeValuesFromRestriction().getSomeValuesFrom() );
        }
    }
	public static void displayRestriction( String qualifier, OntProperty onP, Resource constraint ) {
        String out = String.format( "%s %s %s",
                                    qualifier, renderURI( onP ), renderConstraint( constraint ) );
        System.out.println( "american pizza: " + out );
    }
	public static Object renderURI( Resource onP ) {
        String qName = onP.getModel().qnameFor( onP.getURI() );
        return qName == null ? onP.getLocalName() : qName;
    }
	public static Object renderConstraint( Resource constraint ) {
        if (constraint.canAs( UnionClass.class )) {
            UnionClass uc = constraint.as( UnionClass.class );
            // this would be so much easier in ruby ...
            String r = "union{ ";
            for (Iterator<? extends OntClass> i = uc.listOperands(); i.hasNext(); ) {
                r = r + " " + renderURI( i.next() );
            }
            return r + "}";
        }
        else {
            return renderURI( constraint );
        }
    }


}
