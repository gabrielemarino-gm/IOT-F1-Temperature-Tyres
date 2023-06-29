package it.unipi.iot;

import it.unipi.iot.coap.TyrewarmerCoAPClient;
import it.unipi.iot.coap.TyrewarmerCoAPServer;
import it.unipi.iot.dao.TemperatureDAO;
import it.unipi.iot.dao.exception.DAOException;
import it.unipi.iot.model.Temperature;
import it.unipi.iot.mqtt.TyrewarmerMQTT;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;
import java.util.Scanner;

public class Controller
{
    // Applicazione
    private static String BROKERIP = "tcp://[::1]:1883";
    private static String SUBCLIENTID = "Controller_Tyrewarmer_Sub";
    private static String SUBTOPIC = "TyrewarmerTemp";
    private static String PUBCLIENTID = "Controller_Tyrewarmer_Pub";
    private static String PUBTOPIC = "TyrewarmerConf";
    private static String COMMANDS = "quit -> Close Controller\n" +
                                     "publish <target> <message> -> Publish a message for the target Topic\n" +
                                     "command <target> <type> <args> -> Send a CoAP request to target\n" +
                                     "getTemp -> Get last reported temperature for all sensors\n" +
                                     "getStatus -> Get status of all Tyrewarmers\n";

    public static void main( String[] args )
    {
        Boolean exit = false;
        String c;
        String[] tokens;
        Scanner input = new Scanner(System.in);
//        Start MQTT service
        try
        {
            TyrewarmerMQTT.Subscriber subscriber = new TyrewarmerMQTT.Subscriber(BROKERIP, SUBCLIENTID, SUBTOPIC);
        }
        catch (MqttException e)
        {
            e.printStackTrace();
        }

//        Start CoAP service
        TyrewarmerCoAPServer.startServer();

//        Start DB service

//        Input loop
        System.out.println(COMMANDS);
        while(!exit)
        {
            c = input.nextLine();
            tokens = c.split(" ");
            if(tokens == null | tokens.length < 1)
            {
                System.out.println("Input error");
                continue;
            }

            if(tokens[0].equals("quit"))   //QUIT
            {
                System.out.println("Quitting");
                TyrewarmerCoAPServer.kill();
                System.exit(0);
            }
            else if(tokens[0].equals("publish"))   //PUBLISH SOMETHING
            {
                try
                {
                    TyrewarmerMQTT.Publisher.Publish(BROKERIP, PUBCLIENTID, PUBTOPIC, "PubMessageTest");
                }
                catch (InterruptedException ie)
                {
                    ie.printStackTrace();
                }
                catch (MqttException me)
                {
                    me.printStackTrace();
                }
            }
            else if(tokens[0].equals("command"))   //SEND COAP REQUEST
            {
//                Manda una richiesta CoAP ad uno specifico attuatore
            }
            else if(tokens[0].equals("getTemp"))     //GET LAST REGISTERED TEMP
            {
                try
                {
                    ArrayList<Integer> pass = new ArrayList<>();
                    ArrayList<Temperature> temps = TemperatureDAO.getLastTemperature("temperature_on_warmer");
                    for(Temperature t : temps){
                        pass.add(t.getTyrePosition());
                        System.out.println(String.format("Tyre [%d] -> %s", t.getTyrePosition(), t.getTemperatureValue()));
                    }
                    for(int i = 1; i<=4;i++)
                    {
                        if(!pass.contains(i))
                        {
                            System.out.println(String.format("Tyre [%d] -> %s", i, "OUTDATED"));
                        }
                    }
                }
                catch(DAOException de)
                {
                    de.printStackTrace();
                }
            }
            else if(tokens[0].equals("getStatus"))       //GET TYREWARMER STATUS
            {
                for(int i = 1; i <= 4; i++){

                    String ret = TyrewarmerCoAPClient.getStatRequest("coap://[fd00::20"+(i+1)+":"+(i+1)+":"+(i+1)+":"+(i+1)+"]/stat");
                    System.out.println("STATUS TYREWARMER[" + i + "] -> " + ret);
                }
            }
            else        //UNKNOWN COMMAND
            {
                System.out.println(COMMANDS);
            }
            System.out.println("-----------------------------");
        }
    }
}
