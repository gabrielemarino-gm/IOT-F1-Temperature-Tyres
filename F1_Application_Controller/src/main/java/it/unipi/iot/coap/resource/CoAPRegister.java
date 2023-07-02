package it.unipi.iot.coap.resource;

//  Risorsa del Server CoAP per registrare gli ATTUATORI

import it.unipi.iot.coap.TyreTrackCoAP;
import it.unipi.iot.coap.TyrewarmerCoAP;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class CoAPRegister extends CoapResource
{
    public CoAPRegister(String name)
    {
        super(name);
        setObservable(false);
    }

//    La seguente funzione è utilizzata dagli Attuatori per verificare se sono
//    ancora connessi al controller

    public void handleGET(CoapExchange exchange)
    {

        Response response = new Response(CoAP.ResponseCode.CONTENT);
        response.setPayload("PING");
        exchange.respond(response);
    }

//    La seguente funzione è utilizzata dagli attuatori per registrarsi (IP e ruota servita)

    public void handlePOST(CoapExchange exchange)
    {
//      Quando arriva una richiesta di registrazione di un ATTUATORE CoAP

        String payload = exchange.getRequestText();
        
        String[] fields = payload.split("&");

        String command = fields[0].split("=")[1];
        String val1 = fields[1].split("=")[1];

//      Un ATTUATORE CoAP si sta registrando: REG1 = Tyre Warmer, REG2 = Tyre Track
        if(command.equals("REG1"))
        {
            Response response = new Response(CoAP.ResponseCode.CONTENT);
            if(TyrewarmerCoAP.registerActuator(Integer.parseInt(val1), String.format("coap://[%s]", exchange.getSourceAddress().getHostName())))
            {
                response.setPayload("OK");
            }
            else
            {
                response.setPayload("ERROR");
            }
            exchange.respond(response);
        }
        else if(command.equals("REG2"))
        {
            Response response = new Response(CoAP.ResponseCode.CONTENT);
            if(TyreTrackCoAP.registerActuator(Integer.parseInt(val1), String.format("coap://[%s]", exchange.getSourceAddress().getHostName())))
            {
                response.setPayload("OK");
            }
            else
            {
                response.setPayload("ERROR");
            }
            exchange.respond(response);
        }
    }
}
