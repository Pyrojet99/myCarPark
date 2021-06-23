package giacomo.cignoni.testandroid.mycarpark;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ParkDao {
    @Query("SELECT * FROM park WHERE parkedCarId LIKE :carId")
    LiveData<List<Park>> getAllByCarId(Long carId);

    @Query("SELECT * FROM park WHERE parkId LIKE :queryId LIMIT 1")
    Park findById(Long queryId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Park p);

    @Delete
    void delete(Park p);

}
