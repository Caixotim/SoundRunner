package com.example.goncalomatos.spotifysdktest;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

public class GPSDetector implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
    private static final long INTERVAL = 15000;
    private static final long MIN_INTERVAL = 10000;
    private static final float MIN_DISTANCE = 2;
    private static final int MIN_ACCURACY = 5;

    private Location startLocation = null;
    private Location lastLocation = null;
    private boolean firstCall;
    private ArrayList<GPSLocationListener> mGPSLocationListeners = new ArrayList<>();
    private GoogleApiClient mGoogleApiClient;
    private Context context;

    public GPSDetector(Context _context) {
        context = _context;
        firstCall = true;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        }

    }

    public void addGPSLocationListener(GPSLocationListener gl) {
        Log.d("GPSDetector", "addGPSLocationListener");
        mGPSLocationListeners.add(gl);
    }

    public void disconnect() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onLocationChanged(Location location) {
        float acc = location.getAccuracy();
        Log.d("acc", "" + location.getAccuracy());

        if (acc > MIN_ACCURACY) {
           return;
        }

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

    public double getTraveledDistance () { //get distance in meters
        if (startLocation != null && lastLocation != null && !firstCall) {
            return startLocation.distanceTo(lastLocation);
        }
        return -1;
    }

    public void resetLocation () {
        startLocation = lastLocation;
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setSmallestDisplacement(MIN_DISTANCE);
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(MIN_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        mGoogleApiClient, mLocationRequest, this);
            }
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
