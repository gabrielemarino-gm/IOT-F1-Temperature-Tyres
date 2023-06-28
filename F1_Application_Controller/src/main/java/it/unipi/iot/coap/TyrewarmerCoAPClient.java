package it.unipi.iot.coap;

import it.unipi.iot.coap.resource.CoAPRegister;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;

public class TyrewarmerCoAPClient
{
    public static void simpleRequest(String TARGET){
        CoapClient client = new CoapClient(TARGET);

        CoapResponse response = client.get();

        System.out.println(response.getResponseText());

        client.delete();
    }
}
