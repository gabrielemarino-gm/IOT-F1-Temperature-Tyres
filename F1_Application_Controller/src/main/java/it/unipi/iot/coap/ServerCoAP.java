package it.unipi.iot.coap;

//  Questo funziona da Server CoAP
//  Gli ATTUATORI CoAP inviano richieste qui (per registrarsi)

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;

public class ServerCoAP extends CoapServer
{
    private static ServerCoAP server;
    private ServerCoAP()
    {
        server = new ServerCoAP();
        server.add(new CoapResource("Registrator"));
        server.start();
    }
}
