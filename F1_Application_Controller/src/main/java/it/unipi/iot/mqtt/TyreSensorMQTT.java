package it.unipi.iot.mqtt;

import it.unipi.iot.coap.TyreActuatorCoAP;
import it.unipi.iot.dao.TemperatureDAO;
import it.unipi.iot.dao.exception.DAOException;
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
        MqttClient client = null;
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
            //System.out.println("MQTT Disconnected");
            while(!client.isConnected())
            {
                try
                {
                    System.out.println("MQTT Reconnecting");
                    client.reconnect();
                }
                catch (MqttException me)
                {
//                  Try to reconnect in 5 seconds
                    try
                    {
                        System.out.println("MQTT Retry in 5 seconds");
                        Thread.sleep(5000);
                    }
                    catch(InterruptedException ie)
                    {
                        ie.printStackTrace();
                    }
                }
                //System.out.println("MQTT Reconnected");
            }

        }

        @Override
        public void messageArrived(String topic, MqttMessage message)
        {
//          Ogni volta che arriva un messaggio, lo registro
            String payload = new String(message.getPayload());
            String[] args = payload.split("&");

//          Registra una nuova temperatura per la ruota indicata
            Temperature temp = new Temperature();
            temp.setTimestamp(new Date(System.currentTimeMillis()));
            temp.setTyrePosition(Integer.parseInt(args[0].split("=")[1]));
            temp.setTemperatureValue(Double.parseDouble(args[1].split("=")[1])/10);
            Actuator act = TyreActuatorCoAP.getTyre(temp.getTyrePosition(), topic);
            double temperature = temp.getTemperatureValue();

            if (topic.equals(SUBTOPIC_WARMER))
            {
//              Registra temperatura nel DB
                try
                {
                    TemperatureDAO.writeTemperature(temp, "temperature_on_warmer");
                }
                catch(DAOException de)
                {
                    de.printStackTrace();
                }

//              Fai altre cose qui (AZIONA ATTUATORE CORRETTO)
                if(temperature > 70 && act.isOn())
                {
                    act.toggle();
                    TyreActuatorCoAP.sendCommand(act.getAddr(), act.getResource(), "HIGHTEMP");
                    System.out.println(String.format("Tyrewarmer [%d] -> DISENGAGED", act.getTyre_position()));

//                  Abbassare temperatura simulazione
                    try
                    {
                        TyreSensorMQTT.Publisher.Publish("tcp://[::1]:1883", "SimManager", "warmer_on", "-1");
                    }
                    catch(InterruptedException ie)
                    {
                        ie.printStackTrace();
                    }
                    catch(MqttException me)
                    {
                        me.printStackTrace();
                    }
                }
                else if (temperature > 67 && !act.isOn())
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
//              Registra temperatura nel DB
                try
                {
                    TemperatureDAO.writeTemperature(temp, "temperature_on_track");
                }
                catch(DAOException de)
                {
                    de.printStackTrace();
                }

                if(temperature < 90 && act.isOn())
                {
                    act.toggle();
                    TyreActuatorCoAP.sendCommand(act.getAddr(), act.getResource(), "UNDER");
                    System.out.println(String.format("TyreTrack [%d] -> COLD", act.getTyre_position()));
                }
                else if (temperature > 90 && temperature < 100 && !act.isOn())
                {
                    // act.toggle();
                    TyreActuatorCoAP.sendCommand(act.getAddr(), act.getResource(), "GREAT");
                    System.out.println(String.format("TyreTrack [%d] -> GREAT", act.getTyre_position()));
                }
                else if (temperature > 100 && !act.isOn())
                {
                    // act.toggle();
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
