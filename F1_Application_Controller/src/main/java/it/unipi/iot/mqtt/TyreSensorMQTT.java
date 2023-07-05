package it.unipi.iot.mqtt;

import it.unipi.iot.coap.TyreActuatorCoAP;
import it.unipi.iot.dao.TemperatureDAO;
import it.unipi.iot.dao.exception.DAOException;
import it.unipi.iot.model.Actuator;
import it.unipi.iot.model.Temperature;
import it.unipi.iot.utilis.Utils;
import org.eclipse.paho.client.mqttv3.*;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

public class TyreSensorMQTT
{
    private static String SUBTOPIC_WARMER = "TyrewarmerTemp";
    private static String SUBTOPIC_TRACK = "TyreTemp";

    public static class Subscriber implements MqttCallback
    {
        static MqttClient client = null;
        public Subscriber(String BROKER, String CLIENTID, String... args) throws MqttException
        {
            if(client == null)
            {
                client = new MqttClient(BROKER, CLIENTID);
                client.setCallback(this);
                client.connect();
            }
            for(String s : args)
            {
                client.subscribe(s);
            }
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
            try
            {
//              Ogni volta che arriva un messaggio, lo registro
                String payload = new String(message.getPayload());
                System.out.println("DBG:        payload:" + payload);
                
//              Faccio il parsin del Json, mettento le info in variabili
                Map<String, Object> receivedJson = Utils.jsonParser(payload);

//              Ricavo la Temperatura
                assert receivedJson != null;
                System.out.println("DBG:        " + receivedJson.toString());
                String temperatureString = receivedJson.get("temperature").toString();
                double temperature = Double.parseDouble(temperatureString);
//              Ricavo Posizione della ruota
                String tyrePositiontring = receivedJson.get("tyre").toString();
                int tyrePosition = Integer.parseInt(tyrePositiontring);
//              Ricavo il Timestamp e setto la data
                String timestampString = receivedJson.get("timestamp").toString();
                String pattern = "yyyy-MM-dd HH:mm:ss";
                SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
                Date date = new Date(dateFormat.parse(timestampString).getTime());

//              Registra una nuova temperatura per la ruota indicata
                Temperature temp = new Temperature();
                temp.setTyrePosition(tyrePosition);
                temp.setTemperatureValue(temperature/10);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.add(Calendar.HOUR_OF_DAY, 2);

                temp.setTimestamp(new Date(calendar.getTime().getTime()));

                Actuator act = null;

                if (topic.equals(SUBTOPIC_WARMER))
                {
                    act = TemperatureDAO.getActuator(temp.getTyrePosition(), "tyrewarmer");

//                  Registra temperatura nel DB
                    try
                    {
                        TemperatureDAO.writeTemperature(temp, "temperature_on_warmer");
                    }
                    catch (DAOException de)
                    {
                        de.printStackTrace();
                    }

//                  Fai altre cose qui (AZIONA ATTUATORE CORRETTO)
                    if (temp.getTemperatureValue() > 70)
                    {
                        TyreActuatorCoAP.sendCommand(act.getAddr(), act.getResource(), "HIGHTEMP");
//                        System.out.println(String.format("Tyrewarmer [%d] -> DISENGAGED", act.getTyre_position()));

//                      Abbassare temperatura simulazione
                        try
                        {
                            TyreSensorMQTT.Publisher.Publish("tcp://[::1]:1883", "SimManager", "warmer_on", "-1");
                        }
                        catch (InterruptedException | MqttException ie)
                        {
                            ie.printStackTrace();
                        }
                    }
                    else if (temp.getTemperatureValue() < 67)
                    {
                        TyreActuatorCoAP.sendCommand(act.getAddr(), act.getResource(), "LOWTEMP");
//                        System.out.println(String.format("Tyrewarmer [%d] -> ENGAGED", act.getTyre_position()));

//                      Alzare temperatura simulazione
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
                    act = TemperatureDAO.getActuator(temp.getTyrePosition(), "res_wheel_led");
//                  Registra temperatura nel DB
                    try
                    {
                        TemperatureDAO.writeTemperature(temp, "temperature_on_track");
                    }
                    catch (DAOException de)
                    {
                        de.printStackTrace();
                    }

                    if (temp.getTemperatureValue() < 90)
                    {
                        TyreActuatorCoAP.sendCommand(act.getAddr(), act.getResource(), "UNDER");
//                        System.out.println(String.format("TyreTrack [%d] -> COLD", act.getTyre_position()));
                    }
                    else if (temp.getTemperatureValue() >= 90 && temp.getTemperatureValue() <= 100)
                    {
                        TyreActuatorCoAP.sendCommand(act.getAddr(), act.getResource(), "GREAT");
//                        System.out.println(String.format("TyreTrack [%d] -> GREAT", act.getTyre_position()));
                    }
                    else if (temp.getTemperatureValue() > 100)
                    {
                        TyreActuatorCoAP.sendCommand(act.getAddr(), act.getResource(), "OVER");
//                        System.out.println(String.format("TyreTrack [%d] -> OVERHEATING", act.getTyre_position()));
                    }
                }
            }
            catch (Exception e)
            {
                System.err.println("ERROR DURING INCOMING MESSAGE ARRIVED!");
                e.printStackTrace();
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
