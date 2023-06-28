package it.unipi.iot.coap;

//  Questo funziona da Client CoAP
//  Serve ad inviare comandi CoAP agli ATTUATORI
//  Ogni volta che serve (temperatura supera threshold)

import org.eclipse.californium.core.CoapServer;

public class TyrewarmerCoAPServer extends CoapServer
{
    private static TyrewarmerCoAPServer server = null;
     public static TyrewarmerCoAPServer getServer()
     {
         if(server == null)
         {
             server = new TyrewarmerCoAPServer();
         }
         return server;
     }
}

}


