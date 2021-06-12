package com.example.studienarbeit_kugellabyrinth;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

/** A listener to listen for sensor changes
 * @author Philip Bartmann
 * @version 1.0
 * @since 1.0
 */
public abstract class TiltEventListener implements SensorEventListener {

    final String TAG = "TiltEventListener";

    /** An abstract method to be implemented in MainActivity which is called when
     * the sensor values change, i.e. every 0.18 seconds, because that's the period
     * of the sensor updates defined in MainActivity
     * @param xVal x value of the accelerometer of the phone sensors
     * @param yVal y value of the accelerometer of the phone sensors
     */
    public abstract void onTilt(float xVal, float yVal);

    /** This method is called when the sensor values change or when the period
     * defined in the registering of the listener is run out
     * @param se a SensorEvent, which contains the values of the accelerometer
     */
    public void onSensorChanged(SensorEvent se){
        float x = se.values[0];
        float y = se.values[1];

        onTilt(x, y);
    }

    /** This method is called when the accuracy of the sensor values changes
     * @param sensor the sensor for which the method was called
     * @param accuracy the accuracy of the sensor as an integer value
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
