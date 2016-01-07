package com.example.goncalomatos.spotifysdktest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    // This is just for testing purposes
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Run Button Logic
        ImageButton runBtn = (ImageButton) findViewById(R.id.run_button);
        runBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRun();
            }
        });

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        settings();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.action_statistics:
                startActivity(new Intent(MainActivity.this, StatisticsActivity.class));
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    protected void startRun() {
        Log.d(TAG, "START RUN");

        startActivity(new Intent(MainActivity.this, RunActivity.class));
    }

    protected void settingsMenu() {
        Log.d(TAG, "Settings Menu Activated");
    }

    private void settings() {

        ImageButton actiondefi=(ImageButton) findViewById(R.id.settingsButton);
        actiondefi.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View View) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });
    }
}
