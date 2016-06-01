package com.mscthesis.pli.iotgateway;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;

/**
 * Created by pli on 2016/5/22.
 */
public class IoTResource extends CoapResource {


    public static String BROADCAST_RECEIVE = "BROADCAST_RECEIVE";
    public static String RDF_DATA = "RDF_DATA";

    LocalBroadcastManager broadcaster;

    public IoTResource() {
        super("IoTResource");
        broadcaster = LocalBroadcastManager.getInstance(MainActivity.instance);
    }

    @Override
    public void handlePUT(CoapExchange exchange) {
        Log.d("IoTReasoner", "[ADAPTER] <> received PUT message: \"" + exchange.getRequestText() + '\"');
        exchange.respond("success accept.");
        exchange.accept();
        //sendBroadcast(BROADCAST_RECEIVE, RDF_DATA, exchange.getRequestText());
        //Log.d("IoTReasoner","sned broadcast");
        //broadcaster.sendBroadcast(new Intent(BROADCAST_RECEIVE));
        //MainActivity.reasoning(exchange.getRequestText());
        MainActivity.addReasoningData(exchange.getRequestText());

    }

    @Override
    public void handleGET(CoapExchange exchange) {
        Log.d("IoTReasoner", "Receive get request.");

        exchange.accept();
        exchange.respond("Server is active.");
    }

    @Override
    public Resource getChild(String name) {
        return this;
    }

    public void sendBroadcast(String title, String name, String message) {
        Intent intent = new Intent(title);
        if(message != null)
            intent.putExtra(name, message);
        broadcaster.sendBroadcast(intent);
    }

}