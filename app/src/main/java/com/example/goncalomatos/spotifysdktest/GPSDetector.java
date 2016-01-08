package com.example.goncalomatos.spotifysdktest;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class GPSDetector implements LocationListener
{
    private Location startLocation = null;
    private Location lastLocation = null;
    private boolean firstCall;

    public GPSDetector(Context context) {
        firstCall = true;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (firstCall) {
            startLocation = location;
            Log.d("GPSDetector", "startLocation: \n" + "latitude = " + Double.toString(startLocation.getLatitude()) + "\nlongitude = " + Double.toString(startLocation.getLongitude()));
            firstCall = false;
        } else {
            lastLocation = location;
            Log.d("GPSDetector", "lastLocation: \n" + "latitude = " + Double.toString(startLocation.getLatitude()) + "\nlongitude = " + Double.toString(startLocation.getLongitude()));
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public float getTraveledDistance () { //get distance in meters
        if (startLocation != null && lastLocation != null) {
            return startLocation.distanceTo(lastLocation);
        }
        return 2;
    }

    public void resetLocation () {
        startLocation = lastLocation;
    }
}
