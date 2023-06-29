package it.unipi.iot.coap;

//  Questo funziona da Server CoAP
//  Serve per fare in modo che gli ATTUATORI si registrino

import it.unipi.iot.coap.resource.CoAPRegister;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;

public class TyrewarmerCoAP extends CoapServer
{
//    Semplicemente avvia un server CoAP con la risorsa registrator
//    Per far registrare gli ATTUATORI
    private static TyrewarmerCoAP server = null;
     public static void startServer()
     {
         if(server == null)
         {
             server = new TyrewarmerCoAP();
         }
         server.add(new CoAPRegister("registrator"));
         server.start();
     }

     public static void kill()
     {
         server.stop();
         server.destroy();
     }

//    Funzione statica per connettersi a un CoAP Server e inviare una semplice richiesta CoAP
//    Farne piu di una se la natura della richiesta e' diversa (Get, Put, Post...)

    public static String getStatRequest(String TARGET){
        CoapClient client = new CoapClient(TARGET);
        String toRet;

        try
        {
            client.setTimeout(2000);
            CoapResponse response = client.get();
            toRet = response.getResponseText();
        }
        catch(NullPointerException ne)
        {
            toRet = "OFFLINE";
        }
        finally {
            client.delete();

        }
        return toRet;
    }
//      Altre funzioni statiche utili alla gestione degli attuatori CoAP
//      quali registrazione e cancellazione
}
