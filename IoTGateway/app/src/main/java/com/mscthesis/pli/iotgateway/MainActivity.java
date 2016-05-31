package com.mscthesis.pli.iotgateway;


import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import org.eclipse.californium.core.CoapServer;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Date;
import java.util.Enumeration;


public class MainActivity extends AppCompatActivity {

    static long transmissionTime;

    static long reasonTime;
    public TextView informationView;
    public TextView statusView;
    public TextView resultView;
    public Button resetButton;
    BroadcastReceiver receiver;
    private CoapServer server;
    public static MainActivity instance;
    int requestNumber=0;
    boolean isExisting=false; //is another service exist, if exist , do not start new one as they listen to the same port
    static String httpURI ="tcp://ec2-52-58-177-76.eu-central-1.compute.amazonaws.com:1883";
    static Handler handler = new Handler();
    static String resultModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        reasonTime = 0;
        transmissionTime =0;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isExisting =false;
        instance = this;
        requestNumber = 0;

        informationView = (TextView) findViewById(R.id.Infomation);
        statusView = (TextView) findViewById(R.id.tv_status);
        resultView = (TextView) findViewById(R.id.Result);
        resultView.setMovementMethod(new ScrollingMovementMethod());
        resetButton = (Button) findViewById(R.id.bt_reset);
        resetButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        reasonTime=0;
                        requestNumber=0;
                        transmissionTime =0;
                        resultView.setText("Waiting for new reasoning tasks.");
                    }
                }
        );

        informationView.setText("IP Address: " + getLocalIpAddress());


    }
    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(IoTResource.BROADCAST_RECEIVE)
        );

        initServer();
        if(!isExisting)
        {
            server.start();
            isExisting = true;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(IoTResource.BROADCAST_RECEIVE)
        );
    }

    private void initServer() {
        if (server == null) {
            server = new Coap(new IoTResource());
//            for (Integer port : ports) {
//                server.addEndpoint(new CoapEndpoint(createDtlsConnector(port), NetworkConfig.getStandard()));
//            }
        }
    }

    public static void reasonTimeIncrease( long time){
        reasonTime+=time;
        Log.d("IoTTimer","reasoning time: "+reasonTime);
    }

    public static void transmissionTimeIncrease( long time){
        transmissionTime+=time;
        Log.d("IoTTimer","transmission time: "+transmissionTime);
    }


    @Override
    protected void onStop() {
        super.onStop();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        //server.stop();
        //isExisting =false;
    }


    public String getLocalIpAddress() {
        try {

            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {

                NetworkInterface intf = en.nextElement();

                for (Enumeration<InetAddress> ipAddr = intf.getInetAddresses(); ipAddr.hasMoreElements();) {

                    InetAddress inetAddress = ipAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String ip=inetAddress.getHostAddress();
                        if(ip.charAt(3)=='.' || ip.charAt(2)=='.')
                            return ip;

                    }

                }

            }

        } catch (Exception ex) {

        }

        return "";

    }

    public static void reasoning(String data){

        //Log.d("IoTReasoner", " public static void reasoning(String data){" );
        final String rdfData=data;
        //instance.statusView.setText(instance.statusView.getText()+"\n"+instance.intent.getStringExtra(IoTResource.RDF_DATA));
        instance.requestNumber++;
        handler.post(new Runnable() {
            public void run() {
                ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
                ActivityManager activityManager = (ActivityManager) instance.getSystemService(ACTIVITY_SERVICE);
                activityManager.getMemoryInfo(mi);

                String text = "Receive " + instance.requestNumber + " request(s)." + "\n" +
                        "**************************************************\n" +
                        "Gateway status: Used CPU " + instance.readCPUUsage() + " Available Memory: " + mi.availMem / 1024 / 1024 + "\n" +

                        "**************************************************\n";
                instance.statusView.setText(text);
            }
        });



        //Use thread to reasoning


        new Thread(new Runnable() {
            public void run() {
                Date startTime = new java.util.Date();
                Log.d("IoTReasoner", "Start reasoning.");
                IoTReasoner ioTReasoner;
                ioTReasoner = new IoTReasoner();
                ioTReasoner.setDataFormat("RDF/XML");

                Model model = ModelFactory.createDefaultModel();
                StringReader reader = new StringReader(rdfData);
                model.read( reader,null,"RDF/XML" );
                ioTReasoner.setDataModel(model);
                Model infermodel = ioTReasoner.inferModel(false);// don't save in gateway
                Log.d("IoTReasoner", "Finish reasoning.");
                Date reasoningFinishTime = new java.util.Date();
                MainActivity.reasonTimeIncrease( reasoningFinishTime.getTime()-startTime.getTime() );
                //send to cloud
                //MQTTclient_pub client = new MQTTclient_pub( httpURI,"IoTReasoning",getLocalIpAddress() );
                //client.publish(rdfData);//change to model
                //merge the original rdf model and infered model together

                //update UI
//                            resultModel = infermodel.toString();
                StringWriter inferOut = new StringWriter();
                infermodel.write(inferOut, "TURTLE");
                resultModel = inferOut.toString();

                handler.post(new Runnable() {
                    public void run() {
                        instance.resultView.setText( instance.statusView.getText()+ resultModel);
                    }
                });

                final Model union = ModelFactory.createUnion(model, infermodel);
                StringWriter out = new StringWriter();
                union.write(out, "RDF/XML");
                //client.publish(out.toString());
                IoTMqttClient Mclient = new IoTMqttClient( httpURI,"IoTReasoning" , "gateway");
                Mclient.publish(out.toString());
            }
        }).start();
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
            } catch (Exception e) {}

            reader.seek(0);
            load = reader.readLine();
            reader.close();

            toks = load.split(" +");

            long idle2 = Long.parseLong(toks[4]);
            long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            return (float)(cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return 0;
    }
}