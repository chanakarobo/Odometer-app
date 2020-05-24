package com.example.odometer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private OdometerService odometer;

    //Use this to store whether or not the activity’s bound to the service.
    private boolean bound =false;
    private final int PERMISSION_REQUEST_CODE = 698;

    //this is the bind connection
    private ServiceConnection connection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {

            OdometerService.OdometerBinder odometerBinder=
                    (OdometerService.OdometerBinder)binder;

            //Get a reference to the OdometerService when the service is connected
            odometer=odometerBinder.getOdometer();
            bound=true;

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
             bound=false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        displayDistance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //If permission hasn’t already been granted.
        if(ContextCompat.checkSelfPermission(this,OdometerService.PERMISSION_STRING)//use service permission string
             != PackageManager.PERMISSION_GRANTED){

            //request permission at runtime
            ActivityCompat.requestPermissions(this,
                    new String[]{OdometerService.PERMISSION_STRING},
                    PERMISSION_REQUEST_CODE);
        }else {
            //If permission has already been granted, bind to the service
            Intent intent = new Intent(this, OdometerService.class);
            //Bind the service when the activity starts.
            bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(bound){
            //unbind the service when the activity stops
            unbindService(connection);
            bound=false;
        }
    }

    private void displayDistance(){

        final TextView distanceView=findViewById(R.id.distance);
     //using handlers we can execute codes agan and again
        final Handler handler=new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                double distance=0.0;

                if(bound && odometer !=null){
                    distance=odometer.getDistance();
                }
                String distanceStr = String.format(Locale.getDefault(),
                                                   "%1$,.2f miles", distance);
                distanceView.setText(distanceStr);
                //Update the TextView’s value every second
                handler.postDelayed(this,1000);
            }
        });


    }

// onRequestPermissionsResult returns the results of your permissions requests.
    //requestCode  identify the permissions request
    //int array  for the results of the requests.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
         //check whether the code matches the one we used in our requestPermission method
            case PERMISSION_REQUEST_CODE:{
        //If the request was cancelled, no results will be returned.
              if(grantResults.length>0
                 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                  Intent intent = new Intent(this, OdometerService.class);
                  //Bind the service when the activity starts.
                  bindService(intent, connection, Context.BIND_AUTO_CREATE);
              //display the massege to user for getting permission to accesses location service
              }else{
                  Toast.makeText(MainActivity.this, "permission requred", Toast.LENGTH_SHORT).show();
              }

            }
        }



    }
}
