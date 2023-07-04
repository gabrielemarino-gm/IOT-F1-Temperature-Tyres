package it.unipi.iot.mqtt;

import it.unipi.iot.coap.TyreActuatorCoAP;
import it.unipi.iot.dao.TemperatureDAO;
import it.unipi.iot.dao.exception.DAOException;
import it.unipi.iot.enumActuatorStatus.OnTrackStatus;
import it.unipi.iot.model.Actuator;
import it.unipi.iot.model.Temperature;
import org.eclipse.paho.client.mqttv3.*;

import java.sql.Date;

public class TyreSensorMQTT
{
    private static String SUBTOPIC_WARMER = "TyrewarmerTemp";
    private static String SUBTOPIC_TRACK = "tyre_temp";
    public static class Subscriber implements MqttCallback
    {
        static MqttClient client = null;
        public Subscriber(String BROKER, String CLIENTID, String TOPIC) throws MqttException
        {
            if(client == null)
            {
                client = new MqttClient(BROKER, CLIENTID);
                client.setCallback(this);
                client.connect();
            }

            client.subscribe(TOPIC);
        }

        @Override
        public void connectionLost(Throwable throwable)
        {
//          TODO
            System.out.println("MQTT Disconnected, cause: " + throwable.getCause());
            int timeout = 5000;
            while(!client.isConnected())
            {
                try
                {
//                  Try to reconnect in 5 seconds
                    Thread.sleep(timeout);
                    System.out.println("MQTT Reconnecting");
                    client.connect();
                    // TODO: Sistemare il topic di iscrizione a seconda della connessione persa.
                    client.subscribe("tyre_temp");
                    System.out.println("MQTT Connection Restored");
                }
                catch (MqttException me)
                {
                    me.printStackTrace();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                //System.out.println("MQTT Reconnected");
            }

        }

        @Override
        public void messageArrived(String topic, MqttMessage message)
        {

            System.out.println(topic);

//          Ogni volta che arriva un messaggio, lo registro
            String payload = new String(message.getPayload());
            String[] args = payload.split("&");

//          Registra una nuova temperatura per la ruota indicata
            Temperature temp = new Temperature();
            temp.setTimestamp(new Date(System.currentTimeMillis()));
            temp.setTyrePosition(Integer.parseInt(args[0].split("=")[1]));
            temp.setTemperatureValue(Double.parseDouble(args[1].split("=")[1]) / 10);
            System.out.println(String.format("DBG   Temperature = %s,  TyrePos = %d", "" + temp.getTemperatureValue(), temp.getTyrePosition()));

            Actuator act = null;
            
            if (topic.equals(SUBTOPIC_WARMER))
            {
//              Registra temperatura nel DB
                try
                {
                    TemperatureDAO.writeTemperature(temp, "temperature_on_warmer");
                }
                catch (DAOException de)
                {
                    de.printStackTrace();
                }

//              Fai altre cose qui (AZIONA ATTUATORE CORRETTO)
                if (temp.getTemperatureValue() > 70 && act.isOn())
                {
                    act.toggle();
                    TyreActuatorCoAP.sendCommand(act.getAddr(), act.getResource(), "HIGHTEMP");
                    System.out.println(String.format("Tyrewarmer [%d] -> DISENGAGED", act.getTyre_position()));

//                  Abbassare temperatura simulazione
                    try
                    {
                        TyreSensorMQTT.Publisher.Publish("tcp://[::1]:1883", "SimManager", "warmer_on", "-1");
                    }
                    catch (InterruptedException | MqttException ie)
                    {
                        ie.printStackTrace();
                    }
                }
                else if (temp.getTemperatureValue() > 67 && !act.isOn())
                {
                    act.toggle();
                    TyreActuatorCoAP.sendCommand(act.getAddr(), act.getResource(), "LOWTEMP");
                    System.out.println(String.format("Tyrewarmer [%d] -> ENGAGED", act.getTyre_position()));

//                  Alzare temperatura simulazione
                    try
                    {
                        TyreSensorMQTT.Publisher.Publish("tcp://[::1]:1883", "SimManager", "warmer_on", "1");
                    }
                    catch (InterruptedException | MqttException ie)
                    {
                        ie.printStackTrace();
                    }
                }
            }
            //          ------------------

            else if (topic.equals(SUBTOPIC_TRACK))
            {
                try
                {
                    act = TemperatureDAO.getActuator(temp.getTyrePosition(), "res_wheel_led");
                }
                catch (Exception e)
                {
                    System.err.println("ERROR");
                    e.printStackTrace();
                    return;
                }

//              Registra temperatura nel DB
                try
                {
                    TemperatureDAO.writeTemperature(temp, "temperature_on_track");
                }
                catch (DAOException de)
                {
                    de.printStackTrace();
                }

                if (temp.getTemperatureValue() < 90 && act.getStatus() != OnTrackStatus.UNDER)
                {
                    act.setStatus(OnTrackStatus.UNDER);
                    TyreActuatorCoAP.sendCommand(act.getAddr(), act.getResource(), "UNDER");
                    System.out.println(String.format("TyreTrack [%d] -> COLD", act.getTyre_position()));
                }
                else if (temp.getTemperatureValue() > 90 && temp.getTemperatureValue() < 100 && act.getStatus() != OnTrackStatus.GREAT)
                {
                    act.setStatus(OnTrackStatus.GREAT);
                    TyreActuatorCoAP.sendCommand(act.getAddr(), act.getResource(), "GREAT");
                    System.out.println(String.format("TyreTrack [%d] -> GREAT", act.getTyre_position()));
                }
                else if (temp.getTemperatureValue() > 100 && act.getStatus() != OnTrackStatus.OVER)
                {
                    act.setStatus(OnTrackStatus.OVER);
                    TyreActuatorCoAP.sendCommand(act.getAddr(), act.getResource(), "OVER");
                    System.out.println(String.format("TyreTrack [%d] -> OVERHEATING", act.getTyre_position()));
                }
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token)
        {
//          TODO
        }
    }

    public static class Publisher
    {
        public static void Publish(String BROKER, String CLIENTID, String TOPIC, String MESSAGE)
        throws MqttException, InterruptedException
        {
            MqttClient client = new MqttClient(BROKER, CLIENTID);
            client.connect();
            MqttMessage message = new MqttMessage(MESSAGE.getBytes());
            client.publish(TOPIC, message);
            client.disconnect();
        }
    }
}
