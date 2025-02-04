import java.io.*;
import java.util.*;

public class RoutePlanner {
    private Map<String, City> cities = new HashMap<>();
    private Map<String, List<Connection>> graph = new HashMap<>();
    int permitCost = 10;

    /**
     * Liest die Städte aus der angegebenen Datei ein.
     *
     * @param filename Pfad zur Städte-Datei
     * @throws IOException bei Datei-Lese-Fehlern
     */
    public void readCities(String filename) throws IOException {
        cities.clear(); // Vorherige Daten löschen
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        br.readLine(); // Überspringe Header
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue; // Überspringe leere Zeilen
            String[] parts = line.split(";");
            if (parts.length < 3) continue; // Ungültige Zeile überspringen
            String name = parts[0].trim();
            int heuristic = Integer.parseInt(parts[1].trim());
            String availablePermit = parts[2].trim();
            cities.put(name, new City(name, heuristic, availablePermit));
        }
        br.close();
    }

    /**
     * Liest die Verbindungen aus der angegebenen Datei ein und baut den Graphen auf.
     *
     * @param filename Pfad zur Verbindungen-Datei
     * @throws IOException bei Datei-Lese-Fehlern
     */
    public void readConnections(String filename) throws IOException {
        graph.clear(); // Vorherige Daten löschen
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        br.readLine(); // Überspringe Header
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue; // Überspringe leere Zeilen
            String[] parts = line.split(";");
            if (parts.length < 4) continue; // Ungültige Zeile überspringen
            String city1 = parts[0].trim();
            String city2 = parts[1].trim();
            int distance = Integer.parseInt(parts[2].trim());
            String requiredPermit = parts[3].trim();
            Connection connection = new Connection(city1, city2, distance, requiredPermit);
            // Füge die Verbindung in beide Richtungen hinzu
            graph.computeIfAbsent(city1, k -> new ArrayList<>()).add(connection);
            graph.computeIfAbsent(city2, k -> new ArrayList<>())
                    .add(new Connection(city2, city1, distance, requiredPermit));
        }
        br.close();
    }

    /**
     * Führt die A*-Suche von startCity zu goalCity durch.
     *
     * @param startCity Name der Startstadt
     * @param goalCity  Name der Zielstadt
     * @return Gesamtkosten der optimalen Route oder -1, wenn keine Lösung existiert
     */
    public int aStarSearch(String startCity, String goalCity) {
        PriorityQueue<State> frontier = new PriorityQueue<>();
        Map<String, State> reached = new HashMap<>();

        City start = cities.get(startCity);
        City goal = cities.get(goalCity);

        if (start == null || goal == null) {
            System.out.println("Start- oder Zielstadt nicht gefunden.");
            return -1;
        }

        // Initialer Knoten
        Set<String> initialPermits = new HashSet<>();
        int initialCost = 0;
        List<String> initialPath = new ArrayList<>();
        initialPath.add(startCity);

        // Option 1: Ohne Genehmigung holen
        State initialState = new State(
                startCity,
                initialPermits,
                initialCost,
                initialCost + start.getHeuristic(),
                initialPath
        );
        frontier.add(initialState);
        reached.put(generateStateKey(initialState), initialState);

        // Option 2: Mit Genehmigung holen (falls verfügbar)
        if (start.getAvailablePermit() != null) {
            Set<String> permitsWithNew = new HashSet<>(initialPermits);
            permitsWithNew.add(start.getAvailablePermit());
            int costWithPermit = initialCost + permitCost; // Kosten für Genehmigung
            State stateWithPermit = new State(
                    startCity,
                    permitsWithNew,
                    costWithPermit,
                    costWithPermit + start.getHeuristic(),
                    initialPath
            );
            frontier.add(stateWithPermit);
            reached.put(generateStateKey(stateWithPermit), stateWithPermit);
        }

        while (!frontier.isEmpty()) {
            State currentState = frontier.poll();

            // Ziel erreicht
            if (currentState.getCurrentCity().equals(goalCity)) {
                System.out.println("Optimaler Pfad gefunden: " + currentState.getPath());
                System.out.println("Gesamtkosten: " + currentState.getCostSoFar());
                return currentState.getCostSoFar();
            }

            // Generate actions (neighbours)
            List<Connection> neighbors = graph.getOrDefault(currentState.getCurrentCity(), new ArrayList<>());
            for (Connection conn : neighbors) {
                String nextCityName = conn.getCity2();
                City nextCity = cities.get(nextCityName);
                if (nextCity == null) continue; // unknown city skip

                // check if the required permit is available
                if (conn.getRequiredPermit() != null && !currentState.getPermits().contains(conn.getRequiredPermit())) {
                    continue; // cannot drive on this road without permit
                }


                // Calculate new cost
                int newCost = currentState.getCostSoFar() + conn.getDistance();

                // Create new path
                List<String> newPath = new ArrayList<>(currentState.getPath());
                newPath.add(nextCityName);

                // Option 1: Ohne Genehmigung holen
                Set<String> permitsWithoutNew = new HashSet<>(currentState.getPermits());
                int costWithoutPermit = newCost;
                int estimatedTotalCostWithoutPermit = costWithoutPermit + nextCity.getHeuristic();
                State childStateWithoutPermit = new State(
                        nextCityName,
                        permitsWithoutNew,
                        costWithoutPermit,
                        estimatedTotalCostWithoutPermit,
                        newPath
                );
                String stateKeyWithoutPermit = generateStateKey(childStateWithoutPermit);

                // Check if this state is already in reached
                if (!reached.containsKey(stateKeyWithoutPermit)) {
                    reached.put(stateKeyWithoutPermit, childStateWithoutPermit);
                    frontier.add(childStateWithoutPermit);
                } else {
                    State existingState = reached.get(stateKeyWithoutPermit);
                    if (childStateWithoutPermit.getCostSoFar() < existingState.getCostSoFar()) {
                        // Entferne den schlechteren Knoten aus der Frontier
                        frontier.remove(existingState);
                        reached.put(stateKeyWithoutPermit, childStateWithoutPermit);
                        frontier.add(childStateWithoutPermit);
                    }
                }

                // Option 2: Obtain a new permit (if available and not already obtained)
                if (nextCity.getAvailablePermit() != null && !currentState.getPermits().contains(nextCity.getAvailablePermit())) {
                    Set<String> permitsWithNew = new HashSet<>(currentState.getPermits());
                    permitsWithNew.add(nextCity.getAvailablePermit());
                    int costWithPermit = newCost + permitCost; // Kosten für Genehmigung
                    int estimatedTotalCostWithPermit = costWithPermit + nextCity.getHeuristic();
                    State childStateWithPermit = new State(
                            nextCityName,
                            permitsWithNew,
                            costWithPermit,
                            estimatedTotalCostWithPermit,
                            newPath
                    );
                    String stateKeyWithPermit = generateStateKey(childStateWithPermit);

                    // Check if this state is already in reached
                    if (!reached.containsKey(stateKeyWithPermit)) {
                        reached.put(stateKeyWithPermit, childStateWithPermit);
                        frontier.add(childStateWithPermit);
                    } else {
                        State existingState = reached.get(stateKeyWithPermit);
                        if (childStateWithPermit.getCostSoFar() < existingState.getCostSoFar()) {
                            // Entferne den schlechteren Knoten aus der Frontier
                            frontier.remove(existingState);
                            reached.put(stateKeyWithPermit, childStateWithPermit);
                            frontier.add(childStateWithPermit);
                        }
                    }
                }
            }
        }

        // Keine Lösung gefunden
        System.out.println("Keine Lösung gefunden.");
        return -1;
    }

    private String generateStateKey(State state) {
        // Erstelle einen eindeutigen Schlüssel basierend auf der aktuellen Stadt und den Genehmigungen
        List<String> sortedPermits = new ArrayList<>(state.getPermits());
        Collections.sort(sortedPermits);
        return state.getCurrentCity() + "|" + String.join(",", sortedPermits);
    }

    public static void main(String[] args) {
        RoutePlanner planner = new RoutePlanner();

        if (args.length != 2) {
            System.out.println("Usage: java -jar RoutePlanner.jar <cities_file> <connections_file>");
            System.exit(1);
        }

        String citiesFile = args[0];
        String connectionsFile = args[1];

        try {
            planner.readCities(citiesFile);
            planner.readConnections(connectionsFile);
            int totalCost = planner.aStarSearch("A", "B");
            if (totalCost != -1) {
                System.out.println("Optimale Pfadkosten für : " + totalCost);
            } else {
                System.out.println("Es existiert keine Lösung.");
            }
        } catch (IOException e) {
            System.out.println("Fehler beim Einlesen der Dateien: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println(); // Empty line for better readability
    }
}

