# SafeCity Navigator

A full-stack city safety platform that finds the **safest** route between two
points — not just the shortest one — using a modified Dijkstra's algorithm weighted
by lighting, crowd density, and time of day. Also includes BFS-based nearest
safe-place search, multi-stop trip planning, live-simulated weather/crowd data,
a safety heatmap, and more.

**➡️ To run the app: open `index.html` in any browser. No install, no build step.**

---

## Folder Structure

```
SafeCityNavigator/
├── index.html                  ← Open this file in a browser to run the app
├── css/
│   └── style.css               ← All app styling
├── js/
│   └── script.js                ← All app logic (Dijkstra, BFS, UI, everything)
├── java-original/               ← The original Java prototypes this project evolved from
│   ├── safeRoute/                ← Safe-route console/UI project (Dijkstra, BFS, SOS, Night Mode)
│   │   ├── Main.java
│   │   ├── SafetyGraph.java
│   │   ├── DijkstraSafeRoute.java
│   │   ├── BFSNearestSafePlace.java
│   │   ├── NightModeManager.java
│   │   ├── EmergencyAlertSystem.java
│   │   ├── TripHistoryLog.java
│   │   ├── Node.java
│   │   ├── Edge.java
│   │   └── RouteResult.java
│   ├── smartCityGuide/           ← City Guide console project (weather, crowd, Trie search)
│   │   └── SmartCitySystem.java
│   └── SafeRouteUI.html          ← The original standalone SafeRoute UI (pre-merge)
└── docs/
    └── SafeCity_Navigator_Interview_Prep.md   ← Full interview/revision notes
```

## How to Run

1. Download / clone this whole folder.
2. Double-click `index.html` — it opens directly in your browser and works
   immediately. `index.html` loads `css/style.css` and `js/script.js` automatically,
   so **keep the folder structure intact** (don't move `index.html` out on its own).

## How to Put This on GitHub

1. Create a new GitHub repository.
2. Upload this entire `SafeCityNavigator` folder (drag-and-drop on GitHub's web
   uploader, or `git add . && git commit -m "Initial commit" && git push`).
3. In the repo, go to **Settings → Pages**, set the source to the `main` branch /
   root folder, and save.
4. GitHub will give you a live link (e.g. `yourusername.github.io/SafeCityNavigator`)
   that runs the app directly in the browser — great for a resume link.

## Tech Stack

- **Frontend:** Plain HTML5, CSS3, vanilla JavaScript (no frameworks, no build step)
- **Rendering:** Inline SVG for the interactive city map
- **Original prototyping:** Java (OOP + core data structures/algorithms)

## Core Algorithms & Data Structures

| Concept | Where |
|---|---|
| Modified Dijkstra (safety-weighted shortest path) | `js/script.js` → `dijkstra()` |
| Standard Dijkstra (distance-only, for comparison) | `js/script.js` → `dijkstraShortest()` |
| BFS (nearest police/hospital) | `js/script.js` → `bfsNearestSafePlace()` |
| Priority Queue / Min-Heap simulation | `js/script.js` → `dijkstra()`, `dijkstraShortest()` |
| Stack (undo/redo trip planning) | `js/script.js` → `addToPlan()`, `undoPlan()`, `redoPlan()` |
| LinkedList-style history (add-first/remove-first) | `js/script.js` → `logTripHistory()`, `undoLastHistory()` |
| Adjacency list (graph representation) | `js/script.js` → `nodes`, `edges` |

See `docs/SafeCity_Navigator_Interview_Prep.md` for the full breakdown, including
the original Java implementations of each concept, complexity analysis, and
interview Q&A.
