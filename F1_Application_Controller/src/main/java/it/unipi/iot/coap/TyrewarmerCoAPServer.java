package it.unipi.iot.coap;

//  Questo funziona da Server CoAP
//  Serve per fare in modo che gli ATTUATORI si registrino

import it.unipi.iot.coap.resource.CoAPRegister;
import org.eclipse.californium.core.CoapServer;

public class TyrewarmerCoAPServer extends CoapServer
{
//    Semplicemente avvia un server CoAP con la risorsa registrator
//    Per far registrare gli ATTUATORI
    private static TyrewarmerCoAPServer server = null;
     public static void startServer()
     {
         if(server == null)
         {
             server = new TyrewarmerCoAPServer();
         }
         server.add(new CoAPRegister("registrator"));
         server.start();
     }

     public static void kill()
     {
         server.stop();
         server.destroy();
     }
}
