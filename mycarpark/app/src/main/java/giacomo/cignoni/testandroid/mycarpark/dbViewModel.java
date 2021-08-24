package giacomo.cignoni.testandroid.mycarpark;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

public class dbViewModel extends AndroidViewModel {

    private LiveData<List<Park>> liveParkList;

    private LiveData<List<Car>> liveCarList;
    //LiveData used ONLY at app start to get the current car from DB
    private LiveData<Car> liveInitialCurrentCar;
    //current car kept while the app is running and used for all other operations
    private Car currentCar;

    private CarDao carDao;
    private ParkDao parkDao;


    public dbViewModel(Application application) {
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

    /*
    Getter for liveCarList
     */
    LiveData<List<Car>> getAllCars() {
        return liveCarList;
    }

    /*
    Getter for liveInitialCurrentCar
     */
    LiveData<Car> getLiveInitialCurrentCar() {
        return liveInitialCurrentCar;
    }

    /*
    Insert car in the DB
     */
    public void insertCar(Car c) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                carDao.insert(c)
        );
    }

    /*
    Update isCurrent field in Car with specified boolean value
     */
    public void updateIsCurrentCar(long carId, boolean isCurrent) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                carDao.updateIsCurrent(carId, isCurrent)
        );
    }

    /*
    Update liveParkList with parks from new car
     */
    public void updateParksByCurrentCarId(long newCarId){
        liveParkList = parkDao.getAllByCarId(newCarId);
    }

    /*
    Getter for liveParkList
     */
    public LiveData<List<Park>> getCurrentCarParks() {
        return liveParkList;
    }

    /*
    Insert new park in DB and dismiss older current parks
     */
    public void insertPark(Park p) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            //sets current time as endTime for previous current parks BEFORE a new park is inserted
            parkDao.dismissAllCurrentParks(p.getParkedCarId(), p.getStartTime());
            //after that, inserts new park into database. The 2 DB operations NEED to be sequential
            parkDao.insert(p);
        });
    }

    /*
    Deletes park form DB
     */
    public void deletePark(Park p) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
           parkDao.delete(p);
        });
    }

    /*
    Sets park alarm time
     */
    public void setParkAlarmTime(Park p, long alarmTime) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                parkDao.setAlarmTime(p.getParkId(), alarmTime)
        );
    }

    /*
    Dismiss a park, with end park time
     */
    public void dismissPark(Park p, Long endTime){
        AppDatabase.databaseWriteExecutor.execute(() ->
                parkDao.dismissPark(p.getParkId(), endTime)
        );
    }



    /*
    currentCar methods
     */
    public Car getCurrentCar() {
        return currentCar;
    }
    public void setCurrentCar(Car currentCar) {
        this.currentCar = currentCar;
    }

}