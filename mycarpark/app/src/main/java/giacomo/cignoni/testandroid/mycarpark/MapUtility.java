package giacomo.cignoni.testandroid.mycarpark;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.HashMap;
import java.util.Map;

public class MapUtility {
    static float DEFAULT_MAP_ZOOM = 17f;


    private MainActivity mainActivity;
    private GoogleMap map;
    // register the permissions callback
    private ActivityResultLauncher<String> requestPermissionLauncher;

    //map with <parkId, MarkerOptions> couples, used for deleting or modifying markers.
    //Item is present in markersMap => item is present in markerOptionsMap in viewModel,
    //but the opposite is not always true.
    private Map<Long, Marker> markersMap;

    private Bitmap bitmapCurrentMarker;
    private Bitmap bitmapOldMarker;

    private MarkersViewModel markersViewModel;

    public MapUtility(MainActivity mainActivity) {
        this.mainActivity = mainActivity;

        //generates bitmaps for map markers
        bitmapCurrentMarker = Utils.generateBitmapFromVector(mainActivity, R.drawable.ic_car_current_24_vect);
        bitmapOldMarker = Utils.generateBitmapFromVector(mainActivity, R.drawable.ic_car_old_24_vect);

        //init markers map
        markersMap = new HashMap<>();
        initMap();
    }

    public void initMap() {
        //initializes requestPermissionLauncher with callback (action to do after permission request dialog)
        requestPermissionLauncher = mainActivity.registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // permission is granted
                        Log.d("mytag", "addNewLocation:  permission granted after dialog");
                        //start again addMapCurrPosition
                        addMapCurrPosition();

                    } else {
                        // Explain to the user that the feature is unavailable
                        Toast.makeText(mainActivity.getApplicationContext(), "location permession not granted after dialog", Toast.LENGTH_SHORT).show();

                    }
                });

        //init viewModel
        markersViewModel = new ViewModelProvider(mainActivity).get(MarkersViewModel.class);

        //load mapFragment
        MapFragment mapFragment = (MapFragment) mainActivity.getFragmentManager().findFragmentById(R.id.map_main);
        mapFragment.getMapAsync(googleMap -> {
            //when map loaded

            this.map = googleMap;
            // set map type
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            //disable zoom controls
            googleMap.getUiSettings().setZoomControlsEnabled(false);
            //sets top padding for map controls
            googleMap.setPadding(0, 256, 0, 0);

            this.addMapCurrPosition();

            //sets longonclick listener to add new current park on long press
            googleMap.setOnMapLongClickListener(latLng -> {
                //if the possibility to add new parks is enabled
                if (mainActivity.getNewParkEnabled()) {
                    mainActivity.getLocationUtility().reverseGeocode(latLng.latitude, latLng.longitude);
                }
            });

            googleMap.getUiSettings().setMapToolbarEnabled(true);

            this.restoreMarkers();
        });
    }

    /*
   Checks location permissions to add current location to map
    */
    public void addMapCurrPosition() {
        //checks and requests location permissions
        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("mytag", "addNewLocation: no permission location");
            requestPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }
        this.map.setMyLocationEnabled(true);
    }


    /////////////////////////  MARKERS METHODS //////////////////////////

    /*
    Restores all markers previously preserved in the viewModel
     */
    public void restoreMarkers() {
        Log.d("mytag", "restoring Markers: ");
        for (Map.Entry<Long, MarkerOptions> entry : markersViewModel.getMarkerOptionsMap().entrySet()) {
            //restore marker to map if not already restored (if restored the corresponding value
            //of the entry in the Marker objects map is not null)
            if (this.markersMap.get(entry.getKey()) == null) {
                this.addParkMarker(entry.getValue(), entry.getKey());
                Log.d("mytag", "restored Marker ");
            }
            else {
                Log.d("mytag", "not restored Marker because already is ");

            }
        }
    }

    /*
    Generates MarkerOptions for current park, adds it to markerOptionsMap and calls addParkMarker method
     */
    public void addCurrParkMarker(Park park) {
        if(markersMap.get(park.getParkId()) == null) {
            //create MarkerOptions
            LatLng position = new LatLng(park.getAddress().getLatitude(), park.getAddress().getLongitude());
            MarkerOptions m = new MarkerOptions()
                    .position(position);
            BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(this.bitmapCurrentMarker);
            m.icon(icon);
            //adds MarkerOptions to map data structure to preserve it
            markersViewModel.putMarkerOptions(park.getParkId(), m);
            //adds isCurrent info to map
            markersViewModel.putMarkerIsCurr(park.getParkId(), Boolean.TRUE);

            addParkMarker(m, park.getParkId());
        }
    }

    /*
    Generates MarkerOptions for old park, adds it to markerOptionsMap and calls addParkMarker method
    */
    public void addOldParkMarker(Park park) {
        if(markersMap.get(park.getParkId()) == null) {
            //create MarkerOptions
            LatLng position = new LatLng(park.getAddress().getLatitude(), park.getAddress().getLongitude());
            MarkerOptions m = new MarkerOptions()
                    .position(position);
            BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(this.bitmapOldMarker);
            m.icon(icon);
            //adds MarkerOptions to map data structure to preserve it
            markersViewModel.putMarkerOptions(park.getParkId(), m);
            //adds isCurrentMarker info to map
            markersViewModel.putMarkerIsCurr(park.getParkId(), Boolean.FALSE);

            addParkMarker(m, park.getParkId());
        }
        else {
            //if new marker is not created, focus on existing marker
            centerCameraOnMarker(park.getParkId());
        }
    }

    /*
    Add marker to GoogleMap from MarkerOptions and generated Marker Object to markersMap data structure.
    Called both for current and old park markers
     */
    public void addParkMarker(MarkerOptions m, long parkId) {
        Marker marker;
        //adds marker to GoogleMap if already loaded
        if (this.map != null){
            marker = this.map.addMarker(m);

            //adds marker to Map for later deletion or modification
            this.markersMap.put(parkId, marker);
            Log.d("mytag", "addedParkMarker: ");
        }

        //map camera animation on marker focus on marker
        this.centerCameraOnMarker(parkId);

    }

    /*
    Centers map camera on marker corresponding to a parkId if present on the map
     */
    public void centerCameraOnMarker(long parkId) {
        Marker marker;
        if ((marker = this.markersMap.get(parkId)) != null) {
            this.map.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), DEFAULT_MAP_ZOOM));

            //closes bottomSheet to show map
            mainActivity.getBottomSheetBehavior().setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    /*
   Returns if a marker refers to a current park, based on the value of a map in the viewModel
    */
    public boolean isCurrentParkMarker(long parkId) {
        if (markersViewModel.getMarkerIsCurr(parkId)) return true;
        else return false;
    }

    /*
   Updates all current park markers to old park markers.
    */
    public void setPreviusCurrMarkersToOldMarkers() {
        for(Map.Entry<Long, Marker> entry : markersMap.entrySet()) {
            if (isCurrentParkMarker(entry.getKey())) {
                //updates value in isCurrentMarker map of viewModel to false
                markersViewModel.putMarkerIsCurr(entry.getKey(), false);
                //update markerOptions
                MarkerOptions m = markersViewModel.getMarkerOptions(entry.getKey());
                BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(this.bitmapOldMarker);
                m.icon(icon);
                //adds MarkerOptions to map data structure to preserve it
                markersViewModel.putMarkerOptions(entry.getKey(), m);
                //updates Marker
                entry.getValue().setIcon(icon);
            }
        }
    }

    /*
    Remove all markers from the GoogleMap and the maps used to keep track of markers
     */
    public void removeAllMarkers() {
        Log.d("mytag", "removeAllMarkers");
        for(Marker m : this.markersMap.values()) {
            m.remove();
        }
        //reset Markers, MarkerOptions and isCurrentMarker maps
        markersMap.clear();
        markersViewModel.resetMarkerOptionsMap();
        markersViewModel.resetMarkerIsCurrMap();
    }

    /*
    Remove the marker associated to a single park from GoogleMap and data structures
     */
    public void removeMarker(long parkId) {
        Marker marker;
        if((marker = this.markersMap.get(parkId)) != null) marker.remove();
        //remove values with parkId key from Markers, MarkerOptions and isCurrentMarker maps
        this.markersMap.remove(parkId);
        markersViewModel.removeMarkerOptions(parkId);
        markersViewModel.removeMarkerIsCurr(parkId);
    }
}
