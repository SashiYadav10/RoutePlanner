public class City {
    private String name;
    private int heuristic;
    private String availablePermit;

    public City(String name, int heuristic, String availablePermit) {
        this.name = name;
        this.heuristic = heuristic;
        this.availablePermit = availablePermit.equals("NONE") ? null : availablePermit;
    }

    public String getName() {
        return name;
    }

    public int getHeuristic() {
        return heuristic;
    }

    public String getAvailablePermit() {
        return availablePermit;
    }
}

