package giacomo.cignoni.testandroid.mycarpark;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import java.util.List;

public class ParksViewModel extends AndroidViewModel {
    //list of parks for the current park
    private LiveData<PagedList<Park>> liveParkList;

    private ParkDao parkDao;

    public ParksViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        //get DAO
        parkDao = db.parkDao();
    }

    /*
   Update liveParkList with parks from new car
   Gets dataSource and builds it into LiveData to be observable
    */
    public void updateParkListByCurrentCarId(long newCarId) {
        DataSource.Factory<Integer, Park> factory = parkDao.getAllByCarId(newCarId);
        liveParkList = new LivePagedListBuilder(factory, 10).build();
    }

    /*
    Getter for liveParkList
     */
    public LiveData<PagedList<Park>> getParkList() {
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
        AppDatabase.databaseWriteExecutor.execute(() -> parkDao.delete(p));
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
    Deletes all parks with same parkedCarId
     */
    public void deleteParksOfSameCar(long carId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            parkDao.deleteParksOfSameCar(carId);
        });
    }
}
