package com.example.goncalomatos.spotifysdktest;

public class StepCounter implements StepListener{

    private int steps;

    public StepCounter() {
        steps = 0;
    }

    @Override
    public void onStep() {
        steps++;
    }

    public int getSteps() {
        return steps;
    }
}
