package com.example.goncalomatos.spotifysdktest;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

public class StepCounter implements StepListener{

    private int steps;
    private StepDetector stepDetector;
    private Sensor mSensor;
    private SensorManager mSensorManager;

    public StepCounter(Context context) {
        steps = 0;

        mSensorManager = (SensorManager)context.getSystemService(context.SENSOR_SERVICE);
        stepDetector = new StepDetector();
        stepDetector.addStepListener(this);
        registerStepDetector();
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
