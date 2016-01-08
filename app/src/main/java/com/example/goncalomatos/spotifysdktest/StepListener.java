package com.example.goncalomatos.spotifysdktest;

/**
 * Interface implemented by classes that can handle notifications about steps.
 * These classes can be passed to StepDetector.
 * @author Levente Bagi
 */
public interface StepListener {
    void onStep();
}
