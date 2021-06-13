package giacomo.cignoni.testandroid.mycarpark;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocationManager {
    private MainActivity mainActivity;
    private FusedLocationProviderClient fusedLocationClient;
    Geocoder geocoder;
    static final ExecutorService geocodeExecutor =
            Executors.newFixedThreadPool(2);

    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher, as an instance variable.
    private ActivityResultLauncher<String> requestPermissionLauncher;


    public LocationManager(MainActivity ma){
        this.mainActivity = ma;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(mainActivity);
        geocoder = new Geocoder(mainActivity);

        requestPermissionLauncher =
        mainActivity.registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                Log.d("mylog", "addNewLocation: no permission granted after dialog");

            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                Toast.makeText(mainActivity.getApplicationContext(), "location permession not granted", Toast.LENGTH_SHORT).show();

            }
        });
    }

    public void getCurrentLocation(){
        //TODO: check settings for location enabled https://developer.android.com/training/location/change-location-settings

        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("mylog", "addNewLocation: no permission location");
            requestPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }

        Task<Location> locationTask = fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null);
        locationTask.addOnCompleteListener(task -> {
            Location location = task.getResult();

            Toast.makeText(mainActivity.getApplicationContext(), "location got", Toast.LENGTH_SHORT).show();
            Log.d("mylog", "addNewLocation: posizione presa "+location.getLatitude()+" "+
                    location.getLongitude());
            reverseGeocode(location);

        });
        //locationTask.addOnFailureListener();
    }

    public void reverseGeocode(Location location){
        geocodeExecutor.execute(() -> {
            Address addr;
            List<Address> listAddresses = null;
            try {
                listAddresses = geocoder.getFromLocation(location.getLatitude(),
                        location.getLongitude(),
                        1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(listAddresses == null || listAddresses.size() == 0){
                addr = null;
            }
            else{
                addr = listAddresses.get(0);
            }
            Log.d("mylog", "addNewLocation: address preso "+addr.getLocality());
            mainActivity.runOnUiThread(() -> {
                 TextView textView = mainActivity.findViewById(R.id.textView1);
                 textView.setText(addr.getLocality()+" "+addr.getThoroughfare());
            });

        });

    }
}
