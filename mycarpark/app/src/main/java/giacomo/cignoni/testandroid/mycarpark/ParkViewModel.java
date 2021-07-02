package giacomo.cignoni.testandroid.mycarpark;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class ParkViewModel extends AndroidViewModel {

    private LiveData<List<Park>> liveParkList;
    private ParkDao parkDao;

    public ParkViewModel (Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        parkDao = db.parkDao();
        liveParkList = parkDao.getAll();
    }

    LiveData<List<Park>> getAllParks() { return liveParkList; }

    public void insert(Park p) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                parkDao.insert(p)
        );
    }
}