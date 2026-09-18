package safeRoute;
/*
Represents a road/path between two locations.
Each edge has a physical distance AND a safety weight.
DATA STRUCTURE: Edge (Graph Edge with dual weights)
The SAFETY WEIGHT is the key innovation:
safetyWeight = distance * (11 - avgSafetyScore)
→ Safer roads have LOWER weight → Dijkstra prefers them!
 */
public class Edge {

    public Node destination;    // Where this road leads
    public double distance;     // Physical distance in km
    public double safetyWeight; // Computed weight for Dijkstra (lower = safer)
    public int lighting;        // 1-10: how well-lit the road is
    public int crowdDensity;    // 1-10: crowd level (higher = safer)

    public Edge(Node destination, double distance, int lighting, int crowdDensity) {
        this.destination = destination;
        this.distance = distance;
        this.lighting = lighting;
        this.crowdDensity = crowdDensity;

        // Compute safety weight:
        // We invert the safety score so that SAFER roads have LOWER weight.
        // Dijkstra finds the MINIMUM weight path → it naturally finds the SAFEST path.
        double avgSafety = (destination.safetyScore + lighting + crowdDensity) / 3.0;
        this.safetyWeight = distance * (11 - avgSafety);
    }

    @Override
    public String toString() {
        return String.format("→ %s | Dist: %.1f km | Safety Weight: %.2f", 
            destination.name, distance, safetyWeight);
    }
}
