public class Connection2 {
    private String city1;
    private String city2;
    private double distance;
    private String requiredPermit;

    public Connection2(String city1, String city2, double distance, String requiredPermit) {
        this.city1 = city1;
        this.city2 = city2;
        this.distance = distance;
        this.requiredPermit = requiredPermit.equals("NONE") ? null : requiredPermit;
    }

    public String getCity1() { return city1; }
    public String getCity2() { return city2; }
    public double getDistance() { return distance; }
    public String getRequiredPermit() { return requiredPermit; }
}