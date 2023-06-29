package it.unipi.iot.coap;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;

public class TyrewarmerCoAPClient
{
//    TODO
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
}
