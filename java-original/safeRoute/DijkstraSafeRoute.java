package safeRoute;

import java.util.*;

/**
 * Safe Route Finder using Dijkstra's Algorithm with Safety Weights.
 *
 * ═══════════════════════════════════════════════════════════════
 * CORE DSA: DIJKSTRA'S ALGORITHM + PRIORITY QUEUE (MIN-HEAP)
 * ═══════════════════════════════════════════════════════════════
 *
 * Standard Dijkstra finds the SHORTEST path.
 * Our modification: replace "distance" weights with "safety weights"
 *   safetyWeight = distance × (11 - avgSafetyScore)
 *
 * Result: Dijkstra now finds the SAFEST path automatically!
 *
 * Time Complexity:  O((V + E) log V)
 * Space Complexity: O(V)
 *
 * Key Data Structures:
 *   - PriorityQueue<Node>  → Min-Heap: always processes the safest node next
 *   - HashMap<String, Double> distances → stores best cost to each node
 *   - HashMap<String, String> previous  → stores path backtracking info
 *   - HashSet<String> visited           → avoids reprocessing nodes
 */
public class DijkstraSafeRoute {

    private final SafetyGraph graph;
    private final NightModeManager nightMode; // FEATURE 1: Night Mode

    public DijkstraSafeRoute(SafetyGraph graph, NightModeManager nightMode) {
        this.graph = graph;
        this.nightMode = nightMode;
    }

    /**
     * Find the safest route from source to destination.
     * Now also applies Night Mode multiplier if active.
     * Returns a RouteResult containing the path and total safety cost.
     */
    public RouteResult findSafestRoute(String sourceId, String destId) {
        HashMap<String, Node> allNodes = graph.getAllNodes();

        // Step 1: Initialize distances to INFINITY for all nodes
        HashMap<String, Double> dist = new HashMap<>();
        HashMap<String, String> previous = new HashMap<>(); // for path reconstruction
        HashSet<String> visited = new HashSet<>();

        for (String id : allNodes.keySet()) {
            dist.put(id, Double.MAX_VALUE);
            previous.put(id, null);
        }
        dist.put(sourceId, 0.0);

        // Step 2: Priority Queue (Min-Heap) — processes lowest-cost node first
        PriorityQueue<NodeCost> pq = new PriorityQueue<>(Comparator.comparingDouble(nc -> nc.cost));
        pq.offer(new NodeCost(sourceId, 0.0));

        System.out.println("\n>> Running Dijkstra's Safe Route Algorithm...");
        if (nightMode.isNightTime()) System.out.println("🌙 Night Mode ON — dark roads heavily penalized!");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // Step 3: Main Dijkstra loop
        while (!pq.isEmpty()) {
            NodeCost current = pq.poll(); // Extract node with minimum cost
            String currentId = current.nodeId;

            if (visited.contains(currentId)) continue; // Skip if already processed
            visited.add(currentId);

            Node currentNode = allNodes.get(currentId);
            System.out.printf("  [OK] Processing: %-20s | Cost so far: %.2f%n", currentNode.name, dist.get(currentId));

            if (currentId.equals(destId)) break; // Reached destination!

            // Step 4: Relax all edges from current node
            for (Edge edge : graph.getNeighbors(currentId)) {
                String neighborId = edge.destination.id;
                if (visited.contains(neighborId)) continue;

                // FEATURE 1: Apply night mode multiplier to the edge weight
                double adjustedWeight = nightMode.applyNightMultiplier(edge.safetyWeight, edge.lighting);

                double newCost = dist.get(currentId) + adjustedWeight;

                // If we found a SAFER path, update it
                if (newCost < dist.get(neighborId)) {
                    dist.put(neighborId, newCost);
                    previous.put(neighborId, currentId);
                    pq.offer(new NodeCost(neighborId, newCost));
                }
            }
        }

        // Step 5: Reconstruct path using backtracking
        List<String> path = reconstructPath(previous, sourceId, destId);

        return new RouteResult(path, dist.get(destId), allNodes);
    }

    /**
     * FEATURE 2: SHORTEST ROUTE (by distance only — ignores safety)
     *
     * This runs standard Dijkstra using raw distance as weight.
     * We compare this with the safe route to show how our system
     * avoids dangerous shortcuts.
     *
     * HOW TO EXPLAIN:
     *   "This is what Google Maps would give you — just the shortest.
     *    Our system gives the safest. Look at the difference!"
     */
    public RouteResult findShortestRoute(String sourceId, String destId) {
        HashMap<String, Node> allNodes = graph.getAllNodes();

        HashMap<String, Double> dist = new HashMap<>();
        HashMap<String, String> previous = new HashMap<>();
        HashSet<String> visited = new HashSet<>();

        for (String id : allNodes.keySet()) {
            dist.put(id, Double.MAX_VALUE);
            previous.put(id, null);
        }
        dist.put(sourceId, 0.0);

        PriorityQueue<NodeCost> pq = new PriorityQueue<>(Comparator.comparingDouble(nc -> nc.cost));
        pq.offer(new NodeCost(sourceId, 0.0));

        System.out.println("\n📏 Running Standard Dijkstra (Shortest Distance)...");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        while (!pq.isEmpty()) {
            NodeCost current = pq.poll();
            String currentId = current.nodeId;
            if (visited.contains(currentId)) continue;
            visited.add(currentId);
            if (currentId.equals(destId)) break;

            for (Edge edge : graph.getNeighbors(currentId)) {
                String neighborId = edge.destination.id;
                if (visited.contains(neighborId)) continue;

                // Use raw DISTANCE — no safety consideration at all
                double newCost = dist.get(currentId) + edge.distance;

                if (newCost < dist.get(neighborId)) {
                    dist.put(neighborId, newCost);
                    previous.put(neighborId, currentId);
                    pq.offer(new NodeCost(neighborId, newCost));
                }
            }
        }

        List<String> path = reconstructPath(previous, sourceId, destId);
        return new RouteResult(path, dist.get(destId), allNodes);
    }

    /**
     * FEATURE 2 (continued): Compare safe vs shortest and print side by side.
     * This is the most visually impressive output for your presentation!
     */
    public void compareRoutes(String sourceId, String destId) {
        System.out.println("\n╔═══════════════════════════════════════════════════╗");
        System.out.println(  "║      SAFE ROUTE vs SHORTEST ROUTE COMPARISON      ║");
        System.out.println(  "╚═══════════════════════════════════════════════════╝");

        RouteResult safeRoute    = findSafestRoute(sourceId, destId);
        RouteResult shortestRoute = findShortestRoute(sourceId, destId);

        HashMap<String, Node> nodes = graph.getAllNodes();

        System.out.println("\n  🛡️  SAFEST ROUTE (Our System — Dijkstra + Safety Weights):");
        System.out.print("     ");
        for (int i = 0; i < safeRoute.path.size(); i++) {
            System.out.print(nodes.get(safeRoute.path.get(i)).name);
            if (i < safeRoute.path.size() - 1) System.out.print(" → ");
        }
        System.out.printf("%n     Safety Cost: %.2f%n", safeRoute.totalSafetyCost);

        System.out.println("\n  📏 SHORTEST ROUTE (Like Google Maps — Distance Only):");
        System.out.print("     ");
        for (int i = 0; i < shortestRoute.path.size(); i++) {
            Node n = nodes.get(shortestRoute.path.get(i));
            String warning = (n.safetyScore <= 3) ? " ⚠️" : "";
            System.out.print(n.name + warning);
            if (i < shortestRoute.path.size() - 1) System.out.print(" → ");
        }
        System.out.printf("%n     Total Distance: %.2f km%n", shortestRoute.totalSafetyCost);

        // Show if the paths are different
        if (!safeRoute.path.equals(shortestRoute.path)) {
            System.out.println("\n  [OK] Our system chose a DIFFERENT (safer) route!");
            System.out.println("  ⚠️  The shortest route passes through unsafe areas (marked ⚠️).");
        } else {
            System.out.println("\n  ℹ️  Both routes are the same — the shortest is also the safest here.");
        }
        System.out.println();
    }

    /** Trace back from destination to source using 'previous' map */
    private List<String> reconstructPath(HashMap<String, String> previous, String source, String dest) {
        LinkedList<String> path = new LinkedList<>();
        String current = dest;

        while (current != null) {
            path.addFirst(current);
            current = previous.get(current);
        }

        if (path.isEmpty() || !path.getFirst().equals(source)) {
            return new ArrayList<>(); // No path found
        }
        return path;
    }

    /** Simple helper class to store (nodeId, cost) in PriorityQueue */
    private static class NodeCost {
        String nodeId;
        double cost;
        NodeCost(String nodeId, double cost) {
            this.nodeId = nodeId;
            this.cost = cost;
        }
    }
}