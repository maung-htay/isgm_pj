package com.isgm.camreport.utility;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

//Default Android GetCurrent Location
public class GetCurrentLocation extends Service implements LocationListener {
    private Context context;
    // Flag for GPS status
    private boolean isGPSEnabled = false;
    // Flag for network status
    private  boolean isNetworkEnabled = false;
    // Flag for GPS status
    private  boolean canGetLocation = false;
    private  Location location; // Location
    private  double latitude; // Latitude
    private  double longitude; // Longitude
    private  double altitude; //altitude
    // The minimum distance to change Updates in meters
    private final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    // The minimum time between updates in milliseconds
    private  final long MIN_TIME_BW_UPDATES = 1000 * 60; // 1 minute
    // Declaring a Location Manager
    public LocationManager locationManager;

    private static GetCurrentLocation instance;

    public static GetCurrentLocation getInstance(Context context){
        if(instance==null){
            instance = new GetCurrentLocation(context);
        }
        return instance;
    }

    public GetCurrentLocation(Context context) {
        this.context = context;
    }
    public boolean getLocation() {
        try {
            if(locationManager == null) {
                locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
            }
            // Getting GPS status , network status
            if (locationManager != null) {
                isGPSEnabled = locationManager.
                        isProviderEnabled(LocationManager.GPS_PROVIDER);
                isNetworkEnabled = locationManager.
                        isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            }

            if (isGPSEnabled || isNetworkEnabled) {
                canGetLocation = true;
                if (isNetworkEnabled) {
                    try {
                        if (locationManager != null)
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, GetCurrentLocation.this);
                    }
                    catch (SecurityException ex) {
                        ex.printStackTrace();
                    }
                    if (locationManager != null)
                    {
                        try {
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        }
                        catch (SecurityException ex) {
                            ex.printStackTrace();
                        }
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            altitude =location.getAltitude();
                        }
                    }
                }
                // If GPS enabled, get latitude/longitude using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        try {
                            if (locationManager != null)
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        }
                        catch (SecurityException ex) {
                            ex.printStackTrace();
                        }
                        if (locationManager != null) {
                            try {
                                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            }
                            catch (SecurityException ex) {
                                ex.printStackTrace();
                            }
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                altitude = location.getAltitude();
                            }
                        }
                    }
                }
            }
            else canGetLocation=false;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return canGetLocation;
    }

    public double getLatitude() {
        if (location != null) latitude = location.getLatitude();
        return latitude;
    }
    public double getLongitude() {
        if (location != null) longitude = location.getLongitude();
        return longitude;
    }
    /**
     * Function to check GPS/Wi-Fi enabled
     *
     * @return boolean
     */

    public double getAltitude() {
        if (location != null) altitude = location.getAltitude();
        return altitude;
    }
    /**
     * Function to show settings alert dialog.
     * On pressing the Settings button it will launch Settings Options.
     */
    @Override
    public void onLocationChanged(Location location) {
    }
    @Override
    public void onProviderDisabled(String provider) {
    }
    @Override
    public void onProviderEnabled(String provider) {
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}