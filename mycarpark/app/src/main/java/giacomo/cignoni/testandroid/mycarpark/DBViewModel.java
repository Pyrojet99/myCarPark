package giacomo.cignoni.testandroid.mycarpark;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class DBViewModel extends AndroidViewModel {

    private LiveData<List<Park>> liveParkList;
    private LiveData<List<Car>> liveCarList;
    private LiveData<Car> liveInitialCurrentCar;

    private Car CurrentCar;

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

    public Car getCurrentCar() {
        return CurrentCar;
    }

    public void setCurrentCar(Car currentCar) {
        CurrentCar = currentCar;
    }

    public void dismissPark(Park p, Long endTime){
        AppDatabase.databaseWriteExecutor.execute(() ->
                parkDao.dismissPark(p.getParkId(), endTime)
        );
    }

    public void dismissAllCurrentParks(Car currentCar, long endTime){
        AppDatabase.databaseWriteExecutor.execute(() ->
                parkDao.dismissAllCurrentParks(currentCar.getCarId(), endTime)
        );
    }

}