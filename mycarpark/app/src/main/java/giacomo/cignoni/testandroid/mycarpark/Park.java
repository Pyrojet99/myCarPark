package giacomo.cignoni.testandroid.mycarpark;

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
    private long startTime;
    private long endTime;
    private long alarmTime;

    public Park(ParkAddress address, long parkedCarId, long startTime){
        this.address = address;
        this.parkedCarId = parkedCarId;
        this.startTime = startTime;
        this.alarmTime = 0;
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

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public void setAlarmTime(long alarmTime) {
        this.alarmTime = alarmTime;
    }

    public long getAlarmTime() {
        return alarmTime;
    }
}
