public class City2 {
    private String name;
    private double latitude;
    private double longitude;
    private String availablePermit;

    public City2(String name, double latitude, double longitude, String availablePermit) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.availablePermit = availablePermit.equals("NONE") ? null : availablePermit;
    }

    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getAvailablePermit() { return availablePermit; }
}
