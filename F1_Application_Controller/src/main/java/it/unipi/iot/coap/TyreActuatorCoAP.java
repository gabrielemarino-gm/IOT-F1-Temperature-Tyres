package it.unipi.iot.coap;

//  Questo funziona da Server CoAP
//  Serve per fare in modo che gli ATTUATORI si registrino

import it.unipi.iot.coap.resource.CoAPRegister;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import java.util.ArrayList;
import java.util.Objects;

public class TyreActuatorCoAP extends CoapServer
{
//    Semplicemente avvia un server CoAP con la risorsa "registrator"
//    Per far registrare gli ATTUATORI
    private static TyreActuatorCoAP server = null;

     public static void startServer()
     {
         if(server == null)
         {
             server = new TyreActuatorCoAP();
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

    public static String sendCommand(String TARGET, String RESOURCE, String COMMAND)
    {
        try
        {
            CoapClient client = new CoapClient(String.format("%s/%s?command=%s", TARGET, RESOURCE, COMMAND));

            client.setTimeout(2000);
            CoapResponse response = client.put("", MediaTypeRegistry.TEXT_PLAIN);

            return response.getCode().toString();
        }
        catch (Exception e)
        {
            System.out.println("Target unreachable");
        }
        return "Error";
    }

    public static String getStatRequest(String TARGET, String RESOURCE)
    {
        try
        {
            CoapClient client = new CoapClient(TARGET + "/" + RESOURCE);
            String toRet;

            client.setTimeout(2000);
            CoapResponse response = client.get();
            toRet = response.getResponseText();

            return toRet;
        }
        catch (Exception e){
            return "ERROR";
        }
    }

}
