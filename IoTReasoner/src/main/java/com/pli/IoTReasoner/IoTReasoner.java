package com.pli.IoTReasoner;

import com.pli.sesamedatabase.SesameAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.*;
import java.util.*;

import org.apache.commons.io.IOUtils;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.PrintUtil;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;

/**
 * Created with IntelliJ IDEA.
 * User: amaarala
 * Date: 26.11.2013
 * Time: 16:47
 * To change this template use File | Settings | File Templates.
 * 
 * Edited by : Pingjiang Li
 * Date: 4/13/2016
 * Time: 10:00 AM
 * 
 */
public final class IoTReasoner{

    //private String ontology = "./ontology.owl";
    private String ontology = "ontology.owl";
    private String graphURI = "http://localhost/SensorSchema/ontology#";
    private String prefix = "obs";
    //private String rulesFile = "./rules_jena.txt";
    private String rulesFile = "rules_jena.txt";

    private InfModel rulesmodel = null;

    private String dataFormat = "N3";

    public Model dataModel = null;
    private OntModel ontologyModel = null;
    static public SesameAdapter sesameAdapter = null;

    //private static IoTReasoner iotreasoner = null;
    private Model schema = null; //ontology, tbox --pli

    private Reasoner ruleReasoner = null;
    //private RepositoryConnection con =null;

    //private OntModel ontmodel = null;

    private static final Logger LOGGER = LoggerFactory
            .getLogger(IoTReasoner.class);

    private List rules = new ArrayList<Object>();

    public Model getDeductionsModel() {
        return deductionsModel;
    }

    private Model deductionsModel;

    public IoTReasoner() {

	    schema = ModelFactory.createDefaultModel();
	
		// Use the FileManager to find the input file
		InputStream in = FileManager.get().open(ontology);
	
		if (in == null)
			throw new IllegalArgumentException("File: "+ontology+" not found");
		// Read the RDF/XML file
		schema.read(in, null);

        PrintUtil.registerPrefix(prefix, graphURI);

        this.initializeGenericReasoner();

    }

    public IoTReasoner(String ontology, String graphURI, String prefix, String rulesFile) {
        this.setRulesFile(rulesFile);
        this.setPrefix(prefix);
        this.setGraphURI(graphURI);
        this.setOntology(ontology);

        schema =  ModelFactory.createDefaultModel();

        // load data into model
        //FileManager.get().readModel( this.dataModel, "observations.rdf" );
        FileManager.get().readModel(schema, ontology );

        PrintUtil.registerPrefix(prefix, graphURI);

        this.initializeGenericReasoner();

    }

    public IoTReasoner(String rulesFile) {
        this.setRulesFile(rulesFile);

        schema = ModelFactory.createDefaultModel();

        // load data into model
        //FileManager.get().readModel( this.dataModel, "observations.rdf" );
        FileManager.get().readModel(schema, ontology );

        PrintUtil.registerPrefix(prefix, graphURI);

        this.initializeGenericReasoner();

    }

    public void initJsonLD(){
        //JenaJSONLD.init();
    }

    public IoTReasoner(List<Rule> rl) {


        this.rules = rl;

        schema = ModelFactory.createDefaultModel();

        //FileManager.get().readModel(schema, ontology );
        // Create an empty model

        //----new code
	// Use the FileManager to find the input file
	InputStream in = FileManager.get().open(ontology);

	if (in == null)
		throw new IllegalArgumentException("File: "+ontology+" not found");
	// Read the RDF/XML file
	schema.read(in, null);

        PrintUtil.registerPrefix(prefix, graphURI);

        this.initializeGenericReasoner();
    }

    public void initializeGenericReasoner(){


        //Reasoner iotreasoner = ReasonerRegistry.getOWLReasoner();

        ruleReasoner = new GenericRuleReasoner(getRules());

        //LPBackwardRuleReasoner lpbrr = new LPBackwardRuleReasoner(getRules());

        ruleReasoner.bindSchema(schema); //bindSchema(Model tbox)
        //This is most commonly used to attach an ontology (a set of tbox axioms in description logics jargon) to a reasoner. 


    }

    /*public static OntModel getOntologyModel(ResultSet rs, String prefix, String graphURI){

        OntModel tempModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RULE_INF, ModelFactory.createDefaultModel());
        tempModel.setNsPrefix(prefix, graphURI);
        //System.out.println(" - " + rs.toString());


        //set data to iotreasoner datamodel from sparql results and ini

        //tempModel.write(System.out, dataFormat);

        while(rs.hasNext()) {
            QuerySolution sol = rs.nextSolution();
            Resource resource = sol.getResource("s");
            RDFNode subject = sol.get("s");
            RDFNode pred = sol.get("p");
            RDFNode obj = sol.get("o");

            //System.out.println(" - " + sol.toString());

            //Individual jamsp1 = tempModel.createIndividual(graphURI + "JamSpeed1", instance );
            Property prop = subject.getModel().getProperty(pred.toString());
            //Property prop = tempModel.createProperty(pred.toString());
            tempModel.add(resource, prop, obj);

        }

        //StmtIterator stmit = tempModel.listStatements();
        //while(stmit.hasNext())         {
        //    System.out.println(" STMT - " + stmit.nextStatement().toString());
        //}
        //final StringWriter wr = new StringWriter();

        return tempModel;

    }

    public  OntModel getOntologyModel(ResultSet rs){

        OntModel tempModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RULE_INF, ModelFactory.createDefaultModel());
        tempModel.setNsPrefix(prefix, graphURI);
        //System.out.println(" - " + rs.toString());


        //set data to iotreasoner datamodel from sparql results and ini

        //tempModel.write(System.out, dataFormat);

        while(rs.hasNext()) {
            QuerySolution sol = rs.nextSolution();
            //System.out.println(" - " + sol.toString());
            Resource resource = sol.getResource("s");
            RDFNode subject = sol.get("s");
            RDFNode pred = sol.get("p");
            RDFNode obj = sol.get("o");

            //Individual jamsp1 = tempModel.createIndividual(graphURI + "JamSpeed1", instance );
            Property prop = subject.getModel().getProperty(pred.toString());
            //Property prop = tempModel.createProperty(pred.toString());
            tempModel.add(resource, prop, obj);

        }

        //StmtIterator stmit = tempModel.listStatements();
        //while(stmit.hasNext())         {
        //    System.out.println(" STMT - " + stmit.nextStatement().toString());
        //}
        //final StringWriter wr = new StringWriter();

        return tempModel;

    }*/

    public OntModel getInferredModel() {
        return ontologyModel;
    }

    public SesameAdapter getSesameAdapter() {
        //TODO: some strange things happening here, format is not changed in all cases and when changed other than RDF/XML storing to repository fails
        if (sesameAdapter == null){
            org.openrdf.rio.RDFFormat sformat =  org.openrdf.rio.RDFFormat.RDFXML;
            /*if(dataFormat == "N3")
               sformat = org.openrdf.rio.RDFFormat.N3;
            if(dataFormat == "N-TRIPLE")
                sformat = RDFFormat.NTRIPLES;
            if(dataFormat == "TURTLE")
                sformat = org.openrdf.rio.RDFFormat.TURTLE;*/

            //sesameAdapter = new SesameAdapter(graphURI, sformat);
            sesameAdapter = new SesameAdapter(sformat);
        }
        return sesameAdapter;
    }

    public void setGraphURI(String graphURI) {
        this.graphURI = graphURI;
    }

    public void setRulesmodel(InfModel rulesmodel) {
        this.rulesmodel = rulesmodel;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public  Reasoner getRuleReasoner() {
        return ruleReasoner;
    }

    public  void setRuleReasoner(Reasoner ruleReasoner) {
        this.ruleReasoner = ruleReasoner;
        ruleReasoner.bindSchema(schema);
    }

    public String getRulesFile() {
        return rulesFile;
    }

    public void setRulesFile(String rulesFile) {
        this.rulesFile = rulesFile;
    }

    public void updateSesameRepository(Model model)  {
        System.out.println("******************* update Sesame Repository");

        // creates a new, empty in-memory model
       /* RemoteRepositoryManager manager = new RemoteRepositoryManager(sesameServer);
        try {
            manager.initialize();
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }     */
        // creates a new, empty in-memory model

        //Resource ENTITY_TYPE = rulesmodel.getResource("http://localhost/SensorSchema/ontology#Jam");
        /*StmtIterator it = rulesmodel.getDeductionsModel().listStatements(null, RDF.type, ENTITY_TYPE);

        while (it.hasNext()) {
            System.out.println(" - " + PrintUtil.print(it.nextStatement().toString()));
            //addStatement(getSesameConnection(), it.nextStatement());
        }*/

        
        //write statements as RDF/XML
        final StringWriter wr = new StringWriter();
        //inferredModel.write(System.out, dataFormat);

        model.write(wr, null, dataFormat);

        //Add statements to RDF DB

        getSesameAdapter().addStatements(getSesameAdapter().createConnection(), wr);

        //rulesmodel.write(System.out, dataFormat);

        //showModelSize(this.dataModel);

    }

    

    

    //TODO: not working this way, rules must be parsed other way or read from files separately,but it is expensive
    @Deprecated
    public InfModel  inferModelSelective(String[] classes, final boolean store) {


        //Inference 

        OntModel newKnowledgeModel;
        newKnowledgeModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RULE_INF, ModelFactory.createDefaultModel());
        newKnowledgeModel.setNsPrefix(prefix, graphURI);

        for(String classType : classes) {

            new HashSet<String>();
            //Parse rdf:types from rule body
            List<Rule> headRuleList = new ArrayList<Rule>();

            this.getRules().stream().forEach((rule) -> {
                for (String classe : classes) {
                    if (rule.getName().equals(classType)) {
                        headRuleList.add(rule);
                    }
                }
            });

            if(!headRuleList.isEmpty()){
            InfModel infModel = inferFromRule(headRuleList);

            //Property p = dataModel.getProperty(graphURI, "rdf:type");

            //This takes much longer with backward rules
            //TODO: propably there is a bug, does not go like with non-selective, different instances were saved
            OntModel om = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RULE_INF, infModel);
            OntClass oc = om.getOntClass(graphURI + classType);

            if (oc != null) {
                ExtendedIterator<? extends OntResource> instances = oc.listInstances();
                ExtendedIterator<? extends OntProperty> propit = oc.listDeclaredProperties();
                ArrayList<String> proplist = new ArrayList<String>();

                while (propit.hasNext()) {


                    proplist.add(propit.next().toString());
                }
                while (instances.hasNext()) {

                    OntResource instance = instances.next();

                    //Individual jamsp1 = tempModel.createIndividual(graphURI + "JamSpeed1", instance );

                    //LOGGER.info(" ---------Inferred type--------- " + classType);

                    newKnowledgeModel.add(instance, RDF.type, om.getResource(graphURI + classType));

                    proplist.stream().forEach((prop) -> {
                        //addStatement(getSesameConnection(), it.nextStatement());
                        try {
                            if (instance.getProperty(om.getOntProperty(prop)) != null) {
                                //System.out.println(prop + ": " + instance.getProperty(om.getOntProperty(prop)).getString());

                                newKnowledgeModel.add(instance, om.getOntProperty(prop), instance.getProperty(om.getOntProperty(prop)).getString());
                            }
                        } catch (NoSuchElementException e) {

                        }
                    }); //TODO: try adding one statement at time

                }

            }  }


        }
        if (store == true)
            updateSesameRepository(newKnowledgeModel);

        return newKnowledgeModel;


    }

    public InfModel inferModel2(String[] classes, boolean store){

        //Inference
        InfModel infModel = inferFromRules();

   /*     List<String> interestingEvents = Arrays.asList(classes);

        OntModel om = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RULE_INF, infModel);

        //LOGGER.info(" --------- inferred --------- "+((new Date()).getTime()-time));

        OntModel newKnowledgeModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RULE_INF, ModelFactory.createDefaultModel());
        newKnowledgeModel.setNsPrefix(prefix, graphURI);
    */
        /*
        //TESTING all rules
        Set<String> types = new HashSet<String>();
        //Parse rdf:types from rule body
        for(Rule rule : getRules()){

            types.add(rule.getName());


        }*/

        this.updateSesameRepository(infModel.getDeductionsModel());

        return infModel;
    }

    public long inferModel(boolean store){

        String[] classes = { "HighAvgSpeed4","RightTurn", "LeftTurn", "UTurn", "Jam", "HighAvgSpeed", "LongStop", "HighAcceleration", "HighDeacceleration", "VeryLongStop","LowSpeed"};
        //String[] classes = {"RightTurn", "LeftTurn", "UTurn", "Jam",  "LongStop"};


        //Inference
        InfModel infModel = inferFromRules(); //The content of the model is the input rdf data --pli
        //infer new statement from original rdf data and jena rules 

        List<String> interestingEvents = Arrays.asList(classes);

        OntModel om = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RULE_INF, infModel);//add xmlns:owl,xmlns:rdfs,xmlns:xsd on infModel--pli
        //OWL_DL_MEM_RULE_INF: A specification for OWL DL models that are stored in memory and use the OWL rules inference engine for additional entailments
        //OWL reasoner include subclass inheritance, property inheritance and cardinality reasoning -pli
        
        //LOGGER.info(" --------- inferred --------- "+((new Date()).getTime()-time));

        //OntModel newKnowledgeModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RULE_INF, ModelFactory.createDefaultModel());
        //newKnowledgeModel.setNsPrefix(prefix, graphURI);

        Model inferencedInstancesModel = ModelFactory.createDefaultModel();//it seems this model is the result of the infered facts, it begins from an empty model --pli
        //
        // the content of inferencedInstancesModel:
        //<rdf:RDF
        //    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" >
        //</rdf:RDF>

        inferencedInstancesModel.setNsPrefixes(infModel.getNsPrefixMap());
        //seems it used for storing the final results(only new added statements). --pli

        //the content of become:
        //<rdf:RDF
        //    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        //    xmlns:obs="http://localhost/SensorSchema/ontology#"
        //    xmlns:owl="http://www.w3.org/2002/07/owl#"
        //    xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
        //    xmlns:xsd="http://www.w3.org/2001/XMLSchema#" >
        //</rdf:RDF>

        /*
        //TESTING all rules
        Set<String> types = new HashSet<String>();
        //Parse rdf:types from rule body
        for(Rule rule : getRules()){

            types.add(rule.getName());


        }*/
        //infer according to the classtype --pli
        //System.out.println( "_______________________      new round" );
        interestingEvents.stream().forEach((String classType) -> {
            //Property p = dataModel.getProperty(graphURI, "rdf:type");
            //This takes much longer with backward rules
            //TODO: optimize for backward rules
            OntClass oc = om.getOntClass(graphURI + classType); //ontology class is ontology concept, ontology contain concepts and relations --pli
            //get  a concept from ontology model under class Type, namely, create a "turn left concept" etc.
            //If the ontology model contain this concept, process, not contain then pass
            //System.out.println( classType+" : oc != null : " + (oc != null) );
            if (oc != null) {
                ExtendedIterator<? extends OntResource> instances = oc.listInstances();// a wine class main contain instances like red wine A, red wine B and beer. --pli
                ExtendedIterator<? extends OntProperty> propit = oc.listDeclaredProperties();// get the properties related with the class (not for particular instance but for all) --pli
                ArrayList<String> proplist = new ArrayList<String>();
                
                //convert the properties to a string array --pli
                while (propit.hasNext()) {
                    proplist.add(propit.next().toString());
                }
                System.out.println( "_______________________classType: "+classType );
                //process each instance
                while (instances.hasNext()) {
                    
                    OntResource instance = instances.next();
                    
                    //Individual jamsp1 = tempModel.createIndividual(graphURI + "JamSpeed1", instance );
                    
                    //LOGGER.info(" ---------Inferred type--------- " + classType);
                    
                    inferencedInstancesModel.add(instance, RDF.type, om.getResource(graphURI + classType));//add statement --pli
                    //Each arc in an RDF Model is called a statement. Each statement asserts a fact about a resource. A statement has three parts:--pli
                    //the subject is the resource from which the arc leaves
                    //the predicate is the property that labels the arc
                    //the object is the resource or literal pointed to by the arc
                    //Model.add( resource , property, resource), namely, add a triple, object-relation-object--pli
                    //add(Resource rsrc, Property prprt, RDFNode rdfn);--pli
                    proplist.stream().forEach((String prop) -> {
                        //addStatement(getSesameConnection(), it.nextStatement());
                        try{
                            if(instance.getProperty(om.getOntProperty(prop))!=null){
                                //System.out.println(prop + ": " + instance.getProperty(ontmodel.getOntProperty(prop)).getString());
                                //System.out.println( "om.getOntProperty(prop): " + om.getOntProperty(prop).toString() );
                                //System.out.println(prop + ": " + instance.getProperty(om.getOntProperty(prop)).getString());
                                
                                inferencedInstancesModel.add(instance, om.getOntProperty(prop), instance.getProperty(om.getOntProperty(prop)).getString());
                                //om.getOntProperty(prop) get the property(relation) of name prop
                                //instance.getProperty(om.getOntProperty(prop)) get the value of that property in that instance
                            }
                        }catch(NoSuchElementException e){
                            
                        }
                    }); //TODO: try adding one statement at time
                    //updateSesameRepository(ioTReasoner.getInferredModel());
                }
            }
        });

        long databaseTime=0;
        if(store == true && !inferencedInstancesModel.isEmpty()){//save to the sesame database --pli
        	Date startTime = new Date();
        	this.updateSesameRepository(inferencedInstancesModel);
        	Date finishTime = new Date();
        	databaseTime = finishTime.getTime()-startTime.getTime();
        }

        //tempModel.write(System.out);
        //this.ontologyModel = inferencedInstancesModel;

        //return inferencedInstancesModel;
        return databaseTime;

    }
    

    public InfModel inferFromRules() {

        (new Date()).getTime();

        //debug
        //System.out.println( "ruleReasoner:"+ruleReasoner.toString() );
        //dataModel.write(System.out);
        //System.out.println( );
        
        this.rulesmodel = ModelFactory.createInfModel(ruleReasoner, dataModel);// ruleReasoner contain rule, dataModel contain RDF data, this part do the reasoning!  --pli
        
        //debug
        /*rulesmodel.write(System.out);
        System.out.println( );
        System.out.println( "this.rulesmodel.listStatements():" );
        StmtIterator it = this.rulesmodel.listStatements();
		
        while ( it.hasNext() )
        {
                Statement stmt = it.nextStatement();

                Resource subject = stmt.getSubject();
                Property predicate = stmt.getPredicate();
                RDFNode object = stmt.getObject();

                System.out.println( subject.toString() + " " + predicate.toString() + " " + object.toString() );
        }*/

        //System.out.println(" ---------inferred 1--------- "+((new Date()).getTime()-time));
        this.rulesmodel.setNsPrefix(prefix, graphURI);//set name space prefix --pli

        //Deductions model not created from backward rules
        //to get only deducted triples strictly from the model,
        rulesmodel.getDeductionsModel().setNsPrefix("obs", "http://localhost/SensorSchema/ontology#");
        this.deductionsModel = rulesmodel.getDeductionsModel(); //Returns a derivations model. --pli
        //The rule reasoners typically create a graph containing those triples added to the base graph due to rule firings. --pli
        //In some applications it can useful to be able to access those deductions directly, --pli
        //without seeing the raw data which triggered them. --pli
        //In particular, this allows the forward rules to be used as if they were rewrite transformation rules.--pli
        
        
        
        //updateSesameRepository(rulesmodel.getDeductionsModel());

        return rulesmodel;
    }

    public InfModel inferFromRule(List<Rule> rules) {

        (new Date()).getTime();

        GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
        reasoner.bindSchema(schema);

        //LPBackwardRuleReasoner lpbrr = new LPBackwardRuleReasoner(getRules());

        InfModel infModel = ModelFactory.createInfModel(reasoner, dataModel);

        //System.out.println(" ---------inferred 1--------- "+((new Date()).getTime()-time));
        infModel.setNsPrefix(prefix, graphURI);

        //Deductions model not created from backward rules
        //to get only deducted triples strictly from the model,
        infModel.getDeductionsModel().setNsPrefix("obs", "http://localhost/SensorSchema/ontology#");

        //updateSesameRepository(rulesmodel.getDeductionsModel());

        //OntModel om = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RULE_INF, infModel.getDeductionsModel());

        //LOGGER.info(" --------- inferred --------- "+((new Date()).getTime()-time));


        return infModel;
    }

    protected void printStatements(Model m, Resource s, Property p, Resource o) {
        for (StmtIterator i = m.listStatements(s,p,o); i.hasNext(); ) {
            Statement stmt = i.nextStatement();
            System.out.println(" - " + PrintUtil.print(stmt));
        }
    }

    protected void showModelSize( Model m ) {
        System.out.println( String.format( "The model contains %d triples", m.size() ) );
    }

    /*public static IoTReasoner getReasoner() {
        return this;
    }*/

    public List<Rule> getRules(){
        //GPSObservation(?o), hasVelocity(?o, ?v), lessThan(?v, "5"^^int) -> Stop1(?o)
        //String stop1 =   "[stop1: (?d rdf:type obs:Observation)(?d obs:hasVelocity ?v) lessThan(?v,'5'^^xsd:integer) -> (?d rdf:type obs:Stop1)]";
        //GPSObservation(?o), Stop1(?j1), hasID(?j1, ?ID1), hasID(?o, ?ID2), hasSender(?j1, ?s), hasSender(?o, ?s), hasVelocity(?o, ?v), add(?ID2, ?ID1, "1"^^int), lessThan(?v, 3) -> Stop2(?o)
        List<Rule> rules = new ArrayList<Rule>();
        if(this.rules.isEmpty() && this.rulesFile != null) {
            rules = Rule.rulesFromURL(this.rulesFile);
            this.rules = rules;
        }
        
        //System.out.println("_____ RUN: getRules size=" + this.rules.size());
        
        /*Map<String, List<Rule>> rulez = new HashMap<String, List<Rule>>();
        
        for (Rule rule : rules) {
            if(rulez.containsKey(rule.getName()))
                rulez.get(rule.getName()).add(rule);
            else {
                List<Rule> rl = new ArrayList<Rule>();
                rl.add(rule);
                rulez.put(rule.getName(), rl);
            }
        }*/

        return this.rules;
    }

    public Model getDataModel() {
        return dataModel;
    }

    public Model getSchema() {
        return schema;
    }

    public void initDataModel(Model dataModel) {
        this.dataModel = dataModel;
        //inferFromRules();
        //initOntmodel();
    }

    public void setOntology(String ontology) {
        this.ontology = ontology;
    }

    /*public void createDataModel(String rdfData) {

        Model model = ModelFactory.createDefaultModel();
        StringReader reader = new StringReader(rdfData);
        try{
             model.read(reader, null, dataFormat);
        }catch(org.apache.jena.riot.RiotException e) {
        }
        this.dataModel = model;

        //inferFromRules();
        //initOntmodel();

    }*/

    public String getDataFormat() {
        return dataFormat;
    }

    public void setDataFormat(String dataFormat) {
        this.dataFormat = dataFormat;
    }

    public void setDataModel(Model dataModel) {
        this.dataModel = dataModel;
    }
    public void setDataModel(String rdfData) {
    	Model rdfModel = ModelFactory.createDefaultModel();

        try {
            rdfModel.read(IOUtils.toInputStream(rdfData, "UTF-8"), null, "RDF/XML");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println("run crash.");
            e.printStackTrace();
        }
        this.setDataModel(rdfModel);
    }
    
}
