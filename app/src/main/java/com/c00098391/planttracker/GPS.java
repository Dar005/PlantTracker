package com.c00098391.planttracker;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

public class GPS extends Service implements LocationListener {


    private final Context mContext;
    // Check GPS status
    boolean isGPSEnabled = false;
    // Check network status
    boolean isNetworkEnabled = false;
    // flag for GPS status
    boolean canGetLocation = false;
    Location location;
    double latitude;
    double longitude;

    // Set the distance to update GPS in meters..
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATE = 1;
    // Thr minimum time between updates in milliseconds
    private static final long MIN_TIME_BETWEEN_UPDATES = 1000 * 10; // 10 seconds, 1 min = 1000 * 60 * 1...
    // Declaring a Location Manager
    protected LocationManager locationManager;

    public GPS(Context context){
        this.mContext = context;
        getLocation();
    }
    @SuppressLint("MissingPermission")
    public Location getLocation(){

        try{
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            // Get GPS Status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // Get Network Status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && isNetworkEnabled){
                // no provides enabled
            }else{
                this.canGetLocation = true;
                if(isNetworkEnabled){
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BETWEEN_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATE,
                            this);
                    Log.d("Network Enabled", "Network Enabled");
                    if(locationManager != null){
                        location = locationManager.getLastKnownLocation(
                                LocationManager.NETWORK_PROVIDER);
                        if (location != null){
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                if(isGPSEnabled){
                    if (location == null){
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BETWEEN_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATE,
                                this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null){
                            location = locationManager.getLastKnownLocation(
                                    LocationManager.GPS_PROVIDER);
                            if (location != null){
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e){
            e.printStackTrace();
        }
        return location;
    }

    // Stop using GPS listener
    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(GPS.this);
        }
    }

    // Get Latitude
    public double getLatitude(){
        if (location != null){
            latitude = location.getLatitude();
        }
        return latitude;
    }

    // Get Longitude
    public double getLongitude(){
        if (location != null){
            longitude = location.getLongitude();
        }
        return longitude;
    }

    public boolean canGetLocation(){
        return this.canGetLocation;
    }

    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        // Dialog title
        alertDialog.setTitle("GPS settings");
        // Dialog message
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });
        // if cancel button is pressed
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        alertDialog.show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onLocationChanged(Location location) {

        if(isNetworkEnabled){
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BETWEEN_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATE,
                    this);
            Log.d("Network Enabled", "Network Enabled");
            if(locationManager != null){
                location = locationManager.getLastKnownLocation(
                        LocationManager.NETWORK_PROVIDER);
                if (location != null){
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }
            }
        }
        if(isGPSEnabled){
            if (location == null){
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BETWEEN_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATE,
                        this);
                Log.d("GPS Enabled", "GPS Enabled");
                if (locationManager != null){
                    location = locationManager.getLastKnownLocation(
                            LocationManager.GPS_PROVIDER);
                    if (location != null){
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }
            }
        }

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

}
