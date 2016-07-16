package com.mscthesis.pli.iotgateway;


import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.mscthesis.pli.iotgateway.EN.ENPaser;

import org.apache.commons.io.IOUtils;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;


public class MainActivity extends AppCompatActivity {

    static long transmissionTime;


    public TextView informationView;
    public TextView statusView;
    public TextView tv_reasoningTime;
    public TextView tv_transmissionTime;
    public Button resetButton;
    public Button rdfReasoning;
    public Button jsonReasoning;
    public Button n3Reasoning;
    public Button enReasoning;
    public Button enSchemaReasoning;
    public Button sendToMqtt;
    BroadcastReceiver receiver;
    //private CoapServer server;
    public static MainActivity instance;
    int requestNumber = 0;
    static int finishNumber = 0;
    static long reasonTime;
    static long totalSendTime;
    boolean isExisting = false; //is another service exist, if exist , do not start new one as they listen to the same port
    static String httpURI = "tcp://ec2-52-58-177-76.eu-central-1.compute.amazonaws.com:1883";
    static Handler handler = new Handler();
    static byte[] lockAdd = new byte[0];
    static Queue rdfArray;
    static Queue sendArray;


    static int MaxThreads = 1;
    //socket
    private ServerSocket serverSocket;
    Thread serverThread = null;
    public static final int SERVERPORT = 6000;
    //Handler updateConversationHandler;




    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {



        totalSendTime = 0;
        reasonTime = 0;
        transmissionTime = 0;
        rdfArray =  new LinkedList();
        sendArray  =  new LinkedList();


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isExisting = false;
        instance = this;
        requestNumber = 0;


        informationView = (TextView) findViewById(R.id.Infomation);
        statusView = (TextView) findViewById(R.id.tv_status);
        tv_reasoningTime = (TextView) findViewById(R.id.tv_reasoningTime);
        tv_transmissionTime = (TextView) findViewById(R.id.tv_transmissionTime);

        rdfReasoning = (Button) findViewById(R.id.bt_rdfReasoning);
        jsonReasoning = (Button) findViewById(R.id.bt_jsonReasoning);
        n3Reasoning = (Button) findViewById(R.id.bt_n3Reasoning);
        enReasoning = (Button) findViewById(R.id.bt_enReasoning);
        enSchemaReasoning = (Button) findViewById(R.id.bt_enSchemaReasoning);
        sendToMqtt = (Button) findViewById(R.id.bt_send);

        rdfReasoning.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        ReasonThread thread = new ReasonThread("RDF/XML");
                        thread.start();
                    }
                }
        );
        jsonReasoning.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        ReasonThread thread = new ReasonThread("JSON-LD");
                        thread.start();
                    }
                }
        );
        n3Reasoning.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        ReasonThread thread = new ReasonThread("N-TRIPLE");
                        thread.start();
                    }
                }
        );
        enReasoning.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        ReasonThread thread = new ReasonThread("EN");
                        thread.start();
                    }
                }
        );

        enSchemaReasoning.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        ReasonThread thread = new ReasonThread("EN_SCHEMA");
                        thread.start();
                    }
                }
        );
        sendToMqtt.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {

                        Date startTime = new Date();
                        while ( !sendArray.isEmpty()){
                            String data = (String) sendArray.poll();

                            IoTMqttClient ioTMqttClient = new IoTMqttClient(httpURI,"IoTReasoning","gateway");
                            ioTMqttClient.publish(data);
                        }
                        totalSendTime = new Date().getTime() -startTime.getTime();
                        handler.post(new Runnable() {
                            public void run() {
                                instance.tv_reasoningTime.setText("Reasoning time: "+reasonTime + " ,total send Time: "+ totalSendTime);
                            }
                        });
                    }
                }
        );


        resetButton = (Button) findViewById(R.id.bt_reset);
        resetButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        reasonTime = 0;
                        finishNumber = 0;
                        requestNumber = 0;
                        transmissionTime = 0;
                        rdfArray =  new LinkedList();
                        sendArray  =  new LinkedList();
                        statusView.setText("Reset the gateway, waiting for new reasoning tasks.");
                        changehttpURI( ((EditText) findViewById(R.id.et_hostip)).getText().toString() );
                    }
                }
        );

        informationView.setText("IP Address: " + getLocalIpAddress());

        //socket
        //updateConversationHandler = new Handler();
        this.serverThread = new Thread(new SocketServerThread());
        this.serverThread.start();


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(IoTResource.BROADCAST_RECEIVE)
        );

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.mscthesis.pli.iotgateway/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(IoTResource.BROADCAST_RECEIVE)
        );
    }

    public void changehttpURI( String address){
        httpURI="tcp://"+address+":1883";
    }

    public static void reasonTimeIncrease(long time) {
        reasonTime += time;
        Log.d("IoTTimer", "reasoning time: " + reasonTime);
        handler.post(new Runnable() {
            public void run() {
                instance.tv_reasoningTime.setText("Reasoning time: "+reasonTime + " ,total send Time: "+ totalSendTime);
                updateStatusView();
            }
        });
    }

    public static void transmissionTimeIncrease(long time) {
        transmissionTime += time;
        //Log.d("IoTTimer", "transmission time: " + transmissionTime);
        //handler.post(new Runnable() {
        //    public void run() {
        //        instance.tv_transmissionTime.setText("Transmission to server time: "+transmissionTime);
        //    }
        //});
    }


    @Override
    protected void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.mscthesis.pli.iotgateway/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);

        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        //server.stop();
        //isExisting =false;
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
    }


    public String getLocalIpAddress() {
        try {

            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {

                NetworkInterface intf = en.nextElement();

                for (Enumeration<InetAddress> ipAddr = intf.getInetAddresses(); ipAddr.hasMoreElements(); ) {

                    InetAddress inetAddress = ipAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String ip = inetAddress.getHostAddress();
                        if (ip.charAt(3) == '.' || ip.charAt(2) == '.')
                            return ip;

                    }

                }

            }

        } catch (Exception ex) {

        }

        return "";

    }

    public static void updateStatusView(){
        handler.post(new Runnable() {
            public void run() {
                String text = "Receive " + instance.requestNumber + " request(s)." + " Finished: "+ finishNumber+ "\n";
                instance.statusView.setText(text);
            }
        });
    }
    public static void addReasoningData (String data) {
        //Log.d("IoTSocket","Add Data.");
        synchronized (lockAdd) {
            instance.requestNumber++;
            updateStatusView();
            rdfArray.add(data);
        }
    }


    static public class ReasonThread extends Thread
    {
        private String dataFormat;
        public ReasonThread(String dataFormat)
        {
            this.dataFormat = dataFormat;
        }
        public void run()
        {
            IoTReasoner ioTReasoner  = null;

            if(dataFormat.equals("EN_SCHEMA")){
                //fetch schema from cloud server
                String ontoInfo="";
                String enSchema="";
                // php connection
//                ontoInfo = "@prefix owl: <http://www.w3.org/2002/07/owl#> .\n" +
//                        "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" +
//                        "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
//                        "\n" +
//                        "<http://localhost/SensorSchema/ontology> a owl:Ontology .";
//                enSchema = "<en:Entity <obs:HighAcceleration>\n" +
//                        "\ta owl:Class\n" +
//                        "\trdfs:subClassOf <obs:Event>\n" +
//                        "\trdfs:comment \"\"^^xsd:string\n" +
//                        ">\n" +
//                        "<en:Entity <obs:HighDeacceleration>\n" +
//                        "\ta owl:Class\n" +
//                        "\trdfs:subClassOf <obs:Event>\n" +
//                        ">\n" +
//                        "<en:Entity <obs:Event>\n" +
//                        "\ta owl:Class\n" +
//                        ">";//ontology

                try {
                    ontoInfo= getOntology("http://vm0104.virtues.fi/testing/httprequest/ontology.php?ONTOTYPE=INFO");
                    enSchema= getOntology("http://vm0104.virtues.fi/testing/httprequest/ontology.php?ONTOTYPE=ONTOLOGY");

                } catch (Exception e) {
                    e.printStackTrace();
                }
//                Log.d("HTTPREQUEST","FINISHI" );
                //convert enSchema to turtle

                ENPaser parser = new ENPaser();
                parser.readENString(enSchema);
                //Log.d("HTTPREQUEST","turtle: " + parser.toTurtle("") );
                ioTReasoner = new IoTReasoner(parser.toTurtle(ontoInfo));
                ioTReasoner.setDataFormat("TURTLE");

            }else{
                ioTReasoner = new IoTReasoner();
                ioTReasoner.setDataFormat(dataFormat);

            }



            while( !rdfArray.isEmpty())
            {

                String data = (String) rdfArray.poll();
                Model model = getModelFromString(data,dataFormat);
                Model localModel = ModelFactory.createDefaultModel();
                Model sendModel = ModelFactory.createDefaultModel();
                //Model localModel = getModelFromString(data,dataFormat);
                //Model sendModel = getModelFromString(data,dataFormat);

                StmtIterator  stas = model.listStatements();
                while(stas.hasNext()){

                    Statement sta = stas.next();

                    if( sta.getObject().isLiteral() ){
                        if( sta.getPredicate().getURI().equals( "http://localhost/SensorSchema/ontology#hasID" ) ){
                            sendModel.add(sta);
                        }else if( sta.getPredicate().getURI().equals( "http://localhost/SensorSchema/ontology#hasArea" ) ) {
                            sendModel.add(sta);
                        }else if( sta.getPredicate().getURI().equals( "http://localhost/SensorSchema/ontology#hasLatitude" ) ){
                            sendModel.add(sta);
                        }else if( sta.getPredicate().getURI().equals( "http://localhost/SensorSchema/ontology#hasLongitude" ) ){
                            sendModel.add(sta);
                        }else if( sta.getPredicate().getURI().equals( "http://localhost/SensorSchema/ontology#hasVelocity" ) ){
                            sendModel.add(sta);
                        }else if( sta.getPredicate().getURI().equals( "http://localhost/SensorSchema/ontology#hasDirection" ) ){
                            sendModel.add(sta);
                        }else if( sta.getPredicate().getURI().equals( "http://localhost/SensorSchema/ontology#hasSender" ) ){
                            sendModel.add(sta);
                        }else if( sta.getPredicate().getURI().equals( "http://localhost/SensorSchema/ontology#hasDate" ) ){
                            sendModel.add(sta);
                        }else if( sta.getPredicate().getURI().equals( "http://localhost/SensorSchema/ontology#hasDateTime" ) ){
                            //do nothing
                        }else if( sta.getPredicate().getURI().equals( "http://localhost/SensorSchema/ontology#hasDistance" ) ){
                            //do nothing
                        }else if( sta.getPredicate().getURI().equals( "http://localhost/SensorSchema/ontology#hasAcceleration" ) ){
                            localModel.add(sta);
                        }else{
                            Log.d("IoTReasoner","sta: "+sta.toString() );
                            localModel.add(sta);
                            sendModel.add(sta);
                        }

                    }else{
                        //Log.d("IoTReasoner","sta: "+sta.toString() );
                        localModel.add(sta);
                        sendModel.add(sta);
                    }
                }
                //.d("IoTReasoner", "localmodel"+localModel.toString());
                //Log.d("IoTReasoner", "sendModel"+sendModel.toString());
                /*
                StmtIterator  stas = localModel.listStatements();

                while(stas.hasNext()){
                    Statement sta = stas.next();
                    if( sta.getObject().isLiteral() ){
                        if( sta.getPredicate().getURI().toString().equals( "http://localhost/SensorSchema/ontology#hasID" ) ){
                            sta.remove();
                        }else if( sta.getPredicate().getURI().toString().equals( "http://localhost/SensorSchema/ontology#hasArea" ) ) {
                            sta.remove();
                        }else if( sta.getPredicate().getURI().toString().equals( "http://localhost/SensorSchema/ontology#hasLatitude" ) ){
                            sta.remove();
                        }else if( sta.getPredicate().getURI().toString().equals( "http://localhost/SensorSchema/ontology#hasLongitude" ) ){
                            sta.remove();
                        }else if( sta.getPredicate().getURI().toString().equals( "http://localhost/SensorSchema/ontology#hasVelocity" ) ){
                            sta.remove();
                        }else if( sta.getPredicate().getURI().toString().equals( "http://localhost/SensorSchema/ontology#hasDirection" ) ){
                            sta.remove();
                        }else if( sta.getPredicate().getURI().toString().equals( "http://localhost/SensorSchema/ontology#hasSender" ) ){
                            sta.remove();
                        }else if( sta.getPredicate().getURI().toString().equals( "http://localhost/SensorSchema/ontology#hasDate" ) ){
                            sta.remove();
                        }else if( sta.getPredicate().getURI().toString().equals( "http://localhost/SensorSchema/ontology#hasDateTime" ) ){
                            sta.remove();
                        }else if( sta.getPredicate().getURI().toString().equals( "http://localhost/SensorSchema/ontology#hasDistance" ) ){
                            sta.remove();
                        }

                    }
                }
                stas = sendModel.listStatements();
                while(stas.hasNext()){

                    Statement sta = stas.next();

                    if( sta.getObject().isLiteral() ){
                        if( sta.getPredicate().getURI().toString().equals( "http://localhost/SensorSchema/ontology#hasDistance" ) ){
                            sta.remove();
                        }

                    }
                }
                */

                Date startTime = new Date();

                ioTReasoner.setDataModel(localModel);

                //InfModel infModel = ioTReasoner.inferAndGetResult();
                //ioTReasoner.inferModel(false);
                ioTReasoner.inferAndGetResult();

                Date reasoningFinishTime = new Date();
                MainActivity.reasonTimeIncrease(reasoningFinishTime.getTime() - startTime.getTime());

                finishNumber++;

                Log.d("IoTReasoner", "Finish reasoning. No."+finishNumber);


                if(dataFormat == "EN"){
                    //resultArray.add( modelToEnString(infModel) );
                }else if(dataFormat == "EN_SCHEMA"){
                    //resultArray.add( modelToEnString(infModel) );
                    sendArray.add( data ); // send what it received
                }else{
                    StringWriter out = new StringWriter();
                    sendModel.write(out, dataFormat);
                    sendArray.add(out.toString());
                }

                //Log.d("IoTReasoner","out.print"+out.toString());


            }
        }

        public static String getOntology(String urlToRead) throws Exception {
            StringBuilder result = new StringBuilder();
            URL url = new URL(urlToRead);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line+ "\n");
            }
            rd.close();
            return result.toString();
        }

        public Model getModelFromString( String data, String format){
            Model model = ModelFactory.createDefaultModel();

            // model.read(reader, null, dataFormat);
            if(format.equals("RDF/XML"))
                try {
                    RDFDataMgr.read(model, IOUtils.toInputStream(data, "UTF-8"), Lang.RDFXML) ;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            else if(format.equals("JSON-LD"))
                try {
                    RDFDataMgr.read(model, IOUtils.toInputStream(data, "UTF-8"), Lang.JSONLD) ;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            else if(format.equals("N-TRIPLE"))
                try {
                    RDFDataMgr.read(model, IOUtils.toInputStream(data, "UTF-8"), Lang.N3) ;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            else if(format.equals("EN")) {

                model = enToDataModel(data);

            }else if(format.equals("EN_SCHEMA")) {

                model = enToDataModel(data);

            }
            return model;
        }

        private String modelToEnString(Model model) {

            Hashtable<RDFNode, Hashtable<String,String> > objectsTable
                    = new Hashtable<RDFNode, Hashtable<String,String> >();

//            String ID = null;
//            String hasArea = null;
//            String hasLatitude = null;
//            String hasLongitude = null;
//            String hasVelocity = null;
//            String hasDirection = null;
//            String hasSender = null;
//            String hasDistance = null;
//            String hasAcceleration = null;
//            String hasDateTime = null;
//            String hasDate = null;

            NodeIterator objs = model.listObjects();
            while(objs.hasNext()){
                RDFNode obj = objs.next();
                objectsTable.put(obj, new Hashtable<String,String>() );
            }

            StmtIterator stas = model.listStatements();



            while(stas.hasNext()){

                Statement sta = stas.next();

                if( sta.getObject().isLiteral() ) {

                    if (sta.getPredicate().getURI().equals("http://localhost/SensorSchema/ontology#hasID")) {
                        //ID = sta.getObject().asLiteral().getString();
                        objectsTable.get(sta.getObject()).put("ID",sta.getObject().asLiteral().getString());
                        //Log.d("EnReasoner", "ID" );

                    } else if (sta.getPredicate().getURI().equals("http://localhost/SensorSchema/ontology#hasArea")) {
                        //hasArea = sta.getObject().asLiteral().getString();
                        //Log.d("EnReasoner", "hasArea" );
                        objectsTable.get(sta.getObject()).put("hasArea",sta.getObject().asLiteral().getString());
                    }  else if (sta.getPredicate().getURI().equals("http://localhost/SensorSchema/ontology#hasLatitude")) {
                        //hasLatitude = sta.getObject().asLiteral().getString();
                        //Log.d("EnReasoner", "hasLatitude" );
                        objectsTable.get(sta.getObject()).put("hasLatitude",sta.getObject().asLiteral().getString());
                    } else if (sta.getPredicate().getURI().equals("http://localhost/SensorSchema/ontology#hasLongitude")) {
                        //hasLongitude = sta.getObject().asLiteral().getString();
                        //Log.d("EnReasoner", "hasLongitude" );
                        objectsTable.get(sta.getObject()).put("hasLongitude",sta.getObject().asLiteral().getString());
                    } else if (sta.getPredicate().getURI().equals("http://localhost/SensorSchema/ontology#hasVelocity")) {
                        //hasVelocity = sta.getObject().asLiteral().getString();
                        //Log.d("EnReasoner", "hasVelocity" );
                        objectsTable.get(sta.getObject()).put("hasVelocity",sta.getObject().asLiteral().getString());
                    } else if (sta.getPredicate().getURI().equals("http://localhost/SensorSchema/ontology#hasDirection")) {
                        //hasDirection = sta.getObject().asLiteral().getString();
                        //Log.d("EnReasoner", "hasDirection" );
                        objectsTable.get(sta.getObject()).put("hasDirection",sta.getObject().asLiteral().getString());
                    } else if (sta.getPredicate().getURI().equals("http://localhost/SensorSchema/ontology#hasSender")) {
                        //hasSender = sta.getObject().asLiteral().getString();
                        //Log.d("EnReasoner", "hasSender" );
                        objectsTable.get(sta.getObject()).put("hasSender",sta.getObject().asLiteral().getString());
                    } else if (sta.getPredicate().getURI().equals("http://localhost/SensorSchema/ontology#hasDistance")) {
                        //hasDistance = sta.getObject().asLiteral().getString();
                        //Log.d("EnReasoner", "hasDistance" );
                        objectsTable.get(sta.getObject()).put("hasDistance",sta.getObject().asLiteral().getString());
                    } else if (sta.getPredicate().getURI().equals("http://localhost/SensorSchema/ontology#hasAcceleration")) {
//                        hasAcceleration = sta.getObject().asLiteral().getString();
//                        Log.d("EnReasoner", "hasAcceleration" );
                        objectsTable.get(sta.getObject()).put("hasAcceleration",sta.getObject().asLiteral().getString());
                    } else if (sta.getPredicate().getURI().equals("http://localhost/SensorSchema/ontology#hasDateTime")) {
//                        hasDateTime = sta.getObject().asLiteral().getString();
//                        Log.d("EnReasoner", "hasDateTime" );
                        objectsTable.get(sta.getObject()).put("hasDateTime",sta.getObject().asLiteral().getString());
                    } else if (sta.getPredicate().getURI().equals("http://localhost/SensorSchema/ontology#hasDate")) {
//                        hasDate = sta.getObject().asLiteral().getString();
//                        Log.d("EnReasoner", "hasDate" );
                        objectsTable.get(sta.getObject()).put("hasDate",sta.getObject().asLiteral().getString());
                    }


                }

            }
            String rdfData="";
            objs = model.listObjects();
            while(objs.hasNext()){
                RDFNode obj = objs.next();

                String enPacket = "[urn:uuid:7bcf39 ";
                enPacket +=
                        objectsTable.get(obj).get("ID")+" "+
                        objectsTable.get(obj).get("hasArea") +" "+
                        objectsTable.get(obj).get("hasLatitude") +" "+
                        objectsTable.get(obj).get("hasLongitude") + " "+
                        objectsTable.get(obj).get("hasVelocity") +" "+
                        objectsTable.get(obj).get("hasDirection") +" "+
                        objectsTable.get(obj).get("hasSender") +" "+
                        objectsTable.get(obj).get("hasDistance") +" " +
                        objectsTable.get(obj).get("hasAcceleration") +" "+
                        objectsTable.get(obj).get("hasDate") +" "+
                        objectsTable.get(obj).get("hasDateTime") ;

                enPacket += "]";

                rdfData+=enPacket;
            }

            System.out.println(rdfData);
            return rdfData;
        }
    }


    public static Model enToDataModel(String enData) {
        //long d = (new Date()).getTime();

        Model dataModel = ModelFactory.createDefaultModel();
        //Log.d("EnReasoner","run en reasoner 1");
        String[] enLines = enData.split("__");

        //Statement typeStatement = factory.createStatement(obsURI, RDF.TYPE, obsType);
        //myGraph.add(typeStatement);
        dataModel.setNsPrefix("obs", ontologyURI);

        for(int i = 0; i < enLines.length; i++){

            enLines[i] = enLines[i].substring(17,enLines[i].length()-1);
            String[] obs = enLines[i].split(" ");
            //Log.d("EnReasoner","OBS[0]:"+obs[0]);
            //Log.d("EnReasoner","OBS[1]:"+obs[1]);
            //Log.d("EnReasoner","OBS[2]:"+obs[2]);
            //Log.d("EnReasoner","OBS[3]:"+obs[3]);

            Resource obsInstance = dataModel.createResource();
            dataModel.add(obsInstance, RDF.type, dataModel.createResource(getTemplate()[0]));

            if(obs.length==11){
                dataModel.add(obsInstance, dataModel.createProperty(getTemplate()[1]), dataModel.createTypedLiteral(Integer.valueOf(obs[0])));
                dataModel.add(obsInstance, dataModel.createProperty(getTemplate()[2]), dataModel.createTypedLiteral(Integer.valueOf(obs[1])));
                dataModel.add(obsInstance, dataModel.createProperty(getTemplate()[3]), dataModel.createTypedLiteral(Double.valueOf(obs[2])));
                dataModel.add(obsInstance, dataModel.createProperty(getTemplate()[4]), dataModel.createTypedLiteral(Double.valueOf(obs[3])));
                dataModel.add(obsInstance, dataModel.createProperty(getTemplate()[5]), dataModel.createTypedLiteral(Double.valueOf(obs[4])));
                dataModel.add(obsInstance, dataModel.createProperty(getTemplate()[6]), dataModel.createTypedLiteral(Integer.valueOf(obs[5])));
                dataModel.add(obsInstance, dataModel.createProperty(getTemplate()[7]), dataModel.createTypedLiteral(Integer.valueOf(obs[6])));
                dataModel.add(obsInstance, dataModel.createProperty(getTemplate()[8]), dataModel.createTypedLiteral(Double.valueOf(obs[7])));
                dataModel.add(obsInstance, dataModel.createProperty(getTemplate()[9]), dataModel.createTypedLiteral(Double.valueOf(obs[8])));
                dataModel.add(obsInstance, dataModel.createProperty(getTemplate()[10]), dataModel.createTypedLiteral(Long.valueOf(obs[9])));
                dataModel.add(obsInstance, dataModel.createProperty(getTemplate()[11]), dataModel.createLiteral(obs[10]));
            }
        }

        //reasoningLatency = reasoningLatency + ((new Date()).getTime())-d;
        return dataModel;
    }
    public static String ontologyURI = "http://localhost/SensorSchema/ontology#";
    public static String[] getTemplate(){


        return new String[]{ontologyURI+"Observation", ontologyURI + "hasID", ontologyURI + "hasArea", ontologyURI +
                "hasLatitude", ontologyURI + "hasLongitude", ontologyURI + "hasVelocity", ontologyURI + "hasDirection", ontologyURI +
                "hasSender", ontologyURI + "hasDistance", ontologyURI + "hasAcceleration", ontologyURI + "hasDate", ontologyURI + "hasDateTime"};
    }

    class SocketServerThread implements Runnable {

        public void run() {
            Socket socket;
            try {
                serverSocket = new ServerSocket(SERVERPORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {

                try {

                    socket = serverSocket.accept();

                    CommunicationThread commThread = new CommunicationThread(socket);
                    new Thread(commThread).start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    class CommunicationThread implements Runnable {

        private Socket clientSocket;

        private BufferedReader input;

        public CommunicationThread(Socket clientSocket) {

            this.clientSocket = clientSocket;

            try {

                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {

            while (!Thread.currentThread().isInterrupted()) {

                try {

                    String read = input.readLine();

                    //Log.d("IoTSocket","Receive from socket "+ read );

                    //MainActivity.reasoning(read);
                    if(read != null)
                        MainActivity.addReasoningData(read);
                    else{
                        clientSocket.close();
                        break;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}