package giacomo.cignoni.testandroid.mycarpark;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CarDao {
    //true=1 false=0 in SQLlite

    @Query("SELECT * FROM car WHERE isCurrent LIKE 0")
    LiveData<List<Car>> getAll();

    @Query("SELECT * FROM car WHERE carId LIKE :queryId LIMIT 1")
    Car findById(Long queryId);

    @Query("SELECT * FROM car WHERE isCurrent LIKE 1 LIMIT 1")
    LiveData<Car> getCurrent();

    @Query("UPDATE Car SET isCurrent = :iscurrent WHERE carId LIKE :carid")
    void updateIsCurrent(long carid, boolean iscurrent);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Car c);

    @Delete
    void delete(Car c);
}
