package safeRoute;
import java.util.*;

/*
City map represented as a Weighted Directed Graph.
DATA STRUCTURE: Adjacency List (HashMap of Node → List of Edges)
Why Adjacency List over Adjacency Matrix?
  → Real city maps are SPARSE (not every place connects to every other)
  → Adjacency List is memory efficient: O(V + E) vs O(V²) for matrix
  → Faster iteration over neighbors during Dijkstra

Key DSA Components used:
  1. HashMap         → O(1) lookup of neighbors for any node
  2. ArrayList       → Dynamic list of edges per node
  3. PriorityQueue   → Min-Heap used inside Dijkstra
 */
public class SafetyGraph {

    // Core data structure: adjacency list
    private final HashMap<String, Node> nodes;
    private final HashMap<String, List<Edge>> adjacencyList;

    public SafetyGraph() {
        nodes = new HashMap<>();
        adjacencyList = new HashMap<>();
    }

    /*`Add a location node to the graph */
    public void addNode(Node node) {
        nodes.put(node.id, node);
        adjacencyList.put(node.id, new ArrayList<>());
        System.out.println("--> Added location: " + node);
    }

    /** Add a bidirectional road between two locations */
    public void addEdge(String fromId, String toId, double distance, int lighting, int crowdDensity) {
        Node from = nodes.get(fromId);
        Node to = nodes.get(toId);

        if (from == null || to == null) {
            System.out.println("[X] Invalid node IDs: " + fromId + ", " + toId);
            return;
        }

        Edge forwardEdge = new Edge(to, distance, lighting, crowdDensity);
        Edge reverseEdge = new Edge(from, distance, lighting, crowdDensity);

        adjacencyList.get(fromId).add(forwardEdge);
        adjacencyList.get(toId).add(reverseEdge); // bidirectional
    }

    public Node getNode(String id) {
        return nodes.get(id);
    }

    public HashMap<String, Node> getAllNodes() {
        return nodes;
    }

    public List<Edge> getNeighbors(String nodeId) {
        return adjacencyList.getOrDefault(nodeId, new ArrayList<>());
    }

    /** Print the full graph — useful for debugging and presentation */
    public void printGraph() {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║        CITY SAFETY GRAPH MAP         ║");
        System.out.println("╚══════════════════════════════════════╝");
        for (String nodeId : adjacencyList.keySet()) {
            Node n = nodes.get(nodeId);
            System.out.println("\n" + n);
            List<Edge> edges = adjacencyList.get(nodeId);
            if (edges.isEmpty()) {
                System.out.println("   (No connections)");
            } else {
                for (Edge e : edges) {
                    System.out.println("   " + e);
                }
            }
        }
        System.out.println();
    }

    /** Find all safe refuge points (police stations, hospitals) */
    public List<Node> getSafePlaces() {
        List<Node> safePlaces = new ArrayList<>();
        for (Node node : nodes.values()) {
            if (node.type.equals("police") || node.type.equals("hospital")) {
                safePlaces.add(node);
            }
        }
        return safePlaces;
    }
}
