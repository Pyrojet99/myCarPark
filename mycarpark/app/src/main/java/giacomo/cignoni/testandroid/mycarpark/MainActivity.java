package giacomo.cignoni.testandroid.mycarpark;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private CoordinatorLayout coordinatorLayout;
    private BottomSheetBehavior bottomSheetBehavior;
    private ExtendedFloatingActionButton fabAddLocation;
    private TextView textViewCurrCar;
    private LinearLayout hiddenTopBar;
    private CardView cardTopBar;
    private ImageButton topExpandArrow;

    private LocationUtility locationUtility;
    private AlarmUtility alarmUtility;
    private MapUtility mapUtility;

    private RecyclerView rvPark;
    private ParkRVAdapter parkAdapter;
    private RecyclerView rvCar;
    private CarRVAdapter carAdapter;
    private DbViewModel dbViewModel;

    private boolean newParkEnabled;

    @Override
    protected void onStop() {
        super.onStop();
        //saves current car id in DB
        dbViewModel.updateCurrentCarId(dbViewModel.getCurrentCar().getCarId());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        long carIdFromIntent = getIntent().getLongExtra(getString(R.string.start_car_extra_intent), 0);


        //base coordinator layout
        coordinatorLayout = findViewById(R.id.coordinator_layout_base);

        //initializes locationUtility
        locationUtility = new LocationUtility(this);

        //initializes alarmUtility
        alarmUtility = new AlarmUtility(this);

        //initializes mapUtility
        mapUtility = new MapUtility(this);

        //init bottom sheet
        initBottomSheet();

        //init parks recycler
        initParkRecyclerView();

        //init top bar
        initTopBar();


        final MainActivity ma = this;
        Car currentCar;

        //init viewModel
        dbViewModel = new ViewModelProvider(this).get(DbViewModel.class);

        //use last used current car, no car id in starting intent
        if (carIdFromIntent == 0) {

            //field currentCar has been preserved in the viewModel
            if ((currentCar = dbViewModel.getCurrentCar()) != null) {
                // Update top textview with car name
                textViewCurrCar.setText(currentCar.getName());
                //initialize parks for current car
                dbViewModel.updateParkListByCurrentCarId(currentCar.getCarId());
                //init parks observer
                ma.observeParks();

                //exclude current car from car list
                dbViewModel.setNonCurrCarList(currentCar.getCarId());
                //sets observer for non current cars
                observeNonCurrCars();


            }
            //get current car from DB
            else {
                dbViewModel.setInitialCurrCarId();
                dbViewModel.getInitialCurrCarId().observe(ma, id -> {
                    if (id != null) {
                        if(id.getCarId() == 0) {
                            //initial state of DB, no curr car
                            disableNewParksInsertion();
                        }
                        //remove observers for LiveData of initialCurrCarId
                        dbViewModel.getInitialCurrCarId().removeObservers(ma);

                        //exclude current car from car list
                        dbViewModel.setNonCurrCarList(id.getCarId());
                        //sets observer for non current cars
                        observeNonCurrCars();

                        //initialize and set observer for LiveData of initialCurrentCar with id
                        dbViewModel.setInitialCurrentCar(id.getCarId());
                        dbViewModel.getInitialCurrentCar().observe(ma, car -> {
                            if (car != null) {
                                //initializes currentCar in viewModel
                                dbViewModel.setCurrentCar(car);
                                // Update top textview with car name
                                textViewCurrCar.setText(car.getName());
                                //initialize parks for current car
                                dbViewModel.updateParkListByCurrentCarId(car.getCarId());
                                //init parks observer
                                ma.observeParks();

                                //remove observers for LiveData of initialCurrentCar
                                dbViewModel.getInitialCurrentCar().removeObservers(ma);
                            }
                        });
                    }
                });
            }
        }
        //get initial car id from intent
        else {
            //exclude current car from car list
            dbViewModel.setNonCurrCarList(carIdFromIntent);
            //sets observer for non current cars
            observeNonCurrCars();

            //initialize and set observer for LiveData of initialCurrentCar with id from intent
            dbViewModel.setInitialCurrentCar(carIdFromIntent);
            dbViewModel.getInitialCurrentCar().observe(ma, car -> {
                if (car != null) {
                    //initializes currentCar in viewModel
                    dbViewModel.setCurrentCar(car);
                    // Update top textview with car name
                    textViewCurrCar.setText(car.getName());
                    //initialize parks for current car
                    dbViewModel.updateParkListByCurrentCarId(car.getCarId());
                    //init parks observer
                    ma.observeParks();

                    //remove observers for LiveData of initialCurrentCar
                    dbViewModel.getInitialCurrentCar().removeObservers(ma);
                }
            });
        }

        //init new location button
        initFabAddLocation();

        mapUtility.restoreMarkers();
    }

    /*
    Observes nonCurrCarList in the viewModel and submits changes to carAdapter
     */
    public void observeNonCurrCars() {
        dbViewModel.getNonCurrCarList().observe(this, cars -> {
            if (cars != null) {
                //update the cached copy of the cars in the adapter
                carAdapter.submitList(cars);
            }
        });
    }

    /*
    Observes parkList in the viewModel and submits changes to parkAdapter
     */
    public void observeParks() {
        dbViewModel.getParkList().observe(this, parks -> {
            //update the cached copy of the parks in the adapter
            parkAdapter.submitList(parks);
        });
    }

    public void switchCar(Car newSelectedCar) {
        if(!newParkEnabled) enableNewParksInsertion();

        // Update top textview with car name
        textViewCurrCar.setText(newSelectedCar.getName());

        //remove previous cars observers
        dbViewModel.getNonCurrCarList().removeObservers(this);
        //updates live car list to exclude new current car
        dbViewModel.setNonCurrCarList(newSelectedCar.getCarId());
        observeNonCurrCars();

        if(dbViewModel.getParkList() != null) {
            //remove observers from oldparks liveData
            dbViewModel.getParkList().removeObservers(this);
        }
        //updates parks liveData with parks for new current car
        dbViewModel.updateParkListByCurrentCarId(newSelectedCar.getCarId());
        //set observer for new parks livedata
        observeParks();


        //sets currentCar in viewModel as the selected car
        dbViewModel.setCurrentCar(newSelectedCar);

        //resets map markers
        mapUtility.removeAllMarkers();

        //closes top bar
        this.toggleExpandTopBar();
    }

    /*
    Add new car
     */
    public void addNewCar(EditText editAddCar) {
        String carName = editAddCar.getText().toString();
        if (!carName.trim().equals("")) {
            //new car set as current
            Car c = new Car(carName);
            dbViewModel.insertCar(c);

        } else {
            Snackbar.make(coordinatorLayout, R.string.alert_invalid_car_name,
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

    public void initTopBar() {
        hiddenTopBar = findViewById(R.id.layout_hidden_top_bar);
        cardTopBar = findViewById(R.id.card_top_bar);
        topExpandArrow = findViewById(R.id.button_expand_arrow);
        textViewCurrCar = findViewById(R.id.textview_curr_car);

        //init add car
        EditText editAddCar = findViewById(R.id.editText_add_car);
        ImageButton buttonAddCar = findViewById(R.id.button_add_car);

        //onclick listener for + button
        buttonAddCar.setOnClickListener(v -> addNewCar(editAddCar));

        //listener for done button on keyboard
        editAddCar.setOnEditorActionListener((v, actionId, event) -> {
            addNewCar(editAddCar);
            return true;
        });

        //init cars recycler
        initCarRecyclerView();

        topExpandArrow.setOnClickListener(v -> this.toggleExpandTopBar());
    }

    public void toggleExpandTopBar() {
        // if the CardView is already expanded, set its visibility
        // to gone and change the expand less icon to expand more
        if (hiddenTopBar.getVisibility() == View.VISIBLE) {
            //collapse bar

            //create autotransition
            TransitionManager.beginDelayedTransition(cardTopBar, new AutoTransition());
            hiddenTopBar.setVisibility(View.GONE);
            topExpandArrow.setImageResource(R.drawable.ic_baseline_expand_more_24);
        }
        else {
            //expand bar
            TransitionManager.beginDelayedTransition(cardTopBar, new AutoTransition());
            hiddenTopBar.setVisibility(View.VISIBLE);
            topExpandArrow.setImageResource(R.drawable.ic_baseline_expand_less_24);
        }
    }

    public DbViewModel getDBViewModel(){
        return dbViewModel;
    }

    public AlarmUtility getAlarmUtility() {
        return this.alarmUtility;
    }

    public LocationUtility getLocationUtility() {
        return this.locationUtility;
    }

    public MapUtility getMapUtility() {
        return this.mapUtility;
    }

    public boolean getNewParkEnabled() {
        return newParkEnabled;
    }

    public CoordinatorLayout getCoordinatorLayout() {
        return coordinatorLayout;
    }

    public BottomSheetBehavior getBottomSheetBehavior() {
        return bottomSheetBehavior;
    }


    /*
    Return currentCarId from viewModel
     */
    public long getCurrentCarId() {
        return dbViewModel.getCurrentCar().getCarId();
    }

    public void disableNewParksInsertion() {
        this.newParkEnabled = false;
        fabAddLocation.setVisibility(View.GONE);
    }

    public void enableNewParksInsertion() {
        this.newParkEnabled = true;
        fabAddLocation.setVisibility(View.VISIBLE);
    }

    /*
    Get a new location, if successful insert into DB
     */
    public void addNewLocation() {
        locationUtility.getCurrentLocation();
    }

    /*
    Create and insert new park in the database
     */
    public void generateAndInsertPark(ParkAddress addr) {
        //get current time in millis
        long currentTime = Calendar.getInstance().getTimeInMillis();
        //updates current markers present on map to old markers
        mapUtility.setPreviusCurrMarkersToOldMarkers();

        //create new park
        Park p = new Park(addr, this.getCurrentCarId(), currentTime);
        //insert in database
        dbViewModel.insertPark(p);

        //haptic feedback of confirmation
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            this.fabAddLocation.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        }
    }

    /*
    Deletes park from DB and its marker on the map if present
     */
    public void deletePark(Park p) {
        dbViewModel.deletePark(p);
        mapUtility.removeMarker(p.getParkId());
        alarmUtility.removeAlarm(p);
    }

    public void initFabAddLocation(){
        fabAddLocation = findViewById(R.id.fab_add_location);
        fabAddLocation.setOnClickListener(v -> addNewLocation());
    }

    public void initParkRecyclerView(){
        rvPark = findViewById(R.id.recyclerview_park);

        parkAdapter = new ParkRVAdapter(new ParkRVAdapter.ParkDiff(), this);
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
        // get the bottom sheet view
        LinearLayout llBottomSheet = findViewById(R.id.bottom_sheet);
        // init the bottom sheet behavior
        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
        // change the state of the bottom sheet
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    /*
    Set endtime for current park, so it becomes an old park
     */
    public void dismissPark(Park park){
        //get current time in millis
        long endTime = Calendar.getInstance().getTimeInMillis();
        dbViewModel.dismissPark(park, endTime);
        dbViewModel.setParkAlarmTime(park, 0);

        //change current park marker to old park marker
        mapUtility.setPreviusCurrMarkersToOldMarkers();
        //removes alarmsin case is present
        alarmUtility.removeAlarm(park);
    }


    /*
    Unfocus editText and closes keyboard when clicking outside editText
    https://stackoverflow.com/questions/4828636/edittext-clear-focus-on-touch-outside
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }
}