package com.example.goncalomatos.spotifysdktest;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.text.DecimalFormat;
import java.util.List;

public class RunActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private static final String CLIENT_ID = "68528c82f0a14b1da759976535533f48";
    private static final String ECHONEST_KEY = "BM5IMCRRSRYJMLZVK";
    private static final String REDIRECT_URI = "my-first-spotify-app://callback";
    private static final int REQUEST_CODE = 1337;
    private static final int PERMISSION_LOCATION = 2;

    private double maxSpeed = 0;
    private double maxStepLength = 0;
    private Intent intent;
    private SpotifyHelper spotifyHelper;
    private double lastSpeed = -50;
    private int lastSteps = 0;
    private double lastLength = 0;
    private boolean isSpotifyAuthenticated;
    private boolean isUserRunning;
    private Polyline polyline;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        index = 0;

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FloatingActionButton fab = (FloatingActionButton) view;
                if(!isUserRunning) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        fab.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause, getApplicationContext().getTheme()));
                    } else {
                        fab.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));
                    }
                    startRun();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        fab.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play, getApplicationContext().getTheme()));
                    } else {
                        fab.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play));
                    }
                    stopRun();
                }
            }
        });

        Button skipButton = (Button) findViewById(R.id.skip_button);
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spotifyHelper.skipSong();
            }
        });

        isSpotifyAuthenticated = false;
        isUserRunning = false;
        startSpotifyAuth();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_LOCATION);
            }
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void startRun(){
        isUserRunning = true;
        Button skipButton = (Button) findViewById(R.id.skip_button);
        skipButton.setEnabled(true);
        startService(intent);
        spotifyHelper.queryAndPlay(0);
    }

    private void stopRun(){
        isUserRunning = false;
        Button skipButton = (Button) findViewById(R.id.skip_button);
        skipButton.setEnabled(false);
        stopService(intent);
        spotifyHelper.pause();
    }

    private void startSpotifyAuth() {
        intent = new Intent(this, MockService.class);

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_LOCATION: {
                if(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED ) {
                    Log.d("gnm", "location get!");
                }
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            final AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                spotifyHelper = new SpotifyHelper(RunActivity.this, response);
                isSpotifyAuthenticated = true;

            }
        }
    }

    private BroadcastReceiver stepBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleSensorData(intent);
        }
    };

    private BroadcastReceiver locationBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleLocationData(intent);
        }
    };

    private void handleLocationData(Intent intent) {
        Log.d("gnm", "handleLocation");
        double latitude = intent.getDoubleExtra("latitude", 0);
        double longitude = intent.getDoubleExtra("longitude", 0);

        index++;

        LatLng lastLocation = new LatLng(latitude, longitude);
        if (polyline == null) {
            polyline = mMap.addPolyline(new PolylineOptions().add(lastLocation));
        }else {
            List<LatLng> list = polyline.getPoints();
            list.add(lastLocation);

            polyline.setPoints(list);
        }

        DecimalFormat df = new DecimalFormat("#.##");
        String text = lastSteps + "-" + df.format(lastSpeed) + "-" + df.format(lastLength);
        mMap.addMarker(new MarkerOptions().position(lastLocation).title("" + index).snippet(text));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(lastLocation));
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(stepBroadcastReceiver, new IntentFilter(MockService.STEP_BROADCAST_ACTION));
        registerReceiver(locationBroadcastReceiver, new IntentFilter(MockService.LOCATION_BROADCAST_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(stepBroadcastReceiver);
        unregisterReceiver(locationBroadcastReceiver);
        stopRun();
    }

    public void updateTrackInfo(String currentTrack){
        TextView trackInfoUI = (TextView) findViewById(R.id.track_info);
        trackInfoUI.setText(currentTrack);
    }


    private void handleSensorData(Intent sensorDataIntent)
    {
        double speed = sensorDataIntent.getDoubleExtra("speed", 0);
        int numSteps = sensorDataIntent.getIntExtra("numSteps", 0);
        double length = sensorDataIntent.getDoubleExtra("length", 0);


        //Log.d("gnm", "speed " + speed + "vs maxSpeed " + maxSpeed);
        //Log.d("gnm", "length " + length + "vs maxLength " + maxStepLength);
        if(speed > maxSpeed ){
            maxSpeed = speed;
        }

        if(length > maxStepLength ){
            maxStepLength = length;
        }

        //Log.d("RUN", "" + (speed * 3.6));
        //Log.d("RUN", "numSteps " + numSteps);
        TextView debug = (TextView) findViewById(R.id.debug);

        double speedKm = speed * 3.6;
        String text = "" + speedKm + " - " + length + " numSteps - " + numSteps;
        text += "\nMaxSpeed " + (maxSpeed * 3.6) +
                "\nMaxLength " + maxStepLength +
                "\nTotalSteps" + sensorDataIntent.getIntExtra("totalSteps", 0);
        debug.setText(text);

        lastSteps = numSteps;
        lastLength = length;
        lastSpeed = speedKm;
        double lastSpeedKm = lastSpeed * 3.6;

        //TODO: strategies and stuff
        if (speedKm > lastSpeedKm + 2 || speedKm < lastSpeedKm - 2) {
            Log.d("RunActivity", "changing pace");

            if(spotifyHelper != null && isSpotifyAuthenticated) {
                spotifyHelper.queryAndPlay(speedKm * 17);
            }
        }
        lastSpeed = speed;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        boolean check = false;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                check = true;
            }
        } else {
            check = true;
        }
        if (check) {
            mMap.setMyLocationEnabled(true);
        }
    }
}
