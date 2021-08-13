package giacomo.cignoni.testandroid.mycarpark;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBViewModel extends AndroidViewModel {

    private LiveData<List<Park>> liveParkList;
    private LiveData<List<Car>> liveCarList;
    private LiveData<Car> liveInitialCurrentCar;


    private Car currentCar;
    //map with <parkId, MarkerOptions> couples
    private Map<Long, MarkerOptions> markerOptionsMap;
    //map with <parkId, Boolean> couples, Bool==true if corresponding marker is of current park
    private Map <Long, Boolean> markerIsCurrParkMap;

    private CarDao carDao;
    private ParkDao parkDao;


    public DBViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        //get DAOs
        parkDao = db.parkDao();
        carDao = db.carDao();
        //initialize liveData data structures
        liveCarList = carDao.getAll();
        liveInitialCurrentCar = carDao.getCurrent();

        Car currCar = liveInitialCurrentCar.getValue();
        if(currCar != null) {
            liveParkList = parkDao.getAllByCarId(currCar.getCarId());
        }

        markerOptionsMap = new HashMap<>();
        markerIsCurrParkMap = new HashMap<>();
    }

    LiveData<List<Car>> getAllCars() { return liveCarList; }

    LiveData<Car> getLiveInitialCurrentCar() { return liveInitialCurrentCar; }

    public void insertCar(Car c) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                carDao.insert(c)
        );
    }

    //update isCurrent field in Car with specified boolean value
    public void updateIsCurrentCar(long carId, boolean isCurrent) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                carDao.updateIsCurrent(carId, isCurrent)
        );
    }

    public LiveData<List<Park>> getCurrentCarParks() {
        return liveParkList;
    }

    public void resetInitialCurrentCar(){
        liveInitialCurrentCar = null;
    }

    public void updateParksByCurrentCarId(long newCarId){
        liveParkList = parkDao.getAllByCarId(newCarId);
    }

    public void insertPark(Park p) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            //sets current time as endTime for previous current parks BEFORE a new park is inserted
            parkDao.dismissAllCurrentParks(p.getParkedCarId(), p.getStartTime());
            //after that, inserts new park into database. The 2 DB operations NEED to be sequential
            parkDao.insert(p);
        });
    }

    public void deletePark (Park p) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
           parkDao.delete(p);
        });
    }

    public Car getCurrentCar() {
        return currentCar;
    }

    public void setCurrentCar(Car currentCar) {
        this.currentCar = currentCar;
    }

    public void dismissPark(Park p, Long endTime){
        AppDatabase.databaseWriteExecutor.execute(() ->
                parkDao.dismissPark(p.getParkId(), endTime)
        );
    }

    /*
    Add marker to markersList
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
    }}