package com.example.goncalomatos.spotifysdktest;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

public class GPSDetector implements LocationListener
{
    private static final long INTERVAL = 5000;
    private static final float MIN_DISTANCE = 2;

    private Location startLocation = null;
    private Location lastLocation = null;
    private boolean firstCall;
    private ArrayList<GPSLocationListener> mGPSLocationListeners = new ArrayList<>();

    public GPSDetector(Context context) {
        firstCall = true;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, INTERVAL, MIN_DISTANCE, this);
            }
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, INTERVAL, MIN_DISTANCE, this);
        }
    }

    public void addGPSLocationListener(GPSLocationListener gl) {
        Log.d("GPSDetector", "addGPSLocationListener");
        mGPSLocationListeners.add(gl);
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
        for (GPSLocationListener gpsLocationListener : mGPSLocationListeners) {
            gpsLocationListener.onLocationChange();
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

    public double getTraveledDistance () { //get distance in meters
        if (startLocation != null && lastLocation != null && !firstCall) {
            return startLocation.distanceTo(lastLocation);
        }
        return -1;
    }

    public void resetLocation () {
        startLocation = lastLocation;
    }
}
