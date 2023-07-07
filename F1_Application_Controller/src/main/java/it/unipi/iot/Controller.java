package it.unipi.iot;

import it.unipi.iot.coap.TyreActuatorCoAP;
import it.unipi.iot.dao.OperationsDAO;
import it.unipi.iot.dao.exception.DAOException;
import it.unipi.iot.model.Actuator;
import it.unipi.iot.model.Temperature;
import it.unipi.iot.mqtt.TyreSensorMQTT;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;
import java.util.Scanner;

public class Controller
{
    // Applicazione
    private static String BROKERIP = "tcp://[::1]:1883";
    private static String SUBCLIENTID = "Controller_Sub";
    private static String SUBTOPIC_WARMER = "TyrewarmerTemp";
    private static String SUBTOPIC_TRACK = "TyreTemp";

    private static String PUBCLIENTID = "Controller_Tyrewarmer_Pub";
    private static String PUBTOPIC = "TyrewarmerConf";
    private static String COMMANDS = "help -> Show All Commands\n" +
                                     "quit -> Close Controller\n" +
                                     "publishWarmer <1/0> -> Activate/Deactivate Tyrewarmers temperature simulation\n" +
                                     "publishTrack <INT> -> Change the detection rate of the sensor\n" +
                                     "command <target> <resource> <command> -> Send a CoAP request to target\n" +
                                     "getTemp -> Get last reported temperature for all sensors\n" +
                                     "getStatus -> Get status of all TyreActuator\n";

    public static void main( String[] args )
    {
        Boolean exit = false;
        String c;
        String[] tokens;
        Scanner input = new Scanner(System.in);
        
//      Start MQTT service
        try
        {

            TyreSensorMQTT.Subscriber subscriber = new TyreSensorMQTT.Subscriber(BROKERIP, SUBCLIENTID, SUBTOPIC_TRACK, SUBTOPIC_WARMER);

        }
        catch (MqttException e)
        {
            e.printStackTrace();
        }

//      Start CoAP service
        TyreActuatorCoAP.startServer();


//      Input loop
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
                OperationsDAO.closePool();
                input.close();
                System.exit(0);
            }

            // PUBLISH SOMETHING
            else if (tokens[0].equals("publishTrack"))
            {
                if(tokens.length < 2)
                {
                    System.out.println("Command error");
                    break;
                }

                try
                {
                    TyreSensorMQTT.Publisher.Publish(BROKERIP, PUBCLIENTID, "SetThreshold", tokens[1]);
                }
                catch (InterruptedException | MqttException ie)
                {
                    ie.printStackTrace();
                }
            }

            else if (tokens[0].equals("publishWarmer"))
            {
                if(tokens.length < 2)
                {
                    System.out.println("Command error");
                    break;
                }

                try
                {
                    TyreSensorMQTT.Publisher.Publish(BROKERIP, PUBCLIENTID, "warmer_on", tokens[1]);
                }
                catch (InterruptedException | MqttException ie)
                {
                    ie.printStackTrace();
                }
            }

            // SEND COAP REQUEST
            else if (tokens[0].equals("command"))
            {

//              Manda una richiesta CoAP a uno specifico attuatore
                if(tokens.length < 3)
                {
                    System.out.println("Command error");
                    break;
                }

                Actuator act = OperationsDAO.getActuator(Integer.parseInt(tokens[1]), tokens[2]);

                if(act == null)
                {
                    System.out.println("There is no actuator for given tyre");
                }
                else
                {
                    System.out.println("Sending command");
                    TyreActuatorCoAP.sendCommand(act.getAddr(), act.getResource(), tokens[3]);
                }
            }

            // GET LAST REGISTERED TEMP
            else if (tokens[0].equals("getTemp"))
            {
                try
                {
                    ArrayList<Temperature> TyrewarmerTemps = OperationsDAO.getLastTemperature("temperature_on_warmer");
                    for(Temperature t : TyrewarmerTemps)
                    {
                        System.out.println(String.format("Tyrewarmer [%d] -> %s", t.getTyrePosition(), t.getTemperatureValue()));
                    }
                    if(TyrewarmerTemps.size() == 0)
                    {
                        System.out.println("No recently registrated tyrewarmer temperature");
                    }
                        System.out.println("~~");
                    ArrayList<Temperature> temps = OperationsDAO.getLastTemperature("temperature_on_track");
                    for(Temperature t : temps)
                    {
                        System.out.println(String.format("Tyre [%d] -> %s", t.getTyrePosition(), t.getTemperatureValue()));
                    }
                    if(temps.size() == 0)
                    {
                        System.out.println("No recently registrated tyre temperature");
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
                for(Actuator a : OperationsDAO.getActiveActuators())
                {
                    String ret = TyreActuatorCoAP.getStatRequest(a.getAddr(), a.getResource());
                    System.out.println(String.format("Actuator [%s - %d] -> %s", a.getResource(), a.getTyre_position(), ret));
                }
            }

            // UNKNOWN COMMAND
            else
            {
                System.out.println("COMMAND NOT SUPPORTED. TRY THE FOLLOWING:");
                System.out.println(COMMANDS);
            }
            System.out.println("-----------------------------");
        }
    }
}
