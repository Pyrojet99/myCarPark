package giacomo.cignoni.testandroid.mycarpark;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import java.util.List;

public class DbViewModel extends AndroidViewModel {

    //current car kept while the app is running and used for all operations
    //does not necessarily represent current car in database
    private Car currentCar;

    //LiveData used ONLY at app start to get current car ID from DB
    private LiveData<CurrCarId> liveInitialCurrCarId;
    //LiveData used ONLY at app start to get the current car from DB
    private LiveData<Car> liveInitialCurrentCar;

    //list of all the cars which are not the current car
    private LiveData<List<Car>> liveNonCurrCarList;

    //list of parks for the current park
    private LiveData<PagedList<Park>> liveParkList;

    private CarDao carDao;
    private ParkDao parkDao;
    private CurrCarIdDao currCarIdDao;


    public DbViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        //get DAOs
        parkDao = db.parkDao();
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
    currentCar methods
     */
    public Car getCurrentCar() {
        return currentCar;
    }
    public void setCurrentCar(Car currentCar) {
        this.currentCar = currentCar;
    }

}