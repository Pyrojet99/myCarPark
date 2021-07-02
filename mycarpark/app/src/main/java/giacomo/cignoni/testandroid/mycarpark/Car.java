package giacomo.cignoni.testandroid.mycarpark;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity

public class Car {
    @PrimaryKey(autoGenerate = true)
    private long carId;
    private String name;
    private boolean isCurrent;

    public Car(String name, boolean isCurrent){
        this.name = name;
        this.isCurrent = isCurrent;
    }

    public long getCarId() {
        return carId;
    }

    public String getName() {
        return name;
    }

    public boolean getIsCurrent() {
        return isCurrent;
    }

    public void setCarId(long carId) {
        this.carId = carId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIsCurrent(boolean current) {
        isCurrent = current;
    }
}
