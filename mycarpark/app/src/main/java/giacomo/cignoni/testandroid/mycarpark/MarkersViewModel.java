package giacomo.cignoni.testandroid.mycarpark;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;

import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

public class MarkersViewModel extends AndroidViewModel {
    //map with <parkId, MarkerOptions> couples
    private Map<Long, MarkerOptions> markerOptionsMap;
    //map with <parkId, Boolean> couples, Bool==true if corresponding marker is of current park (colored marker)
    private Map <Long, Boolean> markerIsCurrParkMap;

    public MarkersViewModel(Application application) {
        super(application);

        markerOptionsMap = new HashMap<>();
        markerIsCurrParkMap = new HashMap<>();
    }


    /*
    markerOptionsMap methods
     */
    public void putMarkerOptions(long parkId, MarkerOptions m){
        markerOptionsMap.put(parkId, m);
    }

    public MarkerOptions getMarkerOptions(long parkId) {
        return markerOptionsMap.get(parkId);
    }

    public Map<Long, MarkerOptions> getMarkerOptionsMap(){
        return markerOptionsMap;
    }

    public void removeMarkerOptions(long parkId){
        markerOptionsMap.remove(parkId);
    }

    public  void resetMarkerOptionsMap(){
        markerOptionsMap.clear();
    }


    /*
    markerIsCurrParkMap methods
     */
    public void putMarkerIsCurr(long parkId, Boolean b){
        markerIsCurrParkMap.put(parkId, b);
    }

    public Boolean getMarkerIsCurr(long parkId) {
        return markerIsCurrParkMap.get(parkId);
    }

    public void removeMarkerIsCurr(long parkId){
        markerIsCurrParkMap.remove(parkId);
    }

    public  void resetMarkerIsCurrMap(){
        markerIsCurrParkMap.clear();
    }
}
