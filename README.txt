A* CITY ROUTE PLANNER — FINAL MATCHED VERSION

CONTENTS
- AStarCityRoutePlanner.java — Java Swing desktop simulation.
- AStarCityRoutePlanner.html — standalone browser simulation.
- README.txt — setup and usage instructions.

HTML VERSION
1. Double-click AStarCityRoutePlanner.html to open it in Chrome, Edge, or Firefox.
2. Select a Source and Destination.
3. Click Next Step to watch one A* search step, Run A* to finish automatically, or Reset to restart.
No server, internet connection, or external libraries are required.

JAVA VERSION
Requires a JDK.
1. Open this folder in a terminal.
2. Run:
   javac AStarCityRoutePlanner.java
   java AStarCityRoutePlanner

PARITY AND SEARCH DETAILS
Both versions use the same 16 locations, 34 undirected roads, road-cost formula, heuristic, tie-breaking rule, OPEN/CLOSED handling, route reconstruction, and two-decimal display formatting. Road costs and heuristic values are Euclidean distances between illustrative map coordinates divided by 10, so values are readable distance units—not kilometres or live geographic distances.

The Java and HTML implementations were compared across all 256 source/destination combinations. The final path, displayed total cost, and expanded-node count matched in every case. The HTML path-edge highlighting was also corrected to use the same canonical edge keys as the map renderer.

This is an educational graph simulation, not a live navigation service.
