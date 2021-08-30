package giacomo.cignoni.testandroid.mycarpark;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.paging.PagedList;
import androidx.paging.PagingSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ParkDao {

    @Query("SELECT * FROM park WHERE parkedCarId LIKE :carId ORDER BY startTime DESC")
    DataSource.Factory<Integer, Park> getAllByCarId(Long carId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Park p);

    @Query("UPDATE Park SET alarmTime = :alarmTime WHERE parkId LIKE :parkId")
    void setAlarmTime(Long parkId, long alarmTime);

    @Query("UPDATE Park SET endTime = :endTime WHERE parkId LIKE :parkId")
    void dismissPark(Long parkId, long endTime);

    @Query("UPDATE Park SET endTime = :endTime WHERE parkedCarId LIKE :carId AND endTime LIKE 0")
    void dismissAllCurrentParks(Long carId, long endTime);

    @Delete
    void delete(Park p);

    @Query("SELECT * FROM park WHERE alarmTime > 0")
    List<Park> getParksWithAlarm();

    @Query("DELETE FROM Park WHERE parkedCarId LIKE :carId")
    void deleteParksOfSameCar(long carId);
}
