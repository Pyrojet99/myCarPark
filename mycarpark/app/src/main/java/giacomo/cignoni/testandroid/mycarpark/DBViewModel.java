package giacomo.cignoni.testandroid.mycarpark;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.Objects;

public class DBViewModel extends AndroidViewModel {

    private LiveData<List<Park>> liveParkList;
    private LiveData<List<Car>> liveCarList;
    private LiveData<Car> liveCurrentCar;

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
        liveCurrentCar = carDao.getCurrent();

        Car currCar = liveCurrentCar.getValue();
        if(currCar != null) {
            liveParkList = parkDao.getAllByCarId(currCar.getCarId());
        }
    }

    LiveData<List<Car>> getAllCars() { return liveCarList; }

    LiveData<Car> getCurrentCar() { return liveCurrentCar; }

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


    public void updateParksByCurrentCarId(long newCarId){
        liveParkList = parkDao.getAllByCarId(newCarId);
    }

    public void insertPark(Park p) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                parkDao.insert(p)
        );
    }

}