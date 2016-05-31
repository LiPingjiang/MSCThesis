package com.mscthesis.pli.iotgateway;


import android.net.Uri;
import android.util.Log;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;


/**
 * Created by pli on 2016/5/25.
 */
public class IoTReasoner {

    private String ontology = "ontology.owl";
    private Model schema = null; //ontology, tbox --pli
    private String graphURI = "http://localhost/SensorSchema/ontology#";
    private String prefix = "obs";
    private Reasoner ruleReasoner = null;
    private List rules = new ArrayList<Object>();
    private String rulesFile = "src/main/res/raw/rules_jena.txt";
    private String dataFormat = "RDF/XML";
    public Model dataModel = null;
    private InfModel rulesmodel = null;
    private Model deductionsModel;


    public IoTReasoner() {

        schema = ModelFactory.createDefaultModel();

        // Use the FileManager to find the input file
        //InputStream in = FileManager.get().open(ontology);
        InputStream in = MainActivity.instance.getResources().openRawResource(R.raw.ontology);

        if (in == null)
            throw new IllegalArgumentException("File: "+ontology+" not found");
        // Read the RDF/XML file
        schema.read(in, null);

        PrintUtil.registerPrefix(prefix, graphURI);

        this.initializeGenericReasoner();

    }
    public void initializeGenericReasoner(){

        ruleReasoner = new GenericRuleReasoner(getRules());
        ruleReasoner.bindSchema(schema); //bindSchema(Model tbox), This is most commonly used to attach an ontology (a set of tbox axioms in description logics jargon) to a reasoner.
    }
    public List<Rule> getRules(){
        List<Rule> rules = new ArrayList<Rule>();
        if(this.rules.isEmpty() && this.rulesFile != null) {
            //rules = Rule.rulesFromURL(this.rulesFile);
            //rules = Rule.rulesFromURL(Uri.parse("android.resource://com.mscthesis.pli.iotgateway/" + R.raw.rules_jena).toString());

            rules = Rule.parseRules(
                    convertStreamToString(MainActivity.instance.getResources().openRawResource(R.raw.rules_jena)));

            this.rules = rules;
        }



        return this.rules;
    }
    public void setDataFormat(String dataFormat) {
        this.dataFormat = dataFormat;
    }
    public void setDataModel(Model dataModel) {
        this.dataModel = dataModel;
    }
//    public void setDataModel(String rdfData) {
//        Model rdfModel = ModelFactory.createDefaultModel();
//
//
//        rdfModel.read(rdfData, null, "RDF/XML");
//
//        this.setDataModel(rdfModel);
//    }

    public Model inferModel(boolean store){

        //String[] classes = {"RightTurn", "LeftTurn", "UTurn", "Jam", "HighAvgSpeed", "LongStop", "HighAcceleration", "HighDeAcceleration", "VeryLongStop"};
        String[] classes = {"RightTurn", "LeftTurn", "UTurn",
                "JamSpeed1","JamSpeed2","JamSpeed3","JamSpeed4","Jam",
                "Stop1","Stop2","Stop3","Stop4",
                "LongStop",
                "VeryLongStop",
                "LowSpeed",
                "HighAceleration","HighDeacceleration"
        };


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
        //interestingEvents.stream().forEach((String classType) -> {
        for( String classType : interestingEvents){
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
                //System.out.println( "_______________________found new Type: "+classType );
                Log.d("IoTReasoner", "_______________________found new Type: "+classType);
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
                    //proplist.stream().forEach((String prop) -> {
                    for(String prop: proplist){
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
                    } //TODO: try adding one statement at time
                    //updateSesameRepository(ioTReasoner.getInferredModel());
                }
            }
        }

//        if(store == true && !inferencedInstancesModel.isEmpty())//save to the sesame database --pli
//            this.updateSesameRepository(inferencedInstancesModel);

        //tempModel.write(System.out);
        //this.ontologyModel = inferencedInstancesModel;

        return inferencedInstancesModel;

    }
    public InfModel inferFromRules() {

        (new Date()).getTime();

        //debug
        //System.out.println( "ruleReasoner:"+ruleReasoner.toString() );
        //dataModel.write(System.out);
        //System.out.println( );

        this.rulesmodel = ModelFactory.createInfModel(ruleReasoner, dataModel);// ruleReasoner contain rule, dataModel contain RDF data, this part do the reasoning!  --pli



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
    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
