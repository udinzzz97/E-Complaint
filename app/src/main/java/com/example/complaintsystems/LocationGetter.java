package com.example.complaintsystems;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.content.ContextCompat;


public class LocationGetter implements LocationListener {

    private Location myLocation;
    private LocationManager locationManager;
    private OnFinishedListener mAfter;
    private int LOCATION_REFRESH_TIME = 5000,
            LOCATION_REFRESH_DISTANCE = 10;

    public LocationGetter(Context context, OnFinishedListener after) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // permission denied, boo! Disable the submit button
            Toast.makeText(context, "Permission denied. Please go to setting and enable location access for this app", Toast.LENGTH_SHORT).show();
        }
        else {
            mAfter = after;
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, this);
        }
    }

    @Override
    public void onLocationChanged(final Location location) {
        myLocation = location;
        mAfter.onFinished(myLocation);
        locationManager.removeUpdates(this);
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    public interface OnFinishedListener {
        void onFinished(Location location);
    }
}
