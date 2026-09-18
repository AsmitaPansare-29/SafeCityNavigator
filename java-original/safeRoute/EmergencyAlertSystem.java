package safeRoute;

import java.util.*;

/**
 * Simulates sending emergency alerts to contacts and nearby services.
 *
 * In a real system, this would:
 *   → Send SMS via Twilio API
 *   → Trigger GPS location sharing
 *   → Notify police/hospital APIs
 *
 * For this project: simulates the alert with realistic console output.
 */
public class EmergencyAlertSystem {

    private final List<String> emergencyContacts;
    private final String userName;

    public EmergencyAlertSystem(String userName) {
        this.userName = userName;
        this.emergencyContacts = new ArrayList<>();
    }

    public void addContact(String contact) {
        emergencyContacts.add(contact);
    }

    /**
     * Simulate sending SOS alert to all contacts.
     * Uses a Stack to simulate "last added contact gets priority" (LIFO).
     *
     * DATA STRUCTURE: Stack
     * Stack = Last In, First Out → closest/most recently added contact notified first
     */
    public void triggerSOS(String currentLocation) {
        System.out.println("\n🚨🚨🚨 SOS ALERT TRIGGERED 🚨🚨🚨");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("User: " + userName);
        System.out.println("Location: " + currentLocation);
        System.out.println("Time: " + new Date());
        System.out.println();

        // Stack-based alert: most recently added contact gets alert first
        Stack<String> alertStack = new Stack<>();
        for (String contact : emergencyContacts) {
            alertStack.push(contact);
        }

        System.out.println("📱 Notifying emergency contacts:");
        int priority = 1;
        while (!alertStack.isEmpty()) {
            String contact = alertStack.pop();
            System.out.printf("  [Priority %d] ✉️  Sending SOS to: %s ... SENT [OK]%n", priority++, contact);
            simulateDelay(300);
        }

        System.out.println("\n🚔 Alerting nearest police station ... DONE [OK]");
        System.out.println("🏥 Alerting nearest hospital ... DONE [OK]");
        System.out.println("\n[OK] All alerts sent. Stay safe!");
    }

    /** Simulate network delay for realism */
    private void simulateDelay(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { /* ignore */ }
    }
}
