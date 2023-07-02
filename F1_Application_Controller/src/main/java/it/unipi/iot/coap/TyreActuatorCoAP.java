package it.unipi.iot.coap;

//  Questo funziona da Server CoAP
//  Serve per fare in modo che gli ATTUATORI si registrino

import it.unipi.iot.coap.resource.CoAPRegister;
import it.unipi.iot.model.Actuator;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import java.util.ArrayList;

public class TyreActuatorCoAP extends CoapServer
{
//    Semplicemente avvia un server CoAP con la risorsa "registrator"
//    Per far registrare gli ATTUATORI
    private static TyreActuatorCoAP server = null;
    private static ArrayList<Actuator> actuators = new ArrayList<>();

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
        CoapClient client = new CoapClient(String.format("%s/%s?command=%s", TARGET, RESOURCE, COMMAND));

        client.setTimeout(2000);
        CoapResponse response = client.put("", MediaTypeRegistry.TEXT_PLAIN);

        return response.getCode().toString();
    }

    public static String getStatRequest(String TARGET, String RESOURCE)
    {
        CoapClient client = new CoapClient(TARGET + "/" + RESOURCE);
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
            for(Actuator a : actuators)
            {
                if(a.getAddr().equals(TARGET))
                {
                    a.inactive();
                }
            }
        }
        finally
        {
            client.delete();
        }
        return toRet;
    }

//  Altre funzioni statiche utili alla gestione degli attuatori CoAP
//  quali registrazione e cancellazione
    public static boolean registerActuator(int pos, String addr, String res)
    {
         Actuator toReg = new Actuator(pos, addr, res);

//       Controllo se è presente un attuatore con la stessa ruota e attivo
         for(Actuator a : actuators)
         {
//          Se esiste ed è attivo ritorno falso altrimenti lo elimino e torno true
            if((a.getTyre_position() == pos))
            {
                if(a.isActive())
                {
                    return false;
                }
                else
                {
                    actuators.remove(a);
                    break;
                }
            }
         }

         actuators.add(toReg);
         return true;
    }

    public static ArrayList<Actuator> getActuators()
    {
        return actuators;
    }
    public static Actuator getTyre(int i)
    {
         for(Actuator a : actuators)
         {
             if(a.getTyre_position() == i) return a;
         }
         return null;
    }

}
