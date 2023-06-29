package it.unipi.iot.coap.resource;

//  Risorsa del Server CoAP per registrare gli ATTUATORI

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class CoAPRegister extends CoapResource
{
    public CoAPRegister(String name){
        super(name);
        setObservable(false);
    }

    public void handlePOST(CoapExchange exchange)
    {
//        TODO
//        Quando arriva una richiesta di registrazione di un ATTUATORE CoAP

        String payload = exchange.getRequestText();
        String[] fields = payload.split("&");

        for(String s : fields){
            String[] parts = s.split("=");
            System.out.println(parts[0] + " -> "+ parts[1]);
        }


    }

}
