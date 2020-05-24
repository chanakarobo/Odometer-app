package com.example.odometer;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Binder;

import androidx.core.content.ContextCompat;

import java.util.Random;

public class OdometerService extends Service {

    //all the codes we need to bind the actitivity to bind odometerService

    //we use this listnner to listen location changers
    private LocationListener listener;
    private LocationManager locManager;
    private static double distanceInMeters;
    private final IBinder binder=new  OdometerBinder();

    //We’re using static variables to store the distance traveled and the user’s last location
    // so that their values are retained/keep when the service is destroyed
    private static Location lastLocation = null;

    //We’re adding the permission String as a constant.
    public static final String  PERMISSION_STRING=
            Manifest.permission.ACCESS_FINE_LOCATION;

    @Override
    public void onCreate() {
        super.onCreate();
        //setup the location listner
        listener=new LocationListener() {
           //location variable describe current location
            @Override
            public void onLocationChanged(Location location) {

                if(lastLocation == null){
                    lastLocation=location;// location is current location
                }
                distanceInMeters +=location.distanceTo(lastLocation);
                //Update the distance traveled and the user’s last location
                lastLocation=location;

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
        };
        //get the access to the location service.
        locManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);

        //check wether we have permissoon.
        if(ContextCompat.checkSelfPermission(this,PERMISSION_STRING)
        == PackageManager.PERMISSION_GRANTED){

           //Get the most accurate location provider
            String provider=locManager.getBestProvider(new Criteria(),true);

            // Request updates from the location provider
            if(provider !=null){
                locManager.requestLocationUpdates(provider,1000,1,listener);
            }
        }
    }


    public class OdometerBinder extends Binder{

        OdometerService getOdometer(){

            return OdometerService.this;
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    //end of the binding


    public double getDistance(){
        return this.distanceInMeters / 1609.344;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locManager !=null && listener !=null){

            if(ContextCompat.checkSelfPermission(this,PERMISSION_STRING)
                    == PackageManager.PERMISSION_GRANTED) {

                // stop getting locations updates (if we have permission to remove them)
                locManager.removeUpdates(listener);
            }
            locManager=null;
            listener=null;

        }
    }
}
