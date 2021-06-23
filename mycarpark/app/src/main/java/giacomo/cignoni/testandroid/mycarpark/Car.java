package giacomo.cignoni.testandroid.mycarpark;
import android.location.Address;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity

public class Car {
    @PrimaryKey(autoGenerate = true)
    private long carId;

    private String name;

    public Car(String name){
        this.name = name;
    }

    public long getCarId() {
        return carId;
    }

    public String getName() {
        return name;
    }

    public void setCarId(long id) {
        this.carId = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
