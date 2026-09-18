# 🛡️ Smart Safe Route Recommendation System
### Women's Safety — DSA Project

> *"Not the shortest route. The safest one."*

---

## 📌 Problem Statement

Women often face safety concerns while traveling, especially at night or in unfamiliar areas. Existing navigation apps (Google Maps, etc.) only optimize for **shortest time or distance** — they completely ignore **safety factors** like:

- 🔦 Street lighting
- 👥 Crowd density  
- 🚔 Proximity to police stations
- 📊 Area crime history

This project builds an intelligent system that recommends the **safest route**, not just the shortest one.

---

## 💡 How It Works

```
User selects Start → End location
         ↓
City map loaded as a weighted GRAPH
         ↓
Each road gets a SAFETY WEIGHT (not just distance)
         ↓
Modified Dijkstra's Algorithm runs
         ↓
SAFEST path returned (avoids dark alleys, unsafe zones)
         ↓
Nearby police stations & hospitals displayed
         ↓
Emergency SOS alert system available
```

---

## 🧠 Core Data Structures Used

| Data Structure | Where Used | Why |
|---|---|---|
| **Graph (Adjacency List)** | City map representation | Efficient for sparse city networks |
| **HashMap** | Node/edge lookup | O(1) average time access |
| **Priority Queue (Min-Heap)** | Dijkstra's algorithm | Always process safest node next |
| **ArrayList** | Storing edges per node | Dynamic, cache-friendly |
| **LinkedList** | Path reconstruction | Efficient front-insertion (prepend) |
| **Stack** | Emergency SOS alerts | LIFO — closest contact notified first |
| **HashSet** | Visited nodes in Dijkstra | O(1) membership check |

---

## 🔑 Key Algorithm: Modified Dijkstra

**Standard Dijkstra** minimizes distance.  
**Our Dijkstra** minimizes **safety weight**:

```
safetyWeight = distance × (11 - avgSafetyScore)
```

- A road with safety score **9/10** → low weight → **Dijkstra prefers it** [OK]
- A dark alley with score **2/10** → high weight → **Dijkstra avoids it** [X]

**Time Complexity:** O((V + E) log V)  
**Space Complexity:** O(V + E)

---

## 📁 Project Structure

```
SafeRoute/
└── src/
    └── safeRoute/
        ├── Main.java                # Entry point + interactive menu
        ├── Node.java                # Graph vertex (location)
        ├── Edge.java                # Graph edge (road with safety weight)
        ├── SafetyGraph.java         # Adjacency list graph + helper methods
        ├── DijkstraSafeRoute.java   # Core algorithm implementation
        ├── RouteResult.java         # Route output + formatting
        └── EmergencyAlertSystem.java # SOS alert system (Stack-based)
```

---

## 🚀 How to Run

### Prerequisites
- Java JDK 8 or higher

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/YOUR_USERNAME/SafeRoute.git
cd SafeRoute

# 2. Compile all Java files
javac -d out src/safeRoute/*.java

# 3. Run the program
java -cp out safeRoute.Main
```

---

## 🗺️ Sample City Map

```
[A] Home ──────────── [C] Well-Lit Boulevard ──── [E] Police Station
   |                          |                           |
   |                         [F] Metro ──────────────── [H] Office (DEST)
   |                          |
[D] Dark Alley ──── [I] Underpass ──── [G] Hospital ──── [J] Mall
```

**The system avoids D → I even though it's shorter, because it's UNSAFE.**  
**It chooses A → C → E → F → H — longer but SAFE.**

---

## 🆘 Emergency Alert System

- Stores contacts using a **Stack** (LIFO priority)
- Closest/most recently added contact is notified first
- Simulates SMS + GPS location sharing
- Alerts nearest police station and hospital

---

## 📊 Features

- [OK] Safety-weighted shortest path (Modified Dijkstra)
- [OK] Interactive route finder (try any source → destination)
- [OK] Nearby safe places display (police, hospital)
- [OK] Emergency SOS alert system
- [OK] Visual UI dashboard (open `SafeRouteUI.html` in browser)
- [OK] Fully commented code — explainable for presentations

---

## 👥 Team

| Member | Role |
|---|---|
| [Your Name] | Graph Design + Dijkstra Algorithm |
| [Member 2] | Node/Edge modelling + Safety Scoring |
| [Member 3] | Emergency Alert System + UI |
| [Member 4] | Testing + Documentation |

---

## 🎓 Academic Context

This project demonstrates practical application of:
- **Graph Theory** (weighted directed graphs)
- **Greedy Algorithms** (Dijkstra's shortest/safest path)
- **Priority Queues / Heaps** (efficient node selection)
- **Hash-based data structures** (fast lookup)

---

*Built with ❤️ for Women's Safety | DSA Project*
