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
    @Query("SELECT * FROM car")
    LiveData<List<Car>> getAll();

    @Query("SELECT * FROM car WHERE carId LIKE :queryId LIMIT 1")
    Car findById(Long queryId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Car c);

    @Delete
    void delete(Car c);
}
