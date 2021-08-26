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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Car c);

    @Delete
    void delete(Car c);

    @Query("SELECT * FROM car WHERE carId <> :carIdToExclude")
    LiveData<List<Car>> getAllExcept(long carIdToExclude);

    @Query("SELECT * FROM car WHERE carId = :queryId LIMIT 1")
    LiveData<Car> findById(Long queryId);
}
