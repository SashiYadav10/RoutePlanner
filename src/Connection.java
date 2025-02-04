public class Connection {
    private String city1;
    private String city2;
    private int distance;
    private String requiredPermit;

    public Connection(String city1, String city2, int distance, String requiredPermit) {
        this.city1 = city1;
        this.city2 = city2;
        this.distance = distance;
        this.requiredPermit = requiredPermit.equals("NONE") ? null : requiredPermit;
    }

    public String getCity1() {
        return city1;
    }

    public String getCity2() {
        return city2;
    }

    public int getDistance() {
        return distance;
    }

    public String getRequiredPermit() {
        return requiredPermit;
    }
}
