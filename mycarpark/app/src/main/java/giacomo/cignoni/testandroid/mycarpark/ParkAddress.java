package giacomo.cignoni.testandroid.mycarpark;

public class ParkAddress {
    private double latitude;
    private double longitude;
    private String locality;
    private String thoroughfare;
    private String subThoroughfare;

    ParkAddress(double latitude, double longitude, String locality, String thoroughfare, String subThoroughfare){
        this.latitude = latitude;
        this.longitude = longitude;
        this.locality = locality;
        this.thoroughfare = thoroughfare;
        this.subThoroughfare = subThoroughfare;
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

    public String getSubThoroughfare() {
        return subThoroughfare;
    }

    public void setSubThoroughfare(String subThoroughfare) {
        this.subThoroughfare = subThoroughfare;
    }
}
