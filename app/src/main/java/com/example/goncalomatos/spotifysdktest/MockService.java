package com.example.goncalomatos.spotifysdktest;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

public class MockService extends Service implements GPSLocationListener{
    private static final String TAG = "BroadcastService";
    public static final String BROADCAST_ACTION = "com.soundRunner.Mock";
    private static final int NOTIFICATION_KEY = 123;

    private final Handler handler = new Handler();
    private GPSDetector gpsDetector;
    private StepCounter stepCounter;
    Intent intent;

    private double stepLength;
    private int lastSteps = 0;
    private int lastStepsSinceGPSLocation = 0;

    private static final int INTERVAL = 5000;

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
                .getString("pref_height", "175")) * 0.00414;
        }

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("SoundRunner")
                .setTicker("SoundRunner")
                .setContentText("Running")
                .setOngoing(true).build();
        startForeground(NOTIFICATION_KEY,
                notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.removeCallbacks(calcSpeed);
        handler.postDelayed(calcSpeed, 1);

        return super.onStartCommand(intent, flags, startId);
    }

    private Runnable calcSpeed = new Runnable() {
        public void run() {
            Log.d("cenas", "calcSpeed");
            double speed = 0;
            int currentSteps = stepCounter.getSteps();
            int diffSteps = currentSteps - lastSteps;
            int time = INTERVAL/1000;

            speed = diffSteps * stepLength / time;

            Log.d("cenas", "currSteps:" + currentSteps);
            Log.d("cenas", "lastSteps:" + lastSteps);
            Log.d("cenas", "diffSteps:" + diffSteps);
            lastSteps = currentSteps;
            intent.putExtra("numSteps", diffSteps);
            intent.putExtra("speed", speed);
            intent.putExtra("length", stepLength);
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
        Log.d("cenas", "destroy le service");
        handler.removeCallbacks(calcSpeed);
        stepCounter.unregister();
        super.onDestroy();
    }

    @Override
    public void onLocationChange() {
        Log.d("cenas", "location changed");
        //update stepLength
        int currentSteps = stepCounter.getSteps();
        int diff = currentSteps -  lastStepsSinceGPSLocation;
        double distance = gpsDetector.getTraveledDistance();
        lastStepsSinceGPSLocation = currentSteps;

        stepLength = distance / currentSteps;
        Log.d("cenas", "" + distance);
        Log.d("cenas", "" + currentSteps);
        gpsDetector.resetLocation();
    }
}
