package it.unipi.iot;

import it.unipi.iot.coap.TyrewarmerCoAPClient;
import it.unipi.iot.coap.TyrewarmerCoAPServer;
import it.unipi.iot.mqtt.TyrewarmerMQTT;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.Scanner;

public class Controller
{
    private static String BROKER = "tcp://127.0.0.1:1883";
    private static String SUBCLIENTID = "Controller_Tyrewarmer_Sub";
    private static String SUBTOPIC = "TyrewarmerTemp";
    private static String PUBCLIENTID = "Controller_Tyrewarmer_Pub";
    private static String PUBTOPIC = "TyrewarmerConf";
    private static String RESOURCE = "Registrator";
    public static void main( String[] args )
    {
        Boolean exit = false;
        String c;
        Scanner input = new Scanner(System.in);
//        Start MQTT service
        try
        {
            TyrewarmerMQTT.Subscriber subscriber = new TyrewarmerMQTT.Subscriber(BROKER, SUBCLIENTID, SUBTOPIC);
        }
        catch (MqttException e)
        {
            e.printStackTrace();
        }

//        Start CoAP service
        TyrewarmerCoAPServer.startServer();

//        Start DB service

//        Input loop
        while(!exit){
            c = input.nextLine();
            if(c.equals("q"))   //QUIT
            {
                System.out.println("Quitting");
                TyrewarmerCoAPServer.kill();
                System.exit(0);
            }
            if(c.equals("p"))   //PUBLISH
            {
                try
                {
                    TyrewarmerMQTT.Publisher.Publish(BROKER, PUBCLIENTID, PUBTOPIC, "PubMessageTest");
                }
                catch (InterruptedException ie){
                    ie.printStackTrace();
                }
                catch (MqttException me)
                {
                    me.printStackTrace();
                }
            }
            if(c.equals("c"))   //COAP REQUEST
            {
                TyrewarmerCoAPClient.simpleRequest("coap://[fd00::202:2:2:2]/test");
            }
        }
    }
}
