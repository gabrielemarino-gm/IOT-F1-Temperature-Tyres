package it.unipi.iot;

import it.unipi.iot.coap.TyreActuatorCoAP;
import it.unipi.iot.dao.TemperatureDAO;
import it.unipi.iot.dao.exception.DAOException;
import it.unipi.iot.model.Actuator;
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
    private static String COMMANDS = "help -> Show All Commands\n" +
                                     "quit -> Close Controller\n" +
                                     "publish <target> <message> -> Publish a message for the target Topic\n" +
                                     "command <target> <resource> <command> -> Send a CoAP request to target\n" +
                                     "getTemp -> Get last reported temperature for all sensors\n" +
                                     "getStatus -> Get status of all TyreActuator\n";

    public static void main( String[] args )
    {
        Boolean exit = false;
        String c;
        String[] tokens;
        Scanner input = new Scanner(System.in);
//       Start MQTT service
        try
        {
            TyrewarmerMQTT.Subscriber subscriber = new TyrewarmerMQTT.Subscriber(BROKERIP, SUBCLIENTID, SUBTOPIC);
        }
        catch (MqttException e)
        {
            e.printStackTrace();
        }

//      Start CoAP service
        TyreActuatorCoAP.startServer();


//       Input loop
        System.out.println(COMMANDS);
        while(!exit)
        {
            c = input.nextLine();
            tokens = c.split(" ");
            System.out.println(("DBG:       tokens:" + tokens.toString()));
            
            if(tokens == null | tokens.length < 1)
            {
                System.out.println("Input error");
                continue;
            }

            // HELP
            if (tokens[0].equals("help"))
            {
                System.out.println(COMMANDS);
            }

            // QUIT
            else if (tokens[0].equals("quit"))
            {
                System.out.println("Quitting");
                TyreActuatorCoAP.kill();
                TemperatureDAO.closePool();
                System.exit(0);
            }

            // PUBLISH SOMETHING
            else if (tokens[0].equals("publish"))
            {
                try
                {
                    TyrewarmerMQTT.Publisher.Publish(BROKERIP, PUBCLIENTID, PUBTOPIC, "PUBTOPIC");
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

            // SEND COAP REQUEST
            else if (tokens[0].equals("command"))
            {
//              Manda una richiesta CoAP a uno specifico attuatore
                if(tokens.length < 3)
                {
                    System.out.println("Command error");
                }

                Actuator act = TyreActuatorCoAP.getTyre(Integer.parseInt(tokens[1]));
                if(act == null)
                {
                    System.out.println("There is no actuator for given tyre");
                }
                else
                {
                    System.out.println("Sending command");
                    TyreActuatorCoAP.sendCommand(act.getAddr(), tokens[2], tokens[3]);
                }
            }

            // GET LAST REGISTERED TEMP
            else if (tokens[0].equals("getTemp"))
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

            // GET TYREWARMER STATUS
            else if (tokens[0].equals("getStatus"))
            {
                for(Actuator a : TyreActuatorCoAP.getActuators())
                {
                    String ret = TyreActuatorCoAP.getStatRequest(a.getAddr(), a.getResource());
                    // TODO Sistemare le due risorse
                    System.out.println(String.format("TyreActuator [%d] -> %s", a.getTyre_position(), ret));
                }
            }

            // UNKNOWN COMMAND
            else
            {
                System.out.println("COMMAND NOT SUPPORTED. TRY THE FOLLOWIG:");
                System.out.println(COMMANDS);
            }
            System.out.println("-----------------------------");
        }
    }
}
