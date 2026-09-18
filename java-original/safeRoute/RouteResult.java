package safeRoute;

import java.util.*;

/**
 * Holds the result of a safe route computation.
 * Contains the path (list of node IDs), total safety cost,
 * and helper methods to print the result nicely.
 */
public class RouteResult {

    public List<String> path;
    public double totalSafetyCost;
    private final HashMap<String, Node> allNodes;

    public RouteResult(List<String> path, double totalSafetyCost, HashMap<String, Node> allNodes) {
        this.path = path;
        this.totalSafetyCost = totalSafetyCost;
        this.allNodes = allNodes;
    }

    public boolean pathFound() {
        return path != null && path.size() > 1;
    }

    /** Print the route in a nice readable format */
    public void printRoute() {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║         🛡️  SAFEST ROUTE FOUND           ║");
        System.out.println("╚══════════════════════════════════════════╝");

        if (!pathFound()) {
            System.out.println("[X] No safe route found between these locations.");
            return;
        }

        System.out.println();
        for (int i = 0; i < path.size(); i++) {
            Node node = allNodes.get(path.get(i));
            String icon = getIcon(node.type);
            System.out.printf("  %s Step %d: %-25s [Safety: %d/10]%n",
                icon, i + 1, node.name, node.safetyScore);
            if (i < path.size() - 1) {
                System.out.println("       ↓");
            }
        }

        System.out.println();
        System.out.printf("  📊 Total Safety Score: %.2f (lower = safer path taken)%n", totalSafetyCost);
        System.out.println("  [OK] Route optimized for maximum safety!");
        System.out.println();
    }

    /** Print emergency/safe places along the route */
    public void printSafePlacesNearby(List<Node> safePlaces) {
        System.out.println("🆘 NEARBY SAFE PLACES:");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        for (Node place : safePlaces) {
            String icon = getIcon(place.type);
            System.out.printf("  %s %-25s [Safety: %d/10]%n", icon, place.name, place.safetyScore);
        }
    }

    private String getIcon(String type) {
        return switch (type) {
            case "police" -> "👮";
            case "hospital" -> "🏥";
            case "metro" -> "🚇";
            default -> "-->";
        };
    }

    /** Returns path as a string — useful for frontend/UI */
    public String getPathAsString() {
        if (!pathFound()) return "No route found";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            sb.append(path.get(i));
            if (i < path.size() - 1) sb.append(" → ");
        }
        return sb.toString();
    }
}
