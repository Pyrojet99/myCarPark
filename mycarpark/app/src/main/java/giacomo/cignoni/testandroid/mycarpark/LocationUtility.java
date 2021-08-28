package giacomo.cignoni.testandroid.mycarpark;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocationUtility {
    private MainActivity mainActivity;
    private FusedLocationProviderClient fusedLocationClient;
    private Geocoder geocoder;
    static final ExecutorService geocodeExecutor =
            Executors.newFixedThreadPool(2);

    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher, as an instance variable.
    private ActivityResultLauncher<String> requestPermissionLauncher;


    public LocationUtility(MainActivity ma){
        this.mainActivity = ma;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(mainActivity);
        geocoder = new Geocoder(mainActivity);

        requestPermissionLauncher =
        mainActivity.registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                // permission is granted
                //start again setCurrentLocation
                getCurrentLocation();

            } else {
                // Explain to the user that the feature is unavailable
                Snackbar.make(ma.getCoordinatorLayout(), R.string.alert_no_location_perm,
                        BaseTransientBottomBar.LENGTH_LONG).show();
            }
        });
    }

    /*
    Tries to make a request to get current location, checks for location permissions.
    If succeeds calls reverseGeocode on the location
     */
    public void getCurrentLocation() {
        //checks for permission and requests it in case
        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //if permission missing, launch dialog
            requestPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }

        //makes single location request
        Task<Location> locationTask = fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null);
        locationTask.addOnCompleteListener(task -> {
            Location location = task.getResult();

            reverseGeocode(location.getLatitude(), location.getLongitude());

        });
        locationTask.addOnFailureListener(task -> {
            Snackbar.make(mainActivity.getCoordinatorLayout(), R.string.alert_unable_to_get_loc,
                    BaseTransientBottomBar.LENGTH_LONG).show();
        });
    }

    /*
    Tries to reverse geocode the location passed from getCurrentLocation.
    Then creates parkAddres object and calls generateAndInsertPark
     */
    public void reverseGeocode(double latitude, double longitude) {
        geocodeExecutor.execute(() -> {
            Address addr;
            List<Address> listAddresses = null;
            try {
                listAddresses = geocoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(listAddresses == null || listAddresses.size() == 0){
                addr = null;
            }
            else{
                addr = listAddresses.get(0);
            }

            mainActivity.runOnUiThread(() -> {
                ParkAddress pAddr;
                if(addr != null) {
                    //create new ParkAddress
                    pAddr = new ParkAddress(latitude, longitude,
                            addr.getLocality(), addr.getThoroughfare(), addr.getSubThoroughfare());
                }
                else {
                    //create ParkAddress with only latitude and longitude
                    pAddr = new ParkAddress(latitude, longitude,
                            null, null, null);

                }
                //generate park and insert in DB
                mainActivity.generateAndInsertPark(pAddr);
            });
        });
    }
}
