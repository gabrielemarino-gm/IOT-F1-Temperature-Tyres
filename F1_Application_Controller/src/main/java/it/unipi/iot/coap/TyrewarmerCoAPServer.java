package it.unipi.iot.coap;

//  Questo funziona da Client CoAP
//  Serve ad inviare comandi CoAP agli ATTUATORI
//  Ogni volta che serve (temperatura supera threshold)

import it.unipi.iot.coap.resource.CoAPRegister;
import org.eclipse.californium.core.CoapServer;

public class TyrewarmerCoAPServer extends CoapServer
{
    private static TyrewarmerCoAPServer server = null;
     public static void startServer()
     {
         if(server == null)
         {
             server = new TyrewarmerCoAPServer();
         }
         server.add(new CoAPRegister("Registrator"));
         server.start();
     }

     public static void kill()
     {
         server.stop();
         server.destroy();
     }
}
