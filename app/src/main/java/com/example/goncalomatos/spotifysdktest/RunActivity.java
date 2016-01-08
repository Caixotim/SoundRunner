package com.example.goncalomatos.spotifysdktest;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

public class RunActivity extends Activity implements StepListener{

    private StepDetector stepDetector;
    private GPSDetector gpsDetector;

    private SensorManager mSensorManager;
    private Sensor mSensor;

    //TEMP
    private int steps = 0;
    private long startTime;

    //timer for log
    Timer timer;
    TimerTask timerTask;
    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        stepDetector = new StepDetector();
        stepDetector.addStepListener(this);
        registerDetector();

        gpsDetector = new GPSDetector(this);

        startTime = new Date().getTime();

        timer = new Timer();

        initializeTimerTask();
        timer.schedule(timerTask, 0, 500);


    }



    protected void onPause() {
        super.onPause();
    }

    protected void onResume() {
        super.onResume();
    }

    protected void onDestroy() {
        unregisterDetector();
        super.onDestroy();
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {

                        //float traveledDistance = gpsDetector.getTraveledDistance();
                        long endTime = new Date().getTime();

                        long deltaTime = endTime - startTime;
                        //long deltaTimeInSeconds = TimeUnit.MILLISECONDS.toSeconds(deltaTime);

                        //Run Button Logic
                        TextView stepsValue = (TextView) findViewById(R.id.stepsValue);
                        stepsValue.setText(Integer.toString(steps));

                        //TextView distanceValue = (TextView) findViewById(R.id.distanceValue);
                        //distanceValue.setText(Float.toString(traveledDistance));

                        TextView velocityValue = (TextView) findViewById(R.id.velocityValue);
                        velocityValue.setText(Float.toString(calculateVelocity(steps, 0, deltaTime)));
                    }
                });
            }
        };
    }

    @Override
    public void onStep() {
        steps++;
        Log.d("Run Activity: ", "Steps = " + Integer.toString(steps));
    }

    //Move this to a helper class
    private float calculateVelocity (int steps, float stepDistanceParam, long deltaTime) {
        int userHeight = 180;
        float stepDistance;
        float deltaTimeInSeconds = TimeUnit.MILLISECONDS.toSeconds(deltaTime);

        if (stepDistanceParam == 0) {
            stepDistance = userHeight * 0.414f;
        } else {
            stepDistance = stepDistanceParam;
        }

        return (steps * stepDistance) / deltaTimeInSeconds;
    }



    private void registerDetector() {
        mSensor = mSensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(stepDetector,
                mSensor,
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void unregisterDetector() {
        mSensorManager.unregisterListener(stepDetector);
    }
}
