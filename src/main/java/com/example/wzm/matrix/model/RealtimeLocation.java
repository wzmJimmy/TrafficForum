package com.example.wzm.matrix.model;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import static android.content.Context.LOCATION_SERVICE;

public class RealtimeLocation implements LocationListener{

    private final Activity mContext;
    private static final int PERMISSIONS_REQUEST_LOCATION = 99;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; //mile
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60; //ms

    private boolean mIsGPSEnabled;
    private boolean mIsNetworkEnabled;

    private boolean test=true;
    private Location location;
    private double latitude;
    private double longitude;
    private LocationManager locationManager;


    public RealtimeLocation(Activity context) {
        this.mContext = context;
    }

    @Override
    public void onLocationChanged(Location location) { }

    @Override
    public void onProviderDisabled(String provider) { }

    @Override
    public void onProviderEnabled(String provider) { }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }


    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            mIsGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            mIsNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!mIsGPSEnabled && !mIsNetworkEnabled) {
                return null;
            } else {
                checkLocationPermission();
                // First get location from Network Provider
                if (!test&&mIsNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if ( mIsGPSEnabled && (test||location==null)) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    public double getLatitude() {
        if (location != null) { latitude = location.getLatitude(); }
        return latitude;
    }

    public double getLongitude() {
        if (location != null) { longitude = location.getLongitude(); }

        return longitude;
    }

    /**
     * Run time permission check
     * @return if the permission is set
     */
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(mContext,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_LOCATION);
        }
        return true;
    }

}



