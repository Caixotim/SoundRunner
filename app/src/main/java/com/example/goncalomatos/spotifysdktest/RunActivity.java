package com.example.goncalomatos.spotifysdktest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

public class RunActivity extends AppCompatActivity {

    private static final String CLIENT_ID = "68528c82f0a14b1da759976535533f48";
    private static final String ECHONEST_KEY = "BM5IMCRRSRYJMLZVK";
    private static final String REDIRECT_URI = "my-first-spotify-app://callback";
    private static final int REQUEST_CODE = 1337;

    private Intent intent;
    private SpotifyHelper spotifyHelper;
    private int lastPace = -50;
    private boolean isSpotifyAuthenticated;
    private boolean isUserRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
    }

    private void startRun(){
        isUserRunning = true;
        Button skipButton = (Button) findViewById(R.id.skip_button);
        skipButton.setEnabled(true);
        // TODO: 04/01/16  this should be a foreground service
        startService(intent);
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

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        handleSensorData(intent);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(MockService.BROADCAST_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onStop(){
        super.onStop();
        stopRun();
    }

    public void updateTrackInfo(String currentTrack){
        TextView trackInfoUI = (TextView) findViewById(R.id.track_info);
        trackInfoUI.setText(currentTrack);
    }


    private void handleSensorData(Intent sensorDataIntent)
    {
        int pace = sensorDataIntent.getIntExtra("pace", 200);
        Log.d("RUN", "" + pace);

        //TODO: strategies and stuff
        if (pace > lastPace + 50 || pace < lastPace - 50 ) {
            Log.d("RunActivity", "changing pace");

            if(spotifyHelper != null && isSpotifyAuthenticated) {
                spotifyHelper.queryAndPlay(pace);
            }
        }
        lastPace = pace;
    }


}
