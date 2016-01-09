package com.example.goncalomatos.spotifysdktest;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class StepCounter implements StepListener{

    private int steps;
    private StepDetector stepDetector;
    private Sensor mSensor;
    private SensorManager mSensorManager;

    public StepCounter(Context context) {
        steps = 0;

        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);

        float sensitivity = Float.parseFloat(PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString("pref_sensitivity", "20"));

        stepDetector = new StepDetector();
        stepDetector.setSensitivity(sensitivity);
        stepDetector.addStepListener(this);
        registerStepDetector();
    }

    public void unregister(){
        mSensorManager.unregisterListener(stepDetector);
    }

    @Override
    public void onStep() {
        steps++;
    }

    public int getSteps() {
        return steps;
    }

    private void registerStepDetector() {
        mSensor = mSensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(stepDetector,
                mSensor,
                SensorManager.SENSOR_DELAY_FASTEST);
    }
}
