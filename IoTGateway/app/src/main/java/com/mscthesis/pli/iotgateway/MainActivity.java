package com.mscthesis.pli.iotgateway;


import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class MainActivity extends AppCompatActivity {

    static long transmissionTime;

    static long reasonTime;
    public TextView informationView;
    public TextView statusView;
    public TextView resultView;
    public TextView tv_reasoningTime;
    public TextView tv_transmissionTime;
    public Button resetButton;
    BroadcastReceiver receiver;
    //private CoapServer server;
    public static MainActivity instance;
    int requestNumber = 0;
    static int finishNumber = 0;
    boolean isExisting = false; //is another service exist, if exist , do not start new one as they listen to the same port
    static String httpURI = "tcp://ec2-52-58-177-76.eu-central-1.compute.amazonaws.com:1883";
    static Handler handler = new Handler();
    static String resultModel;
    static byte[] lockAdd = new byte[0];
    static byte[] lockPool = new byte[0];
    static Queue rdfArray;
    static int reasoningThreads;
    static int MaxThreads = 1;
    //socket
    private ServerSocket serverSocket;
    Thread serverThread = null;
    public static final int SERVERPORT = 6000;
    Handler updateConversationHandler;
    public static String rdfFormat= "RDF/XML";




    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {



        reasonTime = 0;
        transmissionTime = 0;
        rdfArray =  new LinkedList();
        reasoningThreads = 0;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isExisting = false;
        instance = this;
        requestNumber = 0;


        informationView = (TextView) findViewById(R.id.Infomation);
        statusView = (TextView) findViewById(R.id.tv_status);
        resultView = (TextView) findViewById(R.id.Result);
        tv_reasoningTime = (TextView) findViewById(R.id.tv_reasoningTime);
        tv_transmissionTime = (TextView) findViewById(R.id.tv_transmissionTime);

        resultView.setMovementMethod(new ScrollingMovementMethod());
        resetButton = (Button) findViewById(R.id.bt_reset);
        resetButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        reasonTime = 0;
                        requestNumber = 0;
                        transmissionTime = 0;
                        statusView.setText("Reset the gateway, waiting for new reasoning tasks.");
                        resultView.setText("Reasoning result has been cleaned.");
                        changehttpURI( ((EditText) findViewById(R.id.et_hostip)).getText().toString() );
                        MaxThreads = Integer.parseInt( ((EditText) findViewById(R.id.et_threadNumber)).getText().toString() );
                    }
                }
        );

        informationView.setText("IP Address: " + getLocalIpAddress());

        //socket
        updateConversationHandler = new Handler();
        this.serverThread = new Thread(new ServerThread());
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

//    private void initServer() {
//        if (server == null) {
//            server = new Coap(new IoTResource());
////            for (Integer port : ports) {
////                server.addEndpoint(new CoapEndpoint(createDtlsConnector(port), NetworkConfig.getStandard()));
////            }
//        }
//    }

    public void changehttpURI( String address){
        httpURI="tcp://"+address+":1883";
    }

    public static void reasonTimeIncrease(long time) {
        reasonTime += time;
        Log.d("IoTTimer", "reasoning time: " + reasonTime);
        handler.post(new Runnable() {
            public void run() {
                instance.tv_reasoningTime.setText("Reasoning time: "+reasonTime);
            }
        });
    }

    public static void transmissionTimeIncrease(long time) {
        transmissionTime += time;
        Log.d("IoTTimer", "transmission time: " + transmissionTime + " time:"+time);
        handler.post(new Runnable() {
            public void run() {
                instance.tv_transmissionTime.setText("Transmission to server time: "+transmissionTime);
            }
        });
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
        //client.disconnect();
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
                ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
                ActivityManager activityManager = (ActivityManager) instance.getSystemService(ACTIVITY_SERVICE);
                activityManager.getMemoryInfo(mi);

                String text = "Receive " + instance.requestNumber + " request(s)." + " Finished: "+ finishNumber+ "\n" +
                        "**************************************************\n" +
                        "Gateway status: Used CPU " + instance.readCPUUsage() + " Available Memory: " + mi.availMem / 1024 / 1024 + "\n" +

                        "**************************************************\n";
                instance.statusView.setText(text);
            }
        });
    }
    public static void addReasoningData (String data) {
        //Log.d("IoTSocket","Add Data.");
        synchronized (lockAdd) {
            instance.requestNumber++;


//            updateStatusView();

            rdfArray.add(data);
        }

        reasoning();
    }
    public static void reasoning() {

        //Log.d("IoTReasoner", " public static void reasoning(String data){" );
        //if( (reasoningThreads < MaxThreads) && !rdfArray.isEmpty() )
        String data="";
        synchronized (lockPool){
            if( !rdfArray.isEmpty() && (reasoningThreads < MaxThreads) )
            {
                reasoningThreads++; // a new thread is running reasoning
                Log.d("IoTReasoner", "current thread: "+ reasoningThreads + " max thread: "+ reasoningThreads );
                data = (String) rdfArray.poll();
//                ReasonThread thread = new ReasonThread(data);
//                thread.start();

                Date startTime = new Date();
                Log.d("IoTReasoner", "Start reasoning.");
                IoTReasoner ioTReasoner;
                ioTReasoner = new IoTReasoner();
                ioTReasoner.setDataFormat("RDF/XML");

                Model model = ModelFactory.createDefaultModel();
                StringReader reader = new StringReader(data);
                model.read(reader, null, "RDF/XML");
                ioTReasoner.setDataModel(model);

                //                    Model infermodel = ioTReasoner.inferModel(false);// don't save in gateway
                InfModel infModel = ioTReasoner.inferAndGetResult();
                Log.d("IoTReasoner", "Finish reasoning.");
                Date reasoningFinishTime = new Date();
                MainActivity.reasonTimeIncrease(reasoningFinishTime.getTime() - startTime.getTime());

                StringWriter out = new StringWriter();
                infModel.write(out, "RDF/XML");
                //client.publish(out.toString());
                IoTMqttClient Mclient = new IoTMqttClient(httpURI, "IoTReasoning", "gateway" );
                Mclient.publish(out.toString());
                reasoningThreads--;
                finishNumber++;
                updateStatusView();
                if( (reasoningThreads < MaxThreads) && !rdfArray.isEmpty() ){
                    reasoning();
                }

            }
        }


//        final String rdfData = data;
//        //Use thread to reasoning
//
//        new Thread(new Runnable() {
//            public void run() {
//                Date startTime = new Date();
//                Log.d("IoTReasoner", "Start reasoning.");
//                IoTReasoner ioTReasoner;
//                ioTReasoner = new IoTReasoner();
//                ioTReasoner.setDataFormat("RDF/XML");
//
//                Model model = ModelFactory.createDefaultModel();
//                StringReader reader = new StringReader(rdfData);
//                model.read(reader, null, "RDF/XML");
//                ioTReasoner.setDataModel(model);
//
//        //                    Model infermodel = ioTReasoner.inferModel(false);// don't save in gateway
//                InfModel infModel = ioTReasoner.inferAndGetResult();
//                Log.d("IoTReasoner", "Finish reasoning.");
//                Date reasoningFinishTime = new Date();
//                MainActivity.reasonTimeIncrease(reasoningFinishTime.getTime() - startTime.getTime());
//                //send to cloud
//                //MQTTclient_pub client = new MQTTclient_pub( httpURI,"IoTReasoning",getLocalIpAddress() );
//                //client.publish(rdfData);//change to model
//                //merge the original rdf model and infered model together
//
//                //update UI
//        //                            resultModel = infermodel.toString();
//                //StringWriter inferOut = new StringWriter();
//                //infModel.write(inferOut, "TURTLE");
//                        /*resultModel = inferOut.toString();
//
//                        handler.post(new Runnable() {
//                            public void run() {
//                                instance.resultView.setText(
//                                        instance.resultView.getText()+
//                                                "\n****************************************\n"
//                                                +resultModel);
//                            }
//                        });*/
//
//        //                    final Model union = ModelFactory.createUnion(model, infermodel);
//                StringWriter out = new StringWriter();
//                infModel.write(out, "RDF/XML");
//                //client.publish(out.toString());
//                IoTMqttClient Mclient = new IoTMqttClient(httpURI, "IoTReasoning", "gateway");
//                Mclient.publish(out.toString());
//                reasoningThreads--;
//                finishNumber++;
//                updateStatusView();
//                if( (reasoningThreads < MaxThreads) && !rdfArray.isEmpty() ){
//                    reasoning();
//                }
//            }
//        }).start();
    }



    static public class ReasonThread extends Thread
    {
        private String rdfData;
        public ReasonThread(String rdf)
        {
            this.rdfData = rdf;
        }
        public void run()
        {
            Date startTime = new Date();
            Log.d("IoTReasoner", "Start reasoning.");
            IoTReasoner ioTReasoner;
            ioTReasoner = new IoTReasoner();
            ioTReasoner.setDataFormat("RDF/XML");

            Model model = ModelFactory.createDefaultModel();
            StringReader reader = new StringReader(rdfData);
            model.read(reader, null, "RDF/XML");
            ioTReasoner.setDataModel(model);

            //                    Model infermodel = ioTReasoner.inferModel(false);// don't save in gateway
            InfModel infModel = ioTReasoner.inferAndGetResult();
            Log.d("IoTReasoner", "Finish reasoning.");
            Date reasoningFinishTime = new Date();
            MainActivity.reasonTimeIncrease(reasoningFinishTime.getTime() - startTime.getTime());
            //send to cloud
            //MQTTclient_pub client = new MQTTclient_pub( httpURI,"IoTReasoning",getLocalIpAddress() );
            //client.publish(rdfData);//change to model
            //merge the original rdf model and infered model together

            //update UI
            //                            resultModel = infermodel.toString();
            //StringWriter inferOut = new StringWriter();
            //infModel.write(inferOut, "TURTLE");
                        /*resultModel = inferOut.toString();

                        handler.post(new Runnable() {
                            public void run() {
                                instance.resultView.setText(
                                        instance.resultView.getText()+
                                                "\n****************************************\n"
                                                +resultModel);
                            }
                        });*/

            //                    final Model union = ModelFactory.createUnion(model, infermodel);
            StringWriter out = new StringWriter();
            infModel.write(out, "RDF/XML");
            //client.publish(out.toString());
            IoTMqttClient Mclient = new IoTMqttClient(httpURI, "IoTReasoning", "gateway");
            Mclient.publish(out.toString());
            reasoningThreads--;
            finishNumber++;
            updateStatusView();
            if( (reasoningThreads < MaxThreads) && !rdfArray.isEmpty() ){
                reasoning();
            }
        }
    }

    private float readCPUUsage() {
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();

            String[] toks = load.split(" +");  // Split on one or more spaces

            long idle1 = Long.parseLong(toks[4]);
            long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            try {
                Thread.sleep(360);
            } catch (Exception e) {
            }

            reader.seek(0);
            load = reader.readLine();
            reader.close();

            toks = load.split(" +");

            long idle2 = Long.parseLong(toks[4]);
            long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            return (float) (cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    class ServerThread implements Runnable {

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

//                PrintWriter out = new PrintWriter(new BufferedWriter(
//                        new OutputStreamWriter(clientSocket.getOutputStream())),
//                        true);
//                out.println("accept.");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {

            while (!Thread.currentThread().isInterrupted()) {

                try {

                    String read = input.readLine();

                    Log.d("IoTSocket","Receive from socket "+ read );

                    //MainActivity.reasoning(read);
                    if(read != null)
                        MainActivity.addReasoningData(read);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}