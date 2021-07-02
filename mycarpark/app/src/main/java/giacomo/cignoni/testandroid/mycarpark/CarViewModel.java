package giacomo.cignoni.testandroid.mycarpark;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class CarViewModel extends AndroidViewModel {

    private LiveData<List<Car>> liveCarList;
    private LiveData<Car> currentCar;
    private CarDao carDao;

    public CarViewModel (Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        carDao = db.carDao();
        liveCarList = carDao.getAll();
        currentCar = carDao.getCurrent();
    }

    LiveData<List<Car>> getAllCars() { return liveCarList; }

    LiveData<Car> getCurrentCar() { return currentCar; }

    public void insert(Car c) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                carDao.insert(c)
        );
    }

    public void updateIsCurrent(long carId, boolean isCurrent) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                carDao.updateIsCurrent(carId, isCurrent)
        );
    }
}