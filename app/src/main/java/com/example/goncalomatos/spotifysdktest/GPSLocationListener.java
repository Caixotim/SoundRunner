package com.example.goncalomatos.spotifysdktest;

import android.location.Location;

/**
 * Interface implemented by classes that can handle notifications about location.
 * These classes can be passed to GPSDetector.
 */
public interface GPSLocationListener {
    void onLocationChange(Location location);
}
