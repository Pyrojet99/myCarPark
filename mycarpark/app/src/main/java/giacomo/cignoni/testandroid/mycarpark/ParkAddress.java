package giacomo.cignoni.testandroid.mycarpark;

public class ParkAddress {
    double latitude;
    double longitude;
    String locality;
    String thoroughfare;

    ParkAddress(double latitude, double longitude, String locality, String thoroughfare){
        this.latitude = latitude;
        this.longitude = longitude;
        this.locality = locality;
        this.thoroughfare = thoroughfare;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public void setThoroughfare(String thoroughfare) {
        this.thoroughfare = thoroughfare;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getLocality() {
        return locality;
    }

    public String getThoroughfare() {
        return thoroughfare;
    }
}
