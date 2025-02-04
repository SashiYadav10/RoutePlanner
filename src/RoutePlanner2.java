import java.io.*;
import java.util.*;

public class RoutePlanner2 {
    private Map<String, City2> cities = new HashMap<>();
    private Map<String, List<Connection2>> graph = new HashMap<>();

    double permitCost = 10.0; // Cost for obtaining the permit

    public void readCities(String filename) throws IOException {
        cities.clear();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        // Skip the header line
        br.readLine();

        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split(";");
            if (parts.length < 4) continue;
            String name = parts[0].trim();
            double latitude = Double.parseDouble(parts[1].trim());
            double longitude = Double.parseDouble(parts[2].trim());
            String availablePermit = parts[3].trim();
            cities.put(name, new City2(name, latitude, longitude, availablePermit));
        }
        br.close();
    }

    public void readConnections(String filename) throws IOException {
        graph.clear();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;

        // Skip the header line
        br.readLine();

        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split(";");
            if (parts.length < 4) continue;
            String city1 = parts[0].trim();
            String city2 = parts[1].trim();
            double distance = Double.parseDouble(parts[2].trim());
            String requiredPermit = parts[3].trim();
            Connection2 connection = new Connection2(city1, city2, distance, requiredPermit);
            graph.computeIfAbsent(city1, k -> new ArrayList<>()).add(connection);
            graph.computeIfAbsent(city2, k -> new ArrayList<>())
                    .add(new Connection2(city2, city1, distance, requiredPermit));
        }
        br.close();
    }

    public List<TestCase> readTestCases(String filename) throws IOException {
        List<TestCase> testCases = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;

        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split(";");
            if (parts.length != 2) continue;
            String startCity = parts[0].trim();
            String goalCity = parts[1].trim();
            testCases.add(new TestCase(startCity, goalCity));
        }
        br.close();
        return testCases;
    }

    public double haversineDistance(City2 city1, City2 city2) {
        final int d = 12742; // Earth's diameter in kilometers
        double lat1 = city1.getLatitude();
        double lon1 = city1.getLongitude();
        double lat2 = city2.getLatitude();
        double lon2 = city2.getLongitude();

        double sinHalfDeltaLat = Math.sin(Math.toRadians(lat2 - lat1) / 2);
        double sinHalfDeltaLon = Math.sin(Math.toRadians(lon2 - lon1) / 2);
        double latARadians = Math.toRadians(lat1);
        double latBRadians = Math.toRadians(lat2);

        double a = sinHalfDeltaLat * sinHalfDeltaLat
                + Math.cos(latARadians) * Math.cos(latBRadians) * sinHalfDeltaLon * sinHalfDeltaLon;

        return d * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public SearchResult aStarSearch(String startCity, String goalCity, double scalingFactor) {

        PriorityQueue<State2> frontier = new PriorityQueue<>();
        Map<String, State2> reached = new HashMap<>();
        int maxFrontierSize = 0;
        int nodesExpanded = 0;

        City2 start = cities.get(startCity);
        City2 goal = cities.get(goalCity);

        if (start == null || goal == null) {
            System.out.println("Start or goal city not found.");
            return null;
        }

        // Initial State
        Set<String> initialPermits = new HashSet<>();
        double initialCost = 0;
        List<String> initialPath = new ArrayList<>();
        initialPath.add(startCity);
        double initialPermitCost = 0;

        double heuristic = scalingFactor * haversineDistance(start, goal);
        //Option 1: Ohne Genehmigung holen
        State2 initialState = new State2(
                startCity,
                initialPermits,
                initialCost,
                initialCost + heuristic,
                initialPath,
                initialPermitCost,
                new ArrayList<>() // Initialize permitsAcquired as an empty list
        );
        frontier.add(initialState);
        reached.put(generateStateKey(initialState), initialState);

        // Option 2: Mit Genehmigung holen (falls verfügbar)
        if (start.getAvailablePermit() != null) {
            Set<String> permitsWithNew = new HashSet<>(initialPermits);
            permitsWithNew.add(start.getAvailablePermit());

            // Add permit cost to the initial cost
            double costWithPermit = initialCost + permitCost; // Kosten für Genehmigung

            // Initialize the list for tracking permits acquired
            List<String> permitsAcquiredWithNew = new ArrayList<>();

            // Track the city and the acquired permit as a single entry
            permitsAcquiredWithNew.add("Acquired permit " + start.getAvailablePermit() + " at " + startCity);  // Permit acquisition detail

            // Create a new State2 object reflecting this new state
            State2 stateWithPermit = new State2(
                    startCity,  // Current city is still the start city
                    permitsWithNew,  // Updated permit list
                    costWithPermit,  // New cost with permit
                    costWithPermit + heuristic,  // Estimate cost with heuristic
                    initialPath,  // Path is still initialPath for now
                    permitCost,  // Total cost of permit acquisition
                    permitsAcquiredWithNew  // The list of all acquired permits
            );

            frontier.add(stateWithPermit);  // Add state to the frontier
            reached.put(generateStateKey(stateWithPermit), stateWithPermit);  // Save to reached set
        }



        while (!frontier.isEmpty()) {
            maxFrontierSize = Math.max(maxFrontierSize, frontier.size());
            State2 currentState = frontier.poll();
            nodesExpanded++;

            // Goal reached
            if (currentState.getCurrentCity().equals(goalCity)) {
                return new SearchResult(
                        currentState.getCostSoFar(),
                        maxFrontierSize,
                        nodesExpanded,
                        currentState.getPath(),
                        currentState.getTotalPermitCost(),
                        currentState.getPermitsAcquired() // Add permitsAcquired here

                );
            }

            // Generate actions (neighbors)
            List<Connection2> neighbors = graph.getOrDefault(currentState.getCurrentCity(), new ArrayList<>());
            for (Connection2 conn : neighbors) {
                String nextCityName = conn.getCity2();
                City2 nextCity = cities.get(nextCityName);
                if (nextCity == null) continue; //unknown city skip

                // Check if the required permit is available
                if (conn.getRequiredPermit() != null && !currentState.getPermits().contains(conn.getRequiredPermit())) {
                    continue; //cannot drive on this road without permit
                }

                // Calculate new cost
                double newCost = currentState.getCostSoFar() + conn.getDistance();

                // Create new path
                List<String> newPath = new ArrayList<>(currentState.getPath());
                newPath.add(nextCityName);
                // Calculate heuristic
                double h = scalingFactor * haversineDistance(nextCity, goal);
                // Option 1: Without obtaining a new permit
                Set<String> permitsWithoutNew = new HashSet<>(currentState.getPermits());
                double costWithoutPermit = newCost;
                double estimatedTotalCostWithoutPermit = costWithoutPermit + h;
                double totalPermitCostWithoutNew = currentState.getTotalPermitCost();
                State2 childStateWithoutPermit = new State2(
                        nextCityName,
                        permitsWithoutNew,
                        costWithoutPermit,
                        estimatedTotalCostWithoutPermit,
                        newPath,
                        totalPermitCostWithoutNew,
                        new ArrayList<>(currentState.getPermitsAcquired()) // Copy parent's permitsAcquired
                );
                String stateKeyWithoutPermit = generateStateKey(childStateWithoutPermit);

                // Check if this state is already in reached
                if (!reached.containsKey(stateKeyWithoutPermit)) {
                    reached.put(stateKeyWithoutPermit, childStateWithoutPermit);
                    frontier.add(childStateWithoutPermit);
                } else {
                    State2 existingState = reached.get(stateKeyWithoutPermit);
                    if (childStateWithoutPermit.getCostSoFar() < existingState.getCostSoFar()) {
                        frontier.remove(existingState);
                        reached.put(stateKeyWithoutPermit, childStateWithoutPermit);
                        frontier.add(childStateWithoutPermit);
                    }
                }

                // Option 2: Obtain a new permit (if available and not already obtained)
                if (nextCity.getAvailablePermit() != null && !currentState.getPermits().contains(nextCity.getAvailablePermit())) {
                    Set<String> permitsWithNew = new HashSet<>(currentState.getPermits());
                    permitsWithNew.add(nextCity.getAvailablePermit());
                    double costWithPermit = newCost + permitCost;
                    double estimatedTotalCostWithPermit = costWithPermit + h;
                    double totalPermitCostWithNew = currentState.getTotalPermitCost() + permitCost;

                    // Update permitsAcquired list with city and permit details
                    List<String> newPermitsAcquired = new ArrayList<>(currentState.getPermitsAcquired());
                    String permitDetail = "Acquired permit " + nextCity.getAvailablePermit() + " at " + nextCityName;
                    newPermitsAcquired.add(permitDetail); // Add full detail to the list

                    State2 childStateWithPermit = new State2(
                            nextCityName,
                            permitsWithNew,
                            costWithPermit,
                            estimatedTotalCostWithPermit,
                            newPath,
                            totalPermitCostWithNew,
                            newPermitsAcquired // Ensure it's correctly passed here
                    );

                    String stateKeyWithPermit = generateStateKey(childStateWithPermit);

                    // Check if this state is already in reached
                    if (!reached.containsKey(stateKeyWithPermit)) {
                        reached.put(stateKeyWithPermit, childStateWithPermit);
                        frontier.add(childStateWithPermit);
                    } else {
                        State2 existingState = reached.get(stateKeyWithPermit);
                        if (childStateWithPermit.getCostSoFar() < existingState.getCostSoFar()) {
                            frontier.remove(existingState);
                            reached.put(stateKeyWithPermit, childStateWithPermit);
                            frontier.add(childStateWithPermit);
                        }
                    }
                }

            }
        }

        // No solution found
        System.out.println("No solution found.");
        return null;
    }


    private String generateStateKey(State2 state) {
        List<String> sortedPermits = new ArrayList<>(state.getPermits());
        Collections.sort(sortedPermits);
        return state.getCurrentCity() + "|" + String.join(",", sortedPermits);
    }

    private static void runTestCases1(RoutePlanner2 planner, List<TestCase> testCases, double scalingFactor) {
        // Statistics variables
        double totalOptimalCost = 0;
        double totalMaxFrontierSize = 0;
        double totalNodesExpanded = 0;
        int successfulCases = 0;

        for (TestCase testCase : testCases) {
            System.out.println("Processing test case: " + testCase.getStartCity() + " to " + testCase.getGoalCity());
            SearchResult result = planner.aStarSearch(testCase.getStartCity(), testCase.getGoalCity(), scalingFactor); // Pass scaling factor here

            if (result != null) {
                totalOptimalCost += result.getOptimalCost();
                totalMaxFrontierSize += result.getMaxFrontierSize();
                totalNodesExpanded += result.getNodesExpanded();
                successfulCases++;

                // Print individual metrics
                System.out.printf("Optimal Cost: %.2f\n", result.getOptimalCost());
                System.out.println("Max Frontier Size: " + result.getMaxFrontierSize());
                System.out.println("Nodes Expanded: " + result.getNodesExpanded());

                // Print the path for all test cases
                System.out.print("Path: ");
                List<String> path = result.getPath();
                for (int i = 0; i < path.size(); i++) {
                    System.out.print(path.get(i));
                    if (i < path.size() - 1) {
                        System.out.print(" -> ");
                    }
                }
                System.out.println();

                System.out.println("Permits Acquired: ");
                if (result.getPermitsAcquired().isEmpty()) {
                    System.out.println("  None");
                } else {
                    for (String permitDetail : result.getPermitsAcquired()) {
                        System.out.println("  " + permitDetail);
                    }
                }

                // Calculate total distance excluding permit costs
                double totalDistance = result.getOptimalCost() - result.getTotalPermitCost();

                // Print costs
                System.out.printf("Total Distance: %.2f km\n", totalDistance);
                System.out.printf("Total Permit Costs: %.2f\n", result.getTotalPermitCost());
                System.out.printf("Total Cost: %.2f km\n", result.getOptimalCost());
            } else {
                System.out.println("No solution between " + testCase.getStartCity() + " and " + testCase.getGoalCity());
            }
            System.out.println(); // For better readability
        }

        if (successfulCases > 0) {
            double avgOptimalCost = totalOptimalCost / successfulCases;
            double avgMaxFrontierSize = totalMaxFrontierSize / successfulCases;
            double avgNodesExpanded = totalNodesExpanded / successfulCases;

            System.out.printf("Average Optimal Cost: %.2f\n", avgOptimalCost);
            System.out.printf("Average Max Frontier Size: %.2f\n", avgMaxFrontierSize);
            System.out.printf("Average Nodes Expanded: %.2f\n", avgNodesExpanded);
        } else {
            System.out.println("No successful test cases.");
        }
    }
    private static void runTestCases2(RoutePlanner2 planner, List<TestCase> testCases, double scalingFactor) {
        // Statistics variables
        double totalOptimalCost = 0;
        double totalMaxFrontierSize = 0;
        double totalNodesExpanded = 0;
        int successfulCases = 0;

        for (TestCase testCase : testCases) {
            SearchResult result = planner.aStarSearch(testCase.getStartCity(), testCase.getGoalCity(), scalingFactor); // Pass scaling factor here

            if (result != null) {
                totalOptimalCost += result.getOptimalCost();
                totalMaxFrontierSize += result.getMaxFrontierSize();
                totalNodesExpanded += result.getNodesExpanded();
                successfulCases++;
            }
        }

        if (successfulCases > 0) {
            double avgOptimalCost = totalOptimalCost / successfulCases;
            double avgMaxFrontierSize = totalMaxFrontierSize / successfulCases;
            double avgNodesExpanded = totalNodesExpanded / successfulCases;

            System.out.printf("Average Optimal Cost: %.2f\n", avgOptimalCost);
            System.out.printf("Average Max Frontier Size: %.2f\n", avgMaxFrontierSize);
            System.out.printf("Average Nodes Expanded: %.2f\n", avgNodesExpanded);
        }
    }

    public static void main(String[] args) {
        RoutePlanner2 planner = new RoutePlanner2();
        /*
        if (args.length != 3) {
            System.out.println("Usage: java -jar RoutePlanner2.jar <cities_file> <connections_file> <testcases_file>");
            System.exit(1);
        }
        String citiesFile = args[0];
        String connectionsFile = args[1];
        String testcasesFile = args[2];
        */
        String citiesFile = "src/testcases_Teilaufgabe_3/bigGraph_cities.txt";
        String connectionsFile = "src/testcases_Teilaufgabe_3/bigGraph_connections.txt";
        String testcasesFile = "src/testcases_Teilaufgabe_3/testcases_bigGraph.txt";

        try {
            planner.readCities(citiesFile);
            planner.readConnections(connectionsFile);

            // Read test cases
            List<TestCase> testCases = planner.readTestCases(testcasesFile);

            // Normal heuristic (scaling factor = 1.0)
            System.out.println("Results with Normal Heuristic (Scaling Factor = 1.0):");
            runTestCases1(planner, testCases, 1.0); // Pass scaling factor as 1.0
            System.out.println("\n");
            // Scaled heuristic (scaling factor = 1.4515)
            System.out.println("Results with Scaled Heuristic (Scaling Factor = 1.4515):");
            runTestCases2(planner, testCases, 1.4515); // Pass scaling factor as 1.4515

        } catch (IOException e) {
            System.out.println("Error reading files: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
