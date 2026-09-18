package safeRoute;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * ════════════════════════════════════════════════════
 * FEATURE 3: TRIP HISTORY LOG (LinkedList)
 * ════════════════════════════════════════════════════
 *
 * WHAT IT DOES:
 *   Saves every route the user searches.
 *   Like a browser history — you can view past trips
 *   and even undo/remove the last one.
 *
 * HOW TO EXPLAIN IN PRESENTATION:
 *   "We use a LinkedList as our history log.
 *    Each node in the LinkedList stores one trip.
 *    New trips are added to the FRONT (most recent first).
 *    We can also remove the last trip — like an UNDO feature."
 *
 * DATA STRUCTURE: LinkedList (Doubly Linked)
 *   → addFirst()   → O(1) — add new trip at front
 *   → removeLast() → O(1) — undo last trip
 *   → Iterate      → O(n) — display all history
 */
public class TripHistoryLog {

    /**
     * Inner class representing one saved trip.
     * This is what each node in our LinkedList stores.
     */
    public static class TripEntry {
        public String from;
        public String to;
        public String fromName;
        public String toName;
        public List<String> path;
        public double safetyCost;
        public String timestamp;
        public boolean nightMode;

        public TripEntry(String from, String fromName,
                         String to,   String toName,
                         List<String> path, double safetyCost, boolean nightMode) {
            this.from       = from;
            this.fromName   = fromName;
            this.to         = to;
            this.toName     = toName;
            this.path       = path;
            this.safetyCost = safetyCost;
            this.nightMode  = nightMode;
            // Save time of search
            this.timestamp  = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
        }
    }

    // The LinkedList that stores all trip entries
    private final  LinkedList<TripEntry> history;

    public TripHistoryLog() {
        history = new LinkedList<>();
    }

    /**
     * Save a new trip to history.
     * addFirst() → most recent trip always at the top.
     */
    public void saveTrip(String from, String fromName,
                         String to,   String toName,
                         List<String> path, double cost, boolean nightMode) {
        TripEntry entry = new TripEntry(from, fromName, to, toName, path, cost, nightMode);
        history.addFirst(entry); // O(1) — add to front of LinkedList
        System.out.println("[SAVE] Trip saved to history: " + fromName + " → " + toName);
    }

    /**
     * Undo: remove the most recently added trip.
     * removeFirst() → O(1)
     */
    public void undoLastTrip() {
        if (history.isEmpty()) {
            System.out.println("⚠️  No trips in history to undo.");
            return;
        }
        TripEntry removed = history.removeFirst();
        System.out.println("↩️  Removed from history: " + removed.fromName + " → " + removed.toName);
    }

    /**
     * Print all saved trips — most recent first.
     * Iterates through the LinkedList O(n).
     */
    public void printHistory() {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║           [LOG] TRIP HISTORY LOG           ║");
        System.out.println("╚══════════════════════════════════════════╝");

        if (history.isEmpty()) {
            System.out.println("  (No trips saved yet)");
            return;
        }

        int index = 1;
        for (TripEntry entry : history) {  // iterates front → back
            System.out.printf("%n  Trip #%d  [%s]%n", index++, entry.timestamp);
            System.out.printf("  --> From : %s%n", entry.fromName);
            System.out.printf("  🏁 To   : %s%n", entry.toName);
            System.out.printf("  🛤️  Path : %s%n", String.join(" → ", entry.path));
            System.out.printf("  📊 Safety Cost: %.2f%n", entry.safetyCost);
            System.out.printf("  [NIGHT] Night Mode : %s%n", entry.nightMode ? "ON" : "OFF");
        }
        System.out.println();
    }

    /** Total number of trips saved */
    public int size() {
        return history.size();
    }

    public boolean isEmpty() {
        return history.isEmpty();
    }
}