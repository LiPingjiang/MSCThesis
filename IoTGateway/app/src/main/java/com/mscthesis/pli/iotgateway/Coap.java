package com.mscthesis.pli.iotgateway;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.server.resources.Resource;

/**
 * Created by pli on 2016/5/22.
 */
public class Coap extends CoapServer {



    private Resource iotResource;

    public Coap(Resource iotResource) {
        super();
        this.iotResource = iotResource;
        add(iotResource);

    }

    @Override
    protected Resource createRoot() {
        return new CoapResource("") {
            @Override
            public Resource getChild(String name) {
                return iotResource;
            }
        };
    }




}