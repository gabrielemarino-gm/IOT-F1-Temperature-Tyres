package it.unipi.iot.mqtt;

import org.eclipse.paho.client.mqttv3.*;


public class TyrewarmerMQTT
{
    public static class Subscriber implements MqttCallback
    {
        public Subscriber(String BROKER, String CLIENTID, String TOPIC) throws MqttException
        {
            MqttClient client = new MqttClient(BROKER, CLIENTID);
            client.setCallback(this);
            client.connect();
            client.subscribe(TOPIC);
        }

        @Override
        public void connectionLost(Throwable throwable) {
//        TODO
        }

        public void messageArrived(String topic, MqttMessage message){
//        TODO
            System.out.println(String.format("[%s] -> %s",topic, new String(message.getPayload())));
        }

        public void deliveryComplete(IMqttDeliveryToken token){
//        TODO
        }
    }

    public static class Publisher
    {
        public void Publish(String BROKER, String CLIENTID, String TOPIC, String MESSAGE)
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
