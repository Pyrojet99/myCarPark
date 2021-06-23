package giacomo.cignoni.testandroid.mycarpark;
import android.location.Address;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Park {
    @PrimaryKey(autoGenerate = true)
    private long parkId;

    private Address address;

    private long parkedCarId;

    public Park(Address address, Long carId){
        this.address = address;
        this.parkedCarId = carId;
    }

    public long getParkId() {
        return parkId;
    }

    public Address getAddress() {
        return address;
    }

    public long getParkedCarId() {
        return parkedCarId;
    }

    public void setParkId(long id) {
        this.parkId = id;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public void setParkedCarId(long carId) {
        this.parkedCarId = carId;
    }
}
