package giacomo.cignoni.testandroid.mycarpark;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private BottomSheetBehavior bottomSheetBehavior;
    private LocationManager locationManager;
    private FloatingActionButton fabAddLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fabAddLocation = findViewById(R.id.fab_add_location);

        locationManager = new LocationManager(this);


        fabAddLocation.setOnClickListener(v -> addNewLocation());


        //init bottom sheet
        initBottomSheet();

        //init new location button
    }

    private void addNewLocation() {
        Log.d("mylog", "addNewLocation: cliccato ");
        locationManager.getCurrentLocation();

    }

    private void initBottomSheet() {
        //non necessario! (per ora)
        
        // get the bottom sheet view
        LinearLayout llBottomSheet = (LinearLayout) findViewById(R.id.bottom_sheet);

        // init the bottom sheet behavior
        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);

        // change the state of the bottom sheet
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        // set callback for changes
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }
}