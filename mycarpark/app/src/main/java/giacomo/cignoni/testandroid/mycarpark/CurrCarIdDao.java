package giacomo.cignoni.testandroid.mycarpark;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CurrCarIdDao {

    @Query("DELETE FROM currCarId")
    void deleteAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CurrCarId c);

    @Query("SELECT * FROM currCarId LIMIT 1")
    LiveData<CurrCarId> getCurrCarId();
}