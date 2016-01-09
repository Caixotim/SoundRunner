package com.example.goncalomatos.spotifysdktest;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.Random;

public class MockService extends Service implements GPSLocationListener{
    private static final String TAG = "BroadcastService";
    public static final String BROADCAST_ACTION = "com.soundRunner.Mock";
    private final Handler handler = new Handler();
    private GPSDetector gpsDetector;
    Intent intent;
    int counter = 0;

    private static final int INTERVAL = 20000;

    @Override
    public void onCreate() {
        super.onCreate();

        gpsDetector = new GPSDetector(getApplicationContext());
        gpsDetector.addGPSLocationListener(this);
        intent = new Intent(BROADCAST_ACTION);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 1000); // 1 second

        return super.onStartCommand(intent, flags, startId);
    }

    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
        DisplayLoggingInfo();
        handler.postDelayed(this, INTERVAL);
        }
    };

    private void DisplayLoggingInfo() {
        Log.d(TAG, "entered DisplayLoggingInfo");

        Random random = new Random();

        int mockPace = (int) (random.nextInt(275));

        intent.putExtra("pace", mockPace);
        sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(sendUpdatesToUI);
        super.onDestroy();
    }

    @Override
    public void onLocationChange() {
        //TODO: IMPLEMENT
    }
}
