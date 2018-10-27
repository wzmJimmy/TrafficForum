package com.example.wzm.matrix.model;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import static android.content.Context.LOCATION_SERVICE;

public class LocationTracker{

    private final Activity mContext;
    private static final int PERMISSIONS_REQUEST_LOCATION = 99;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; //mile
    private static final long MIN_TIME_BW_UPDATES = 1000 * 10; //ms

    private boolean mIsGPSEnabled;
    private boolean mIsNetworkEnabled;

    private Location location;
    private double latitude;
    private double longitude;
    private LocationManager locationManager;

    private final boolean test = true;
    OnLocationChangedListener mCallback;

    // Containermust implement this interface
    public interface OnLocationChangedListener {
        void OnLocationChanged();
    }
    public void setOnLocationChangedListener(OnLocationChangedListener callback) {
        mCallback = callback;
    }

    public LocationTracker(Activity activity) {
        this.mContext = activity;
        bindLocationManager();
    }

    private class mLocationListener implements LocationListener{
        @Override
        public void onLocationChanged(Location newlocation) {
                location = newlocation;
                Log.d("Location#", "LocationTracker: "+location);
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }
                mCallback.OnLocationChanged();
        }

        @Override
        public void onProviderDisabled(String provider) {
            bindLocationManager();
        }

        @Override
        public void onProviderEnabled(String provider) { }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { }
    }




    public void bindLocationManager() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            mIsGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            mIsNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!mIsGPSEnabled && !mIsNetworkEnabled) {
                return;
            } else {
                checkLocationPermission();
                // First get location from Network Provider
                if (!test&&mIsNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, new mLocationListener());
                }
                // if GPS Enabled get lat/long using GPS Services
                if ( mIsGPSEnabled && (test||location==null)) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, new mLocationListener());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Location getLocation() {
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



