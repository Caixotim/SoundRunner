package com.example.goncalomatos.spotifysdktest;

import java.util.Date;

/**
 * Created by goncalomatos on 10/01/16.
 */
public class RunStat {
    private long time;
    private int steps;
    private double speed;

    public RunStat(long _time, int _steps, double _speed) {
        time = _time;
        steps = _steps;
        speed = _speed;
    }

    public long getTime() {
        return time;
    }

    public String getFormattedTime() {
        int hours = (int) (time/(1000 * 60 * 60));
        int mins = (int) (time/(1000*60)) % 60;
        long secs = (int) (time / 1000) % 60;
        return hours + ":" + mins + ":" + secs;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
}
