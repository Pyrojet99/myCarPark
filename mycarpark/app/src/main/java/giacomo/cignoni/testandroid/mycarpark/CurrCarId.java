package giacomo.cignoni.testandroid.mycarpark;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class CurrCarId {

    @PrimaryKey
    private int id;
    private long carId;

    public CurrCarId(long carId) {
        this.carId = carId;
        id = 1;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getCarId() {
        return carId;
    }

    public void setCarId(long carId) {
        this.carId = carId;
    }

}
