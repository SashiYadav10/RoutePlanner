import java.util.*;

public class State implements Comparable<State> {
    private String currentCity;
    private Set<String> permits;
    private int costSoFar;
    private int estimatedTotalCost;
    private List<String> path;

    public State(String currentCity, Set<String> permits, int costSoFar, int estimatedTotalCost, List<String> path) {
        this.currentCity = currentCity;
        this.permits = new HashSet<>(permits);
        this.costSoFar = costSoFar;
        this.estimatedTotalCost = estimatedTotalCost;
        this.path = new ArrayList<>(path);
    }

    public String getCurrentCity() {
        return currentCity;
    }

    public Set<String> getPermits() {
        return permits;
    }

    public int getCostSoFar() {
        return costSoFar;
    }

    public int getEstimatedTotalCost() {
        return estimatedTotalCost;
    }

    public List<String> getPath() {
        return path;
    }

    @Override
    public int compareTo(State other) {
        return Integer.compare(this.estimatedTotalCost, other.estimatedTotalCost);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof State) {
            State other = (State) obj;
            return this.currentCity.equals(other.currentCity) && this.permits.equals(other.permits);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentCity, permits);
    }
}

