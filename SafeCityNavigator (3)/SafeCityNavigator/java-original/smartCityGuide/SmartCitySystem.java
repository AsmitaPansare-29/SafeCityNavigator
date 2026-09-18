import java.util.*;

// ==================== CLASS: Place ====================
class Place {
    String name;
    String type;
    double rating;
    int crowd;
    boolean disasterAlert;
    String weather;

    public Place(String name, String type, double rating, int crowd) {
        this.name = name;
        this.type = type;
        this.rating = rating;
        this.crowd = crowd;
        this.disasterAlert = false;
        this.weather = "Unknown";
    }
}

// ==================== CLASS: Graph ====================
class Graph {
    private Map<String, List<Edge>> adjList = new HashMap<>();

    static class Edge {
        String destination;
        double cost;
        Edge(String d, double c) { destination = d; cost = c; }
    }

    public void addEdge(String src, String dest, double cost) {
        adjList.putIfAbsent(src, new ArrayList<>());
        adjList.putIfAbsent(dest, new ArrayList<>());
        adjList.get(src).add(new Edge(dest, cost));
        adjList.get(dest).add(new Edge(src, cost));
    }

    public double findMinCost(String start, String end) {
        Map<String, Double> dist = new HashMap<>();
        for (String key : adjList.keySet()) dist.put(key, Double.MAX_VALUE);
        dist.put(start, 0.0);

        PriorityQueue<Map.Entry<String, Double>> pq =
            new PriorityQueue<>(Map.Entry.comparingByValue);
        pq.add(Map.entry(start, 0.0));

        while (!pq.isEmpty()) {
            var curr = pq.poll();
            String place = curr.getKey();
            double d = curr.getValue();
            if (place.equals(end)) return d;

            for (Edge e : adjList.get(place)) {
                double newDist = d + e.cost;
                if (newDist < dist.get(e.destination)) {
                    dist.put(e.destination, newDist);
                    pq.add(Map.entry(e.destination, newDist));
                }
            }
        }
        return dist.get(end);
    }
}

// ==================== CLASS: Trie ====================
class TrieNode {
    Map<Character, TrieNode> children = new HashMap<>();
    boolean isEndOfWord;
    List<String> suggestions = new ArrayList<>();
}

class Trie {
    private TrieNode root = new TrieNode();

    public void insert(String word) {
        TrieNode node = root;
        for (char c : word.toLowerCase().toCharArray()) {
            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);
            if (!node.suggestions.contains(word)) node.suggestions.add(word);
        }
        node.isEndOfWord = true;
    }

    public List<String> autocomplete(String prefix) {
        TrieNode node = root;
        for (char c : prefix.toLowerCase().toCharArray()) {
            if (!node.children.containsKey(c)) return new ArrayList<>();
            node = node.children.get(c);
        }
        return node.suggestions;
    }
}

// ==================== CLASS: TravelPlan ====================
class TravelPlan {
    LinkedList<String> plan = new LinkedList<>();
    Stack<String> undoStack = new Stack<>();
    Stack<String> redoStack = new Stack<>();

    public void addPlace(String place) {
        plan.add(place);
        undoStack.push("ADD:" + place);
    }

    public void removePlace(String place) {
        plan.remove(place);
        undoStack.push("REMOVE:" + place);
    }

    public void undo() {
        if (undoStack.isEmpty()) {
            System.out.println("No actions to undo.");
            return;
        }
        String action = undoStack.pop();
        redoStack.push(action);
        String[] parts = action.split(":");
        if (parts[0].equals("ADD")) plan.remove(parts[1]);
        else if (parts[0].equals("REMOVE")) plan.add(parts[1]);
    }

    public void redo() {
        if (redoStack.isEmpty()) {
            System.out.println("No actions to redo.");
            return;
        }
        String action = redoStack.pop();
        String[] parts = action.split(":");
        if (parts[0].equals("ADD")) plan.add(parts[1]);
        else if (parts[0].equals("REMOVE")) plan.remove(parts[1]);
    }

    public void showPlan() {
        System.out.println("📍 Current Travel Plan: " + plan);
    }
}

// ==================== CLASS: WeatherAPI ====================
class WeatherAPI {
    public String getWeather(String place) {
        String[] weathers = {"Sunny", "Rainy", "Cloudy", "Windy", "Stormy"};
        return weathers[(int)(Math.random() * weathers.length)];
    }

    public boolean checkDisaster(String place) {
        return Math.random() < 0.1; // 10% chance
    }
}

// ==================== CLASS: CrowdSimulator ====================
class CrowdSimulator {
    private Map<String, Integer> crowdData = new HashMap<>();

    public void updateCrowd(String place) {
        int newCrowd = (int)(Math.random() * 500);
        crowdData.put(place, newCrowd);
    }

    public int getCrowd(String place) {
        return crowdData.getOrDefault(place, 0);
    }

    public void showCrowd() {
        for (var entry : crowdData.entrySet()) {
            System.out.println(entry.getKey() + " → " + entry.getValue() + " people");
        }
    }
}

// ==================== CLASS: SmartCitySystem ====================
public class SmartCitySystem {
    HashMap<String, Place> places = new HashMap<>();
    Trie searchTrie = new Trie();
    Graph graph = new Graph();
    TravelPlan travelPlan = new TravelPlan();
    CrowdSimulator crowdSim = new CrowdSimulator();
    WeatherAPI weatherAPI = new WeatherAPI();
    Scanner sc = new Scanner(System.in);

    public void addPlace(Place p) {
        places.put(p.name, p);
        searchTrie.insert(p.name);
        crowdSim.updateCrowd(p.name);
    }

    public void showPlaceDetails(String name) {
        Place p = places.get(name);
        if (p == null) {
            System.out.println(" Place not found!");
            return;
        }
        p.weather = weatherAPI.getWeather(p.name);
        p.disasterAlert = weatherAPI.checkDisaster(p.name);
        p.crowd = crowdSim.getCrowd(p.name);
        System.out.println("\n=== 📍 Place Details ===");
        System.out.println("Name: " + p.name);
        System.out.println("Type: " + p.type);
        System.out.println("Rating: " + p.rating);
        System.out.println("Crowd: " + p.crowd + " people");
        System.out.println("Weather: " + p.weather);
        System.out.println("Disaster Alert: " + (p.disasterAlert ? " Yes" : "No"));
    }

    public void planRoute(String src, String dest) {
        if (!places.containsKey(src) || !places.containsKey(dest)) {
            System.out.println("Invalid source or destination!");
            return;
        }
        double cost = graph.findMinCost(src, dest);
        System.out.println(" Minimum travel cost from " + src + " → " + dest + " = ₹" + cost);
    }

    public void mainMenu() {
        while (true) {
            System.out.println("\n=====  SMART CITY MENU =====");
            System.out.println("1️⃣ Search Place");
            System.out.println("2️⃣ Show Place Details");
            System.out.println("3️⃣ Plan Route");
            System.out.println("4️⃣ Manage Travel Plan");
            System.out.println("5️⃣ View Crowd Info");
            System.out.println("6️⃣ Exit");
            System.out.print("Enter choice: ");
            int ch = sc.nextInt();
            sc.nextLine();

            switch (ch) {
                case 1 -> {
                    System.out.print("Enter prefix to search: ");
                    String prefix = sc.nextLine();
                    List<String> res = searchTrie.autocomplete(prefix);
                    System.out.println(" Suggestions: " + res);
                }
                case 2 -> {
                    System.out.print("Enter place name: ");
                    String name = sc.nextLine();
                    showPlaceDetails(name);
                }
                case 3 -> {
                    System.out.print("Enter source: ");
                    String src = sc.nextLine();
                    System.out.print("Enter destination: ");
                    String dest = sc.nextLine();
                    planRoute(src, dest);
                }
                case 4 -> travelPlanMenu();
                case 5 -> crowdSim.showCrowd();
                case 6 -> {
                    System.out.println(" Exiting Smart City System. Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    public void travelPlanMenu() {
        while (true) {
            System.out.println("\n=====  TRAVEL PLAN MENU =====");
            System.out.println("1. Add Place");
            System.out.println("2. Remove Place");
            System.out.println("3. Undo Last");
            System.out.println("4. Redo Last");
            System.out.println("5. Show Plan");
            System.out.println("6. Back");
            System.out.print("Enter choice: ");
            int c = sc.nextInt();
            sc.nextLine();

            switch (c) {
                case 1 -> {
                    System.out.print("Enter place to add: ");
                    travelPlan.addPlace(sc.nextLine());
                }
                case 2 -> {
                    System.out.print("Enter place to remove: ");
                    travelPlan.removePlace(sc.nextLine());
                }
                case 3 -> travelPlan.undo();
                case 4 -> travelPlan.redo();
                case 5 -> travelPlan.showPlan();
                case 6 -> { return; }
                default -> System.out.println("Invalid option!");
            }
        }
    }

    // ==================== MAIN ====================
    public static void main(String[] args) {
        SmartCitySystem sc = new SmartCitySystem();

        // Initialize sample city data
        sc.addPlace(new Place("City Hospital", "Hospital", 4.6, 100));
        sc.addPlace(new Place("Green Park", "Park", 4.8, 200));
        sc.addPlace(new Place("Royal Dine", "Restaurant", 4.5, 150));
        sc.addPlace(new Place("Tech Mall", "Shopping", 4.3, 250));
        sc.addPlace(new Place("Metro Station", "Transport", 4.0, 300));

        // Create graph routes with travel costs
        sc.graph.addEdge("City Hospital", "Green Park", 12);
        sc.graph.addEdge("Green Park", "Royal Dine", 8);
        sc.graph.addEdge("Royal Dine", "Tech Mall", 5);
        sc.graph.addEdge("Tech Mall", "Metro Station", 10);
        sc.graph.addEdge("City Hospital", "Metro Station", 25);

        System.out.println("Welcome to Smart City System!");
        sc.mainMenu();
    }
}
