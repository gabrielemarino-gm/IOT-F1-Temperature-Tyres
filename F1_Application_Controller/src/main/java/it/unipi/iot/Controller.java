package it.unipi.iot;

import it.unipi.iot.mqtt.TyrewarmerMQTT;
import org.eclipse.paho.client.mqttv3.MqttException;

public class Controller
{
    private static String BROKER = "tcp://127.0.0.1:1883";
    private static String CLIENTID = "Controller";
    private static String SUBTOPIC = "TyrewarmerTemp";
    public static void main( String[] args )
    {
        Boolean exit = false;
//        Start MQTT service
        try
        {
            TyrewarmerMQTT.Subscriber subscriber = new TyrewarmerMQTT.Subscriber(BROKER, CLIENTID + "_Tyrewarmer_Sub", SUBTOPIC);
        }
        catch (MqttException e)
        {
            e.printStackTrace();
        }

//        Start CoAP service


//        Start DB service

//        Input loop
        while(!exit){

        }
    }
}
