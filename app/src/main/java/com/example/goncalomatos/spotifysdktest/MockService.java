package com.example.goncalomatos.spotifysdktest;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class MockService extends Service implements GPSLocationListener{
    private static final String TAG = "BroadcastService";
    public static final String BROADCAST_ACTION = "com.soundRunner.Mock";
    private final Handler handler = new Handler();
    private GPSDetector gpsDetector;
    private StepCounter stepCounter;
    Intent intent;

    private double stepLength;
    private int lastSteps = 0;
    private int lastStepsSinceGPSLocation = 0;

    private static final int INTERVAL = 2000;

    @Override
    public void onCreate() {
        super.onCreate();

        stepCounter = new StepCounter(this);
        gpsDetector = new GPSDetector(this);
        gpsDetector.addGPSLocationListener(this);
        intent = new Intent(BROADCAST_ACTION);

        if(stepLength == 0) {
            stepLength = Float.parseFloat(PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString("pref_height", "1.75")) * 0.414;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.removeCallbacks(calcSpeed);
        handler.postDelayed(calcSpeed, INTERVAL);

        return super.onStartCommand(intent, flags, startId);
    }

    private Runnable calcSpeed = new Runnable() {
        public void run() {
            double speed = 0;
            int currentSteps = stepCounter.getSteps();
            double diffSteps = currentSteps - lastSteps;
            int time = INTERVAL/1000;

            speed = diffSteps * stepLength / time;

            lastSteps = currentSteps;
            intent.putExtra("speed", speed);
            sendBroadcast(intent);
            handler.postDelayed(this, INTERVAL);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(calcSpeed);
        super.onDestroy();
    }

    @Override
    public void onLocationChange() {
        //update stepLength
        int currentSteps = stepCounter.getSteps();
        double distance = gpsDetector.getTraveledDistance();

        stepLength = distance / currentSteps;
        gpsDetector.resetLocation();
    }
}
