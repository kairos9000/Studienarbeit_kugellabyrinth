package com.example.studienarbeit_kugellabyrinth;

import android.util.Log;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MQTTHandler {

    final String TAG = "MQTTHandler";

    private final MemoryPersistence persistence = new MemoryPersistence();
    private MqttClient client;


    /**
     * Connect to broker and
     * @param broker Broker to connect to
     */
    public boolean connect (String broker) {
        try {
            broker = "tcp://" + broker + ":1883";
            String clientId = MqttClient.generateClientId();
            client = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            Log.d(TAG, "Connecting to broker: " + broker);
            client.connect(connOpts);
            Log.d(TAG, "Connected with broker: " + broker);
            return true;
        } catch (MqttException me) {
            return false;
        }

    }


    /**
     * Unsubscribe from default topic (please unsubscribe from further
     * topics prior to calling this function)
     */
    public void disconnect() {
        try {
            Log.d(TAG, "Disconnecting from broker");
            client.disconnect();
            Log.d(TAG, "Disconnected.");
        } catch (MqttException me) {
            Log.e(TAG, me.getMessage());
        }
    }
}
