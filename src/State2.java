import java.util.*;

public class State2 implements Comparable<State2> {
    private String currentCity;
    private Set<String> permits;
    private double costSoFar;
    private double estimatedTotalCost;
    private List<String> path;
    private double totalPermitCost; // New field
    private List<String> permitsAcquired; // New field

    public State2(String currentCity, Set<String> permits, double costSoFar, double estimatedTotalCost, List<String> path, double totalPermitCost, List<String> permitsAcquired ) {
        this.currentCity = currentCity;
        this.permits = new HashSet<>(permits);
        this.costSoFar = costSoFar;
        this.estimatedTotalCost = estimatedTotalCost;
        this.path = new ArrayList<>(path);
        this.totalPermitCost = totalPermitCost;
        this.permitsAcquired = permitsAcquired;
    }

    public String getCurrentCity() { return currentCity; }
    public Set<String> getPermits() { return permits; }
    public double getCostSoFar() { return costSoFar; }
    public double getEstimatedTotalCost() { return estimatedTotalCost; }
    public List<String> getPath() { return path; }
    public double getTotalPermitCost() { return totalPermitCost; }

    @Override
    public int compareTo(State2 other) {
        return Double.compare(this.estimatedTotalCost, other.estimatedTotalCost);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof State2) {
            State2 other = (State2) obj;
            return this.currentCity.equals(other.currentCity) && this.permits.equals(other.permits);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentCity, permits);
    }
    public List<String> getPermitsAcquired() {
        return permitsAcquired;
    }

}

