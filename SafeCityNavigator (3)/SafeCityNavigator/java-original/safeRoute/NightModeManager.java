package safeRoute;

import java.time.LocalTime;

/**
 * ════════════════════════════════════════════════════
 * FEATURE 1: NIGHT MODE
 * ════════════════════════════════════════════════════
 *
 * WHAT IT DOES:
 *   After 8:00 PM, unsafe roads become EVEN MORE costly.
 *   We apply a multiplier to the safety weight of dark roads.
 *
 * HOW TO EXPLAIN IN PRESENTATION:
 *   "At night, a poorly lit road is more dangerous than during the day.
 *    So we multiply its safety weight by 1.8 — making Dijkstra avoid
 *    it even more aggressively at night."
 *
 * DATA USED: LocalTime (Java built-in) — no extra library needed.
 */
public class NightModeManager {

    // Night time = 8:00 PM to 6:00 AM
    private static final LocalTime NIGHT_START = LocalTime.of(20, 0); // 8:00 PM
    private static final LocalTime NIGHT_END   = LocalTime.of(6, 0);  // 6:00 AM

    // How much MORE dangerous unsafe roads are at night
    public static final double NIGHT_MULTIPLIER = 1.8;

    private boolean manualOverride = false;  // lets user force night mode on/off
    private boolean overrideValue  = false;

    /** Check if it is currently night time (auto-detected from system clock) */
    public boolean isNightTime() {
        if (manualOverride) return overrideValue;

        LocalTime now = LocalTime.now();
        // Night = after 8 PM OR before 6 AM
        return now.isAfter(NIGHT_START) || now.isBefore(NIGHT_END);
    }

    /** Force night mode ON or OFF (useful for demos/testing) */
    public void setManualNightMode(boolean on) {
        this.manualOverride = true;
        this.overrideValue  = on;
    }

    /** Reset to auto-detect from system clock */
    public void setAutoMode() {
        this.manualOverride = false;
    }

    /**
     * Apply night mode multiplier to a safety weight.
     *
     * LOGIC:
     *   - If it's night AND the road is poorly lit (lighting < 5):
     *     → multiply weight by 1.8 (road becomes much more "costly")
     *   - Otherwise: return original weight unchanged
     *
     * @param baseWeight  the original safety weight of the edge
     * @param lighting    the lighting score of the road (1–10)
     * @return adjusted weight
     */
    public double applyNightMultiplier(double baseWeight, int lighting) {
        if (isNightTime() && lighting < 5) {
            return baseWeight * NIGHT_MULTIPLIER;
        }
        return baseWeight; // daytime or well-lit road → no change
    }

    /** Print current mode status — useful for terminal output */
    public void printStatus() {
        System.out.println("\n🌙 NIGHT MODE STATUS");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        if (isNightTime()) {
            System.out.println("  🔴 Night Mode: ACTIVE");
            System.out.printf ("  ⚠️  Poorly-lit roads get %.1fx safety penalty%n", NIGHT_MULTIPLIER);
            System.out.println("  💡 Roads with lighting < 5/10 are heavily penalized");
        } else {
            System.out.println("  🟢 Night Mode: INACTIVE (Daytime)");
            System.out.println("  [OK] Normal safety weights in effect");
        }
        System.out.println();
    }
}
