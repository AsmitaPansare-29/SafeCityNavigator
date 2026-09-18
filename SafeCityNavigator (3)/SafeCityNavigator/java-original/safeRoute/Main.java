package safeRoute;

import java.util.*;

/**
 * ╔═══════════════════════════════════════════════════════════════╗
 * ║       SMART SAFE ROUTE RECOMMENDATION SYSTEM                 ║
 * ║       Women's Safety Project — DSA Implementation            ║
 * ╚═══════════════════════════════════════════════════════════════╝
 *
 * CORE DATA STRUCTURES USED:
 * ┌─────────────────────────────────────────────────────────────┐
 * │ 1. Graph (Adjacency List)  → Represents the city map       │
 * │ 2. HashMap                 → O(1) node/edge lookup         │
 * │ 3. Priority Queue (MinHeap)→ Core of Dijkstra's algorithm  │
 * │ 4. ArrayList               → Stores edges per node         │
 * │ 5. LinkedList              → Path reconstruction + History │
 * │ 6. Stack                   → Priority-based SOS alerts     │
 * │ 7. HashSet                 → Track visited nodes           │
 * │ 8. Queue (BFS)             → Nearest safe place finder     │
 * └─────────────────────────────────────────────────────────────┘
 *
 * ALGORITHMS: Dijkstra (safe + shortest) + BFS (nearest safe place)
 * Time Complexity:  O((V + E) log V)
 * Space Complexity: O(V + E)
 */
public class Main {

    public static void main(String[] args) {

        printBanner();

        // ── STEP 1: Build the City Safety Graph ──────────────────────────
        SafetyGraph cityMap = new SafetyGraph();
        System.out.println("🏙️  Building city safety map...\n");

        cityMap.addNode(new Node("A", "Home Area",           100, 300, 5,  "normal"));
        cityMap.addNode(new Node("B", "Market Street",       200, 200, 4,  "normal"));
        cityMap.addNode(new Node("C", "Well-Lit Boulevard",  300, 300, 9,  "normal"));
        cityMap.addNode(new Node("D", "Dark Back Alley",     200, 400, 2,  "normal"));
        cityMap.addNode(new Node("E", "City Police Station", 400, 200, 10, "police"));
        cityMap.addNode(new Node("F", "Metro Station",       500, 300, 8,  "metro"));
        cityMap.addNode(new Node("G", "General Hospital",    400, 400, 9,  "hospital"));
        cityMap.addNode(new Node("H", "Office District",     600, 300, 7,  "normal"));
        cityMap.addNode(new Node("I", "Lonely Underpass",    300, 450, 2,  "normal"));
        cityMap.addNode(new Node("J", "Busy Shopping Mall",  500, 450, 8,  "normal"));

        System.out.println("\n🛣️  Adding roads...\n");
        cityMap.addEdge("A", "B", 1.5, 5, 6);
        cityMap.addEdge("A", "D", 1.0, 2, 2);
        cityMap.addEdge("A", "C", 2.0, 9, 8);
        cityMap.addEdge("B", "E", 1.5, 8, 7);
        cityMap.addEdge("B", "D", 0.8, 3, 3);
        cityMap.addEdge("C", "E", 1.2, 9, 8);
        cityMap.addEdge("C", "F", 1.5, 8, 9);
        cityMap.addEdge("D", "I", 0.5, 1, 1);
        cityMap.addEdge("E", "F", 1.0, 9, 8);
        cityMap.addEdge("F", "H", 1.0, 8, 9);
        cityMap.addEdge("F", "J", 1.2, 8, 9);
        cityMap.addEdge("G", "J", 0.8, 7, 6);
        cityMap.addEdge("H", "J", 0.5, 7, 8);
        cityMap.addEdge("I", "G", 1.0, 4, 3);

        cityMap.printGraph();

        // ── FEATURE 1: Night Mode Setup ───────────────────────────────────
        NightModeManager nightMode = new NightModeManager();
        nightMode.setManualNightMode(true); // Force ON for demo (remove for auto-detect)
        nightMode.printStatus();

        // ── STEP 2: Find the Safest Route (with Night Mode) ───────────────
        String source = "A";
        String dest   = "H";

        DijkstraSafeRoute finder = new DijkstraSafeRoute(cityMap, nightMode);

        System.out.println("═══════════════════════════════════════════════════════");
        System.out.printf("🗺️  Finding safest route: %s → %s%n",
            cityMap.getNode(source).name, cityMap.getNode(dest).name);
        System.out.println("═══════════════════════════════════════════════════════");

        RouteResult result = finder.findSafestRoute(source, dest);
        result.printRoute();

        // ── FEATURE 2: Compare Safe vs Shortest Route ─────────────────────
        finder.compareRoutes(source, dest);

        // ── FEATURE 3: Trip History Log ───────────────────────────────────
        TripHistoryLog history = new TripHistoryLog();
        history.saveTrip(
            source, cityMap.getNode(source).name,
            dest,   cityMap.getNode(dest).name,
            result.path, result.totalSafetyCost,
            nightMode.isNightTime()
        );

        // Save a second trip for demo
        RouteResult result2 = finder.findSafestRoute("B", "J");
        history.saveTrip("B", cityMap.getNode("B").name,
                         "J", cityMap.getNode("J").name,
                         result2.path, result2.totalSafetyCost,
                         nightMode.isNightTime());

        history.printHistory();

        // ── FEATURE 4: BFS — Find Nearest Safe Place ──────────────────────
        BFSNearestSafePlace bfsFinder = new BFSNearestSafePlace(cityMap);
        bfsFinder.findNearest("D"); // From the dangerous Dark Alley — find help fast!
        bfsFinder.findNearest("A"); // From Home

        // ── Show Nearby Safe Places ───────────────────────────────────────
        List<Node> safePlaces = cityMap.getSafePlaces();
        result.printSafePlacesNearby(safePlaces);

        // ── Emergency Alert System ────────────────────────────────────────
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🛡️  EMERGENCY ALERT SYSTEM DEMO");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        EmergencyAlertSystem sos = new EmergencyAlertSystem("Priya Sharma");
        sos.addContact("Mom: +91-9876543210");
        sos.addContact("Friend Neha: +91-9123456789");
        sos.addContact("Dad: +91-9988776655");
        sos.triggerSOS(cityMap.getNode(source).name);

        // ── Interactive Menu ──────────────────────────────────────────────
        interactiveMenu(cityMap, finder, history, bfsFinder, nightMode);
    }

    /** Interactive menu — now includes all 4 new features */
    private static void interactiveMenu(SafetyGraph cityMap, DijkstraSafeRoute finder,
                                        TripHistoryLog history, BFSNearestSafePlace bfs,
                                        NightModeManager nightMode) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n╔══════════════════════════════════════════╗");
            System.out.println("║            MAIN MENU                     ║");
            System.out.println("╠══════════════════════════════════════════╣");
            System.out.println("║  1. Find Safest Route                    ║");
            System.out.println("║  2. Compare Safe vs Shortest Route       ║");
            System.out.println("║  3. Find Nearest Safe Place (BFS)        ║");
            System.out.println("║  4. View Trip History                    ║");
            System.out.println("║  5. Undo Last Trip                       ║");
            System.out.println("║  6. Toggle Night Mode                    ║");
            System.out.println("║  7. Quit                                 ║");
            System.out.println("╚══════════════════════════════════════════╝");
            System.out.println("Nodes: A(Home) B(Market) C(Boulevard) D(Alley)");
            System.out.println("       E(Police) F(Metro) G(Hospital) H(Office)");
            System.out.println("       I(Underpass) J(Mall)");
            System.out.print("\nChoose option (1-7): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    System.out.print("Source node: ");
                    String src = scanner.nextLine().trim().toUpperCase();
                    System.out.print("Destination node: ");
                    String dst = scanner.nextLine().trim().toUpperCase();
                    if (cityMap.getNode(src) == null || cityMap.getNode(dst) == null) {
                        System.out.println("❌ Invalid nodes."); break;
                    }
                    RouteResult r = finder.findSafestRoute(src, dst);
                    r.printRoute();
                    // Auto-save to history
                    history.saveTrip(src, cityMap.getNode(src).name,
                            dst, cityMap.getNode(dst).name,
                            r.path, r.totalSafetyCost, nightMode.isNightTime());
                }

                case "2" -> {
                    System.out.print("Source node: ");
                    String s2 = scanner.nextLine().trim().toUpperCase();
                    System.out.print("Destination node: ");
                    String d2 = scanner.nextLine().trim().toUpperCase();
                    if (cityMap.getNode(s2) == null || cityMap.getNode(d2) == null) {
                        System.out.println("❌ Invalid nodes."); break;
                    }
                    finder.compareRoutes(s2, d2);
                }

                case "3" -> {
                    System.out.print("Your current node: ");
                    String loc = scanner.nextLine().trim().toUpperCase();
                    if (cityMap.getNode(loc) == null) { System.out.println("❌ Invalid node."); break; }
                    bfs.findNearest(loc);
                }

                case "4" -> history.printHistory();

                case "5" -> history.undoLastTrip();

                case "6" -> {
                    boolean current = nightMode.isNightTime();
                    nightMode.setManualNightMode(!current);
                    nightMode.printStatus();
                }

                case "7" -> {
                    System.out.println("\n[OK] Stay Safe! 🛡️");
                    scanner.close();
                    return;
                }

                default -> System.out.println("❌ Invalid option. Enter 1–7.");
            }
        }
    }

    private static void printBanner() {
        System.out.println();
        System.out.println("╔═════════════════════════════════════════════╗");
        System.out.println("║   SMART SAFE ROUTE RECOMMENDATION SYSTEM    ║");
        System.out.println("╚═════════════════════════════════════════════╝");
        System.out.println();
    }
}