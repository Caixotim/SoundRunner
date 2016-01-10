package com.example.goncalomatos.spotifysdktest;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

public class MockService extends Service implements GPSLocationListener{
    private static final String TAG = "BroadcastService";
    public static final String STEP_BROADCAST_ACTION = "com.soundRunner.Step";
    public static final String LOCATION_BROADCAST_ACTION = "com.soundRunner.Location";
    private static final int NOTIFICATION_KEY = 123;

    private final Handler handler = new Handler();
    private GPSDetector gpsDetector;
    private StepCounter stepCounter;
    Intent stepIntent;
    Intent locationIntent;

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
        stepIntent = new Intent(STEP_BROADCAST_ACTION);
        locationIntent = new Intent(LOCATION_BROADCAST_ACTION);

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
            stepIntent.putExtra("numSteps", diffSteps);
            stepIntent.putExtra("totalSteps", currentSteps);
            stepIntent.putExtra("speed", speed);
            stepIntent.putExtra("length", stepLength);
            sendBroadcast(stepIntent);
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
        gpsDetector.disconnect();
        super.onDestroy();
    }

    @Override
    public void onLocationChange(Location location) {
        Log.d("cenas", "location changed ");
        //update stepLength

        int currentSteps = stepCounter.getSteps();
        Log.d("gnm", "currentSteps " + currentSteps + "lastSteps " + lastStepsSinceGPSLocation);
        int diff = currentSteps -  lastStepsSinceGPSLocation;
        if (diff == 0) {
            locationIntent.putExtra("latitude", location.getLatitude());
            locationIntent.putExtra("longitude", location.getLongitude());
            return;
        }
        double distance = gpsDetector.getTraveledDistance();
        if (distance != -1) {
            stepLength = distance / diff;
            gpsDetector.resetLocation();
        }
        lastStepsSinceGPSLocation = currentSteps;

        Log.d("gnm", " distance " + distance);
        Log.d("gnm", " latitude " + location.getLatitude());
        Log.d("gnm", " longitude " + location.getLongitude());

        locationIntent.putExtra("latitude", location.getLatitude());
        locationIntent.putExtra("longitude", location.getLongitude());
        sendBroadcast(locationIntent);
    }
}
