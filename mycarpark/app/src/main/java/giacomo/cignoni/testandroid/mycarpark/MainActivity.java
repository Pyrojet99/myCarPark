package giacomo.cignoni.testandroid.mycarpark;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {
    private CoordinatorLayout  coordinatorLayout;
    private BottomSheetBehavior bottomSheetBehavior;
    private LocationManager locationManager;
    private FloatingActionButton fabAddLocation;
    private TextView textViewCurrCar;

    private RecyclerView rvPark;
    private ParkRVAdapter parkAdapter;
    private RecyclerView rvCar;
    private CarRVAdapter carAdapter;
    private DBViewModel DBViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //base coordinator layout
        coordinatorLayout = findViewById(R.id.coordinator_layout_base);

        //init bottom sheet
        initBottomSheet();

        //init parks recycler
        initParkRecyclerView();

        //init top bar
        initTopBar();

        //init viewModel
        DBViewModel = new ViewModelProvider(this).get(DBViewModel.class);

        DBViewModel.getAllCars().observe(this, cars -> {
            // Update the cached copy of the cars in the adapter
            carAdapter.submitList(cars);
        });


        MainActivity ma = this;

        Car currentCar;
        //field currentCar has been preserved in the viewModel
        if((currentCar = DBViewModel.getCurrentCar()) != null){
            // Update top textview with car name
            textViewCurrCar.setText(currentCar.getName());
            //initialize parks for current car
            DBViewModel.updateParksByCurrentCarId(currentCar.getCarId());
            //init parks observer
            DBViewModel.getCurrentCarParks().observe(ma, parks -> {
                // Update the cached copy of the parks in the adapter
                parkAdapter.submitList(parks);
            });
        }

        //get current car from DB
        else if (DBViewModel.getLiveInitialCurrentCar() != null) {
            //starting currCar observer
            DBViewModel.getLiveInitialCurrentCar().observe(ma, car -> {
                if (car != null) {
                    //initializes currentCar in viewModel
                    DBViewModel.setCurrentCar(car);
                    // Update top textview with car name
                    textViewCurrCar.setText(car.getName());
                    //initialize parks for current car
                    DBViewModel.updateParksByCurrentCarId(car.getCarId());
                    //init parks observer
                    DBViewModel.getCurrentCarParks().observe(ma, parks -> {
                        // Update the cached copy of the parks in the adapter
                        parkAdapter.submitList(parks);
                    });

                    //remove observers for LiveData of initialCurrentCar
                    DBViewModel.getLiveInitialCurrentCar().removeObservers(ma);
                    //DBViewModel.resetInitialCurrentCar();
                }
            });
        }


        locationManager = new LocationManager(this);

        //init new location button
        initFabAddLocation();

        initMap();
    }

    public void switchCar(Car newSelectedCar){
        // Update top textview with car name
        textViewCurrCar.setText(newSelectedCar.getName());

        //remove observers from oldparks liveData
        DBViewModel.getCurrentCarParks().removeObservers(this);
        //updates parks liveData with parks for new current car
        DBViewModel.updateParksByCurrentCarId(newSelectedCar.getCarId());
        //set observer for new parks livedata
        DBViewModel.getCurrentCarParks().observe(this, parks -> {
            // Update the cached copy of the parks in the adapter
            parkAdapter.submitList(parks);
        });

        //set isCurrent as true for newly selected car
        DBViewModel.updateIsCurrentCar(newSelectedCar.getCarId(), true);
        //set previous curr car isCurrent as false
        DBViewModel.updateIsCurrentCar(DBViewModel.getCurrentCar().getCarId(), false);

        //sets currentCar in viewModel as the selected car
        DBViewModel.setCurrentCar(newSelectedCar);
    }

    public void addNewCar(EditText editAddCar){
        String carName = editAddCar.getText().toString();
        Log.d("mytag", "new car: "+ carName);
        if(!carName.trim().equals("")){
            //new car set as current
            Car c = new Car(carName, false);
            DBViewModel.insertCar(c);

            //CONSIDER: switch to new car? need observer for currCar to get carId generated by DB
            //switchCar(c);
        }
        else{
            //TODO: make toast or other
            Log.d("mytag", "invalid car name");
            Snackbar.make(coordinatorLayout, "use a non-void car name",
                    BaseTransientBottomBar.LENGTH_SHORT).show();
        }
        //reset input text
        editAddCar.setText("");
        //close keyboard
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editAddCar.getWindowToken(), 0);
        //remove focus form edit text
        editAddCar.clearFocus();
    }

    public void initTopBar(){
        LinearLayout hiddenTopBar = findViewById(R.id.layout_hidden_top_bar);
        CardView cardTopBar = findViewById(R.id.card_top_bar);
        ImageButton expandArrow = findViewById(R.id.button_expand_arrow);
        textViewCurrCar = findViewById(R.id.textview_curr_car);

        //init add car
        EditText editAddCar = findViewById(R.id.editText_add_car);
        ImageButton buttonAddCar = findViewById(R.id.button_add_car);

        //onclick listener for + button
        buttonAddCar.setOnClickListener(v -> {
            addNewCar(editAddCar);
        });

        //listener for done button on keyboard
        editAddCar.setOnEditorActionListener( (v, actionId, event) -> {
            addNewCar(editAddCar);
            return true;
        });

        //init cars recycler
        initCarRecyclerView();

        expandArrow.setOnClickListener(v -> {
            Log.d("mytag", "expandedArrow: cliccato ");
            //TODO: close keyboard if open

            // If the CardView is already expanded, set its visibility
            //  to gone and change the expand less icon to expand more.
            if (hiddenTopBar.getVisibility() == View.VISIBLE) {

                // The transition of the hiddenView is carried out
                //  by the TransitionManager class.
                // Here we use an object of the AutoTransition
                // Class to create a default transition.
                TransitionManager.beginDelayedTransition(cardTopBar, new AutoTransition());
                hiddenTopBar.setVisibility(View.GONE);
                //arrow.setImageResource(R.drawable.ic_baseline_expand_more_24);
            }

            // If the CardView is not expanded, set its visibility
            // to visible and change the expand more icon to expand less.
            else {

                TransitionManager.beginDelayedTransition(cardTopBar, new AutoTransition());
                hiddenTopBar.setVisibility(View.VISIBLE);
                //arrow.setImageResource(R.drawable.ic_baseline_expand_less_24);
            }
        });
    }

    public void initMap(){
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_main);
        mapFragment.getMapAsync(googleMap -> {
            // set map type
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            //Disable zoom controls
            googleMap.getUiSettings().setZoomControlsEnabled(false);

            // Enable compass icon - already enabled
            //googleMap.getUiSettings().setCompassEnabled(true);
            // Enable Rotate gesture - already enabled
            //googleMap.getUiSettings().setRotateGesturesEnabled(true);
            // Enable zoom gestures - already enebled
            //googleMap.getUiSettings().setZoomGesturesEnabled(true);
            // Enable scroll gestures - already enebled
            //googleMap.getUiSettings().setScrollGesturesEnabled(true);

            googleMap.getUiSettings().setMapToolbarEnabled(true);

        });
    }

    public DBViewModel getDBViewModel(){
        return DBViewModel;
    }

    public long getCurrentCarId() {
        //return currentCarId;
        return DBViewModel.getCurrentCar().getCarId();
    }

    private void addNewLocation() {
        Log.d("mytag", "addNewLocation: cliccato ");
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

    public void initCarRecyclerView(){
        rvCar = findViewById(R.id.recyclerview_car);

        carAdapter = new CarRVAdapter(new CarRVAdapter.CarDiff(), this);
        rvCar.setAdapter(carAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rvCar.setLayoutManager(llm);
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