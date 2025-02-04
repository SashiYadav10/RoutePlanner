import java.util.ArrayList;
import java.util.List;

public class SearchResult {
    private double optimalCost;
    private int maxFrontierSize;
    private int nodesExpanded;
    private List<String> path;
    private double totalPermitCost;
    List<String> permitsAcquired;

    public SearchResult(double optimalCost, int maxFrontierSize, int nodesExpanded, List<String> path, double totalPermitCost, List<String> permitsAcquired) {
        this.optimalCost = optimalCost;
        this.maxFrontierSize = maxFrontierSize;
        this.nodesExpanded = nodesExpanded;
        this.path = new ArrayList<>(path);
        this.totalPermitCost = totalPermitCost;
        this.permitsAcquired = permitsAcquired; // Initialize
    }

    public double getOptimalCost() { return optimalCost; }
    public int getMaxFrontierSize() { return maxFrontierSize; }
    public int getNodesExpanded() { return nodesExpanded; }
    public List<String> getPath() { return path; }
    public double getTotalPermitCost() { return totalPermitCost; }
    public List<String> getPermitsAcquired() {
        return permitsAcquired;
    }
}
