package giacomo.cignoni.testandroid.mycarpark;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class DBViewModel extends AndroidViewModel {

    private LiveData<List<Park>> liveParkList;
    private LiveData<List<Car>> liveCarList;
    private LiveData<Car> currentCar;

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
        currentCar = carDao.getCurrent();
        liveParkList = parkDao.getAllByCarId(currentCar.getValue().getCarId());
    }

    LiveData<List<Car>> getAllCars() { return liveCarList; }

    LiveData<Car> getCurrentCar() { return currentCar; }

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

    LiveData<List<Park>> getCurrentCarParks() { return liveParkList; }

    public void updateCurrentCarId(long newCarId){
        liveParkList = parkDao.getAllByCarId(newCarId);
    }

    public void insertPark(Park p) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                parkDao.insert(p)
        );
    }

}