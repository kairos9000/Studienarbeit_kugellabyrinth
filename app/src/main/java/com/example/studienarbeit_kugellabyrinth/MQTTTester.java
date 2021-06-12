package com.example.studienarbeit_kugellabyrinth;

import android.util.Log;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/** Tests if a MQTT connection can be established by connecting and disconnecting in
 * SettingsActivity
 * @author Philip Bartmann
 * @version 1.0
 * @since 1.0
 */
public class MQTTTester {

    final String TAG = "MQTTTester";

    /** MqttClient to test connection
     */
    private MqttClient client;

    /**
     * Connect to broker given by the user
     * @param broker Broker to connect to
     */
    public boolean connect (String broker) {
        try {
            broker = "tcp://" + broker + ":1883";
            String clientId = MqttClient.generateClientId();
            final MemoryPersistence persistence = new MemoryPersistence();
            client = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            client.connect(connOpts);

            return true;
        } catch (MqttException me) {
            return false;
        }

    }



    /**
     * end test connection to MQTT Broker
     */
    public void disconnect() {
        try {
            client.disconnect();
        } catch (MqttException me) {
            Log.e(TAG, me.getMessage());
        }
    }
}
