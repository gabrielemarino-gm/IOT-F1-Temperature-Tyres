package it.unipi.iot.mqtt;

import it.unipi.iot.dao.TemperatureDAO;
import it.unipi.iot.dao.exception.DAOException;
import it.unipi.iot.model.Temperature;
import org.eclipse.paho.client.mqttv3.*;

import java.sql.Date;

public class TyrewarmerMQTT
{
    public static class Subscriber implements MqttCallback
    {
        MqttClient client = null;
        public Subscriber(String BROKER, String CLIENTID, String TOPIC) throws MqttException
        {
            client = new MqttClient(BROKER, CLIENTID);
            client.setCallback(this);
            client.connect();
            client.subscribe(TOPIC);
        }

        @Override
        public void connectionLost(Throwable throwable) {
//            TODO
            System.out.println("MQTT Disconnected");
            while(!client.isConnected()){
                try
                {
                    System.out.println("MQTT Reconnecting");
                    client.reconnect();
                }
                catch(MqttException me)
                {
//                    Try to reconnect in 5 seconds
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
                System.out.println("MQTT Reconnected");
            }

        }

        @Override
        public void messageArrived(String topic, MqttMessage message){
//            TODO
//            Ogni volta che arriva un messaggio, lo registro e attivo le termocoperte
//            se serve
            String payload = new String(message.getPayload());

            Temperature temp = new Temperature();
            temp.setTimestamp(new Date(System.currentTimeMillis()));
            temp.setTyrePosition(1);
            temp.setTemperatureValue(Double.parseDouble(payload)/10);

//            Registra temperatura
            try
            {
                TemperatureDAO.writeTemperature(temp, "temperature_on_warmer");
            }
            catch(DAOException de)
            {
                de.printStackTrace();
            }

//            Fai altre cose qui (AZIONA ATTUATORE CORRETTO)

//            ------------------
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token){
//        TODO
        }
    }

    public static class Publisher
    {
        public static void Publish(String BROKER, String CLIENTID, String TOPIC, String MESSAGE)
        throws MqttException, InterruptedException
        {
            MqttClient client = new MqttClient(BROKER, CLIENTID+"Pub");
            client.connect();
            MqttMessage message = new MqttMessage(MESSAGE.getBytes());
            client.publish(TOPIC, message);
            client.disconnect();
        }
    }



}
