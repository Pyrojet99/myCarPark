package giacomo.cignoni.testandroid.mycarpark;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
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
    private RecyclerView rvPark;
    private ParkRVAdapter parkAdapter;
    private ParkViewModel parkViewModel;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //init bottom sheet
        initBottomSheet();

        //init parks recycler
        initParkRecyclerView();

        //init top bar
        initTopBar();

        parkViewModel = new ViewModelProvider(this).get(ParkViewModel.class);

        parkViewModel.getAllParks().observe(this, parks -> {
            // Update the cached copy of the words in the adapter.
            parkAdapter.submitList(parks);
        });

        locationManager = new LocationManager(this);

        //init new location button
        initFabAddLocation();



    }

    public void initTopBar(){
        LinearLayout hiddenTopBar = findViewById(R.id.layout_hidden_top_bar);
        CardView cardTopBar = findViewById(R.id.card_top_bar);
        ImageButton expandArrow = findViewById(R.id.button_expand_arrow);
        expandArrow.setOnClickListener(v -> {
            Log.d("mylog", "expandedArrow: cliccato ");

            // If the CardView is already expanded, set its visibility
            //  to gone and change the expand less icon to expand more.
            if (hiddenTopBar.getVisibility() == View.VISIBLE) {

                // The transition of the hiddenView is carried out
                //  by the TransitionManager class.
                // Here we use an object of the AutoTransition
                // Class to create a default transition.
                TransitionManager.beginDelayedTransition(cardTopBar);
                hiddenTopBar.setVisibility(View.GONE);
                //arrow.setImageResource(R.drawable.ic_baseline_expand_more_24);
            }

            // If the CardView is not expanded, set its visibility
            // to visible and change the expand more icon to expand less.
            else {

                TransitionManager.beginDelayedTransition(cardTopBar);
                hiddenTopBar.setVisibility(View.VISIBLE);
                //arrow.setImageResource(R.drawable.ic_baseline_expand_less_24);
            }
        });
    }

    public ParkViewModel getParkViewModel(){
        return parkViewModel;
    }

    private void addNewLocation() {
        Log.d("mylog", "addNewLocation: cliccato ");
        locationManager.setCurrentLocation();
    }

    public void initFabAddLocation(){
        fabAddLocation = findViewById(R.id.fab_add_location);
        fabAddLocation.setOnClickListener(v -> addNewLocation());
    }

    public void initParkRecyclerView(){
        rvPark = findViewById(R.id.recyclerview_park);

        parkAdapter = new ParkRVAdapter(new ParkRVAdapter.ParkDiff());
        rvPark.setAdapter(parkAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rvPark.setLayoutManager(llm);
    }

    private void initBottomSheet() {
        //non necessario! (per ora)
        
        // get the bottom sheet view
        LinearLayout llBottomSheet = findViewById(R.id.bottom_sheet);

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