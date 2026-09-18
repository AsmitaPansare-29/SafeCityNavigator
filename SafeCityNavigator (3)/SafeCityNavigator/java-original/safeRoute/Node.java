package safeRoute;
/*
Represents a location node on the safety map.
Each node has an ID, name, coordinates, and a safety score.
DATA STRUCTURE: Node (Graph Vertex)
Used as vertices in our weighted graph.
 */
public class Node implements Comparable<Node> {

    public String id;           // Unique identifier (e.g., "A", "B")
    public String name;         // Human-readable name (e.g., "City Center")
    public double x, y;         // Coordinates for visual map rendering
    public int safetyScore;     // 1 (very unsafe) to 10 (very safe)
    public String type;         // "normal", "police", "hospital", "metro"

    // Used by Dijkstra's algorithm
    public double distanceCost; // Accumulated cost from source

    public Node(String id, String name, double x, double y, int safetyScore, String type) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.safetyScore = safetyScore;
        this.type = type;
        this.distanceCost = Double.MAX_VALUE; // Initialize to infinity
    }

    /*
    compareTo is needed because Node goes inside a PriorityQueue (Min-Heap).
    The node with the LOWEST cost gets processed first.
     */
    @Override
    public int compareTo(Node other) {
        return Double.compare(this.distanceCost, other.distanceCost);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (Safety: %d/10)", id, name, safetyScore);
    }
}
