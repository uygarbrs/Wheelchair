package com.example.wheelchair;

import static android.os.Build.*;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.wheelchair.databinding.ActivityMainBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_FINE_LOCATION = 99;
    TextView txt_latitude, txt_longtitude, txt_speed;
    Switch switch_update_loc, switch_save_power;

    boolean updateOn = false;

    LocationRequest locationRequest;
    LocationCallback locationCallBack;
    FusedLocationProviderClient fusedLocationProviderClient;
    ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        txt_latitude = binding.txtLatitude;
        txt_longtitude = binding.txtLongtitude;
        txt_speed = binding.txtSpeed;
        switch_update_loc = binding.switchUpdateLoc;
        switch_save_power = binding.switchSavePower;

        /*
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000 * 30);
        locationRequest.setFastestInterval(1000 * 5);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
         */

        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000 * 10)
                .setMinUpdateIntervalMillis(1000 * 5)
                .build();

        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                updateUI(locationResult.getLastLocation());
            }
        };

        switch_save_power.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switch_save_power.isChecked()){
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                }
                else {
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                }
            }
        });

        switch_update_loc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switch_update_loc.isChecked()){
                    startLocationUpdates();
                }else{
                    stopLocationUpdates();
                }
            }
        });

        updateGPS();
    }

    private void stopLocationUpdates() {
        txt_latitude.setText("Not Tracking");
        txt_longtitude.setText("Not Tracking");
        txt_speed.setText("Not Tracking");

        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }

    private void startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
        updateGPS();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateGPS();
            } else {
                Toast.makeText(this, "This app needs permissions to be granted.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void updateGPS(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    updateUI(location);
                }
            });
        }else{
            if (VERSION.SDK_INT >= VERSION_CODES.M){
                requestPermissions(new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            }
        }
    }

    private void updateUI(Location location){
        txt_latitude.setText(String.valueOf(location.getLatitude()));
        txt_longtitude.setText(String.valueOf(location.getLongitude()));

        if (location.hasSpeed()){
            txt_speed.setText(String.valueOf(location.getSpeed()));
        }else{
            txt_speed.setText("Not Available");
        }
    }
}