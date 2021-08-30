package giacomo.cignoni.testandroid.mycarpark;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import java.util.List;

public class CarsViewModel extends AndroidViewModel {

    //current car kept while the app is running and used for all operations
    //does not necessarily represent current car in database
    private Car currentCar;

    //LiveData used ONLY at app start to get current car ID from DB
    private LiveData<CurrCarId> liveInitialCurrCarId;
    //LiveData used ONLY at app start to get the current car from DB
    private LiveData<Car> liveInitialCurrentCar;

    //list of all the cars which are not the current car
    private LiveData<List<Car>> liveNonCurrCarList;

    private CarDao carDao;
    private CurrCarIdDao currCarIdDao;


    public CarsViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        //get DAOs
        carDao = db.carDao();
        currCarIdDao = db.currCarIdDao();
    }


    public LiveData<CurrCarId> getInitialCurrCarId() {
        return liveInitialCurrCarId;
    }

    public void setInitialCurrCarId() {
        liveInitialCurrCarId = currCarIdDao.getCurrCarId();
    }

    /*
    Getter for liveNonCurrCarList
     */
    public LiveData<List<Car>> getNonCurrCarList() {
        return liveNonCurrCarList;
    }

    /*
    Set the car list to be all the cars from DB except the one with the id specified as param
     */
    public void setNonCurrCarList(long carIdToExclude) {
        liveNonCurrCarList = carDao.getAllExcept(carIdToExclude);
    }

    /*
    Set liveInitialCurrentCar to be the car from DB with the id specified as param
     */
    public void setInitialCurrentCar(long carId) {
        liveInitialCurrentCar = carDao.findById(carId);
    }

    /*
    Getter for liveInitialCurrentCar
     */
    public LiveData<Car> getInitialCurrentCar() {
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
    public void updateCurrentCarId(long carId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            currCarIdDao.deleteAll();
            currCarIdDao.insert(new CurrCarId(carId));
        });
    }

    public void deleteCar(Car car) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            carDao.delete(car);
        });
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