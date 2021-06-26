package giacomo.cignoni.testandroid.mycarpark;
import android.location.Address;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Park {
    @PrimaryKey(autoGenerate = true)
    private long parkId;
    @Embedded
    private ParkAddress address;

    private long parkedCarId;

    public Park(ParkAddress address, long parkedCarId){
        this.address = address;
        this.parkedCarId = parkedCarId;
    }

    public long getParkId() {
        return parkId;
    }

    public ParkAddress getAddress() {
        return address;
    }

    public long getParkedCarId() {
        return parkedCarId;
    }

    public void setParkId(long id) {
        this.parkId = id;
    }

    public void setAddress(ParkAddress address) {
        this.address = address;
    }

    public void setParkedCarId(long carId) {
        this.parkedCarId = carId;
    }
}
