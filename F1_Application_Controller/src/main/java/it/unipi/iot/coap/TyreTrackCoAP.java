package it.unipi.iot.coap;

import it.unipi.iot.coap.resource.CoAPRegister;
import it.unipi.iot.model.Actuator;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import java.util.ArrayList;

public class TyreTrackCoAP extends CoapServer
{
    private static TyreTrackCoAP server = null;

    private static ArrayList<Actuator> actuators = new ArrayList<>();

    public static void startServer()
    {
        if(server == null)
        {
            server = new TyreTrackCoAP();
        }

        server.add(new CoAPRegister("registratorTrack"));
        server.start();
    }

    public static void kill()
    {
        server.stop();
        server.destroy();
    }

//  Funzione statica per connettersi a un CoAP Server e inviare una semplice richiesta PUT - CoAP
//  Farne piu di una se la natura della richiesta è diversa (Get, Put, Post...)
    public static String sendCommand(String TARGET, String COMMAND)
    {
        CoapClient client = new CoapClient(String.format("%s/res_wheel_led?command=%s", TARGET, COMMAND));

        client.setTimeout(2000);
        CoapResponse response = client.put("", MediaTypeRegistry.TEXT_PLAIN);

        return response.getCode().toString();
    }


    public static boolean registerActuator(int pos, String addr)
    {
        Actuator toReg = new Actuator(pos,addr);

//      Controllo se è presente un attuatore con la stessa ruota attivo
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

    public static String getStatRequest(String TARGET)
    {
        CoapClient client = new CoapClient(TARGET + "/res_wheel_led");
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
