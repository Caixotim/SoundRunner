package com.example.goncalomatos.spotifysdktest;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;


public class RunActivity extends Activity implements StepListener{

    private StepDetector stepDetector;

    private SensorManager mSensorManager;
    private Sensor mSensor;

    //TEMP
    private int steps = 0;

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

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {
                        //Run Button Logic
                        TextView accelValue = (TextView) findViewById(R.id.acceleration);

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

    @Override
    public void passValue() {

    }

    private float calculateVelocity (int steps) {
        int userHeight = 180;
        float stepDistance = userHeight * 0.414f;
        float time = 0.5f;
        return steps * stepDistance / time;
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
