package safeRoute;

import java.util.*;

/**
 * ════════════════════════════════════════════════════
 * FEATURE 2: NEAREST SAFE PLACE FINDER (BFS)
 * ════════════════════════════════════════════════════
 *
 * WHAT IT DOES:
 *   From your current location, finds the nearest
 *   police station or hospital using BFS.
 *
 * HOW TO EXPLAIN IN PRESENTATION:
 *   "BFS explores the map layer by layer — first checks
 *    all locations 1 step away, then 2 steps, then 3...
 *    The FIRST police station or hospital it reaches
 *    is guaranteed to be the CLOSEST one."
 *
 * WHY BFS (not Dijkstra) here?
 *   BFS finds the nearest node in terms of NUMBER OF HOPS.
 *   It's simpler and faster for "find nearest X" problems.
 *
 * DATA STRUCTURE: Queue (LinkedList) — FIFO
 *   → First node added = first node explored
 *   → This is what makes BFS explore layer by layer
 *
 * Time Complexity:  O(V + E)
 * Space Complexity: O(V)
 */
public class BFSNearestSafePlace {

    private final SafetyGraph graph;

    public BFSNearestSafePlace(SafetyGraph graph) {
        this.graph = graph;
    }

    /**
     * Find the nearest safe place (police or hospital) from a starting node.
     *
     * @param startId  the node ID where the user currently is
     * @return the nearest safe Node, or null if none found
     */
    public Node findNearest(String startId) {
        System.out.println("\n🔎 BFS: Searching for nearest safe place from "
            + graph.getNode(startId).name + "...");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // BFS uses a Queue — FIFO (First In, First Out)
        // LinkedList implements Queue in Java
        Queue<String> queue   = new LinkedList<>();
        HashSet<String> visited = new HashSet<>();
        HashMap<String, String> parent = new HashMap<>(); // to reconstruct path

        queue.add(startId);
        visited.add(startId);
        parent.put(startId, null);

        int layer = 0; // how many "hops" away we are

        while (!queue.isEmpty()) {
            int layerSize = queue.size(); // process all nodes in current layer
            layer++;

            System.out.printf("  Layer %d: checking %d location(s)...%n", layer, layerSize);

            for (int i = 0; i < layerSize; i++) {
                String currentId = queue.poll(); // remove from front of queue
                Node currentNode = graph.getNode(currentId);

                // Check if this is a safe place!
                if (currentNode.type.equals("police") || currentNode.type.equals("hospital")) {
                    System.out.println("\n  [OK] Found! " + currentNode);
                    printPath(parent, startId, currentId);
                    return currentNode;
                }

                // Add unvisited neighbors to queue
                for (Edge edge : graph.getNeighbors(currentId)) {
                    String neighborId = edge.destination.id;
                    if (!visited.contains(neighborId)) {
                        visited.add(neighborId);
                        parent.put(neighborId, currentId);
                        queue.add(neighborId); // add to BACK of queue
                    }
                }
            }
        }

        System.out.println("  [X] No safe place found nearby.");
        return null;
    }

    /** Reconstruct and print the path from start to the safe place */
    private void printPath(HashMap<String, String> parent, String start, String dest) {
        LinkedList<String> path = new LinkedList<>();
        String cur = dest;
        while (cur != null) {
            path.addFirst(cur);
            cur = parent.get(cur);
        }

        System.out.print("  📍 Path to safety: ");
        for (int i = 0; i < path.size(); i++) {
            System.out.print(graph.getNode(path.get(i)).name);
            if (i < path.size() - 1) System.out.print(" → ");
        }
        System.out.println();
        System.out.printf("  🚶 Hops needed: %d%n%n", path.size() - 1);
    }
}