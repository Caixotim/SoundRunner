package com.example.goncalomatos.spotifysdktest;

import android.util.Log;

import java.util.ArrayList;

/**
 * Counts steps provided by StepDetector and passes the current
 * step count to the activity.
 */
public class StepManager implements StepListener {

    private int mCount = 0;
    private static StepManager _instance = null;

    private StepManager() {
        notifyListener();
    }

    public static StepManager instance() {
        if (_instance == null) {
            _instance = new StepManager();
        }
        return _instance;
    }

    public void setSteps(int steps) {
        mCount = steps;
        notifyListener();
    }

    @Override
    public void onStep() {
        Log.d("StepManager", "onStep");
        mCount ++;
        notifyListener();
    }

    @Override
    public void passValue() {
        Log.d("StepManager", "passValue");

    }
    /*public void reloadSettings() {
        notifyListener();
    }*/

    public void resetStepCount() {
        mCount = 0;
    }



    //-----------------------------------------------------
    // Listener

    public interface Listener {
        public void stepsChanged(int value);
        public void passValue();
    }
    private ArrayList<Listener> mListeners = new ArrayList<Listener>();

    public void addListener(Listener l) {
        mListeners.add(l);
    }
    public void notifyListener() {
        for (Listener listener : mListeners) {
            listener.stepsChanged((int)mCount);
        }
    }
}