package com.example.studienarbeit_kugellabyrinth;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

public abstract class TiltEventListener implements SensorEventListener {
    final String TAG = "TiltEventListener";
    float mAccelCurrent;
    float mAccelLast;

    public abstract void onTilt(float xVal, float yVal);

    public void setGravitationalConstant(float gravConstant){
        mAccelCurrent = gravConstant;
        mAccelLast = gravConstant;
    }


    public void onSensorChanged(SensorEvent se){
        float x = se.values[0];
        float y = se.values[1];
        //float z = se.values[2];

        //Log.d(TAG, String.valueOf(x)+ String.valueOf(y));


        onTilt(x, y);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
