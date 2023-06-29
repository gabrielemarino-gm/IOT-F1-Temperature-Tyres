package it.unipi.iot.coap.resource;

//  Risorsa del Server CoAP per registrare gli ATTUATORI

import it.unipi.iot.coap.TyrewarmerCoAP;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
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

        String command = fields[0].split("=")[1];
        String val1 = fields[1].split("=")[1];
        String val2 = fields[2].split("=")[1];

//        Un ATTUATORE CoAP si sta registrando
        if(command.equals("REG")){
            if(TyrewarmerCoAP.registerActuator(Integer.parseInt(val1), val2))
            {
                exchange.respond(CoAP.ResponseCode.CREATED);
            }
            else
            {

                exchange.respond(CoAP.ResponseCode.NOT_IMPLEMENTED);
            }
        }

    }

}
