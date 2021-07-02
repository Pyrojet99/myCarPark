package giacomo.cignoni.testandroid.mycarpark;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class CarViewModel extends AndroidViewModel {

    private LiveData<List<Car>> liveCarListe;
    private CarDao carDao;

    public CarViewModel (Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        carDao = db.carDao();
        liveCarListe = carDao.getAll();
    }

    LiveData<List<Car>> getAllCars() { return liveCarListe; }

    public void insert(Car c) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                carDao.insert(c)
        );
    }
}