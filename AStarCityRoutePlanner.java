import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * City Route Planner using A* Search
 * Run: javac AStarCityRoutePlanner.java
 *      java AStarCityRoutePlanner
 */
@SuppressWarnings({"serial", "this-escape"})
public class AStarCityRoutePlanner extends JFrame {
    private static final long serialVersionUID = 1L;
    private final CityGraph graph = new CityGraph();
    private final GraphPanel graphPanel = new GraphPanel();
    private final JTextArea details = new JTextArea();
    private final JLabel status = new JLabel("Ready. Select a source and destination, then click Step or Run A*.");
    private final JComboBox<String> sourceBox = new JComboBox<>();
    private final JComboBox<String> goalBox = new JComboBox<>();
    private final JButton stepButton = new JButton("Next Step");
    private final JButton runButton = new JButton("Run A*");
    private final JButton resetButton = new JButton("Reset");

    private AStarSearch search;
    private boolean finished = false;

    public AStarCityRoutePlanner() {
        super("City Route Planner | A* Heuristic Search");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new Dimension(980, 650));
        setLocationRelativeTo(null);

        for (CityNode n : graph.nodes) {
            sourceBox.addItem(n.name);
            goalBox.addItem(n.name);
        }
        sourceBox.setSelectedItem("Central Station");
        goalBox.setSelectedItem("Airport");

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        top.add(new JLabel("Source:"));
        top.add(sourceBox);
        top.add(new JLabel("Destination:"));
        top.add(goalBox);
        top.add(stepButton);
        top.add(runButton);
        top.add(resetButton);

        details.setEditable(false);
        details.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        details.setText("A* combines the cost already travelled g(n) and estimated remaining cost h(n).\n"
                + "f(n) = g(n) + h(n)\n\n"
                + "Choose a source and destination, then click Next Step to watch the search.");
        JScrollPane detailScroll = new JScrollPane(details);
        detailScroll.setPreferredSize(new Dimension(335, 100));

        JPanel right = new JPanel(new BorderLayout(8, 8));
        right.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        right.add(new JLabel("SEARCH TRACE", SwingConstants.LEFT), BorderLayout.NORTH);
        right.add(detailScroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createEmptyBorder(5, 12, 8, 12));
        bottom.add(status, BorderLayout.CENTER);
        JLabel legend = new JLabel("  Blue = open | Grey = closed | Green = final path | Red = source | Orange = goal");
        bottom.add(legend, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);
        add(graphPanel, BorderLayout.CENTER);
        add(right, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        stepButton.addActionListener(e -> doStep());
        runButton.addActionListener(e -> runAll());
        resetButton.addActionListener(e -> resetSearch());
        sourceBox.addActionListener(e -> resetSearch());
        goalBox.addActionListener(e -> resetSearch());

        resetSearch();
    }

    private void resetSearch() {
        String source = (String) sourceBox.getSelectedItem();
        String goal = (String) goalBox.getSelectedItem();
        if (source == null || goal == null) return;
        search = new AStarSearch(graph, source, goal);
        finished = false;
        stepButton.setEnabled(true);
        runButton.setEnabled(true);
        details.setText("A* Search initialized\n"
                + "Source: " + source + "\nDestination: " + goal + "\n\n"
                + "Click Next Step to expand one node at a time, or Run A* to finish automatically.\n\n"
                + "f(n) = g(n) + h(n)\n"
                + "g(n): actual cost from source\n"
                + "h(n): straight-line estimate to destination\n"
                + "f(n): estimated total route cost");
        status.setText("Search reset. Ready to start.");
        graphPanel.repaint();
    }

    private void doStep() {
        if (finished || search == null) return;
        String message = search.step();
        details.setText(search.getTrace());
        details.setCaretPosition(details.getDocument().getLength());
        if (search.isFinished()) {
            finished = true;
            stepButton.setEnabled(false);
            runButton.setEnabled(false);
            status.setText(search.getResultSummary());
        } else {
            status.setText(message);
        }
        graphPanel.repaint();
    }

    private void runAll() {
        if (finished || search == null) return;
        while (!search.isFinished()) search.step();
        details.setText(search.getTrace());
        details.setCaretPosition(details.getDocument().getLength());
        finished = true;
        stepButton.setEnabled(false);
        runButton.setEnabled(false);
        status.setText(search.getResultSummary());
        graphPanel.repaint();
    }

    private static String formatCost(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    @SuppressWarnings("serial")
    private class GraphPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private final Map<String, Point> positions = new HashMap<>();

        GraphPanel() {
            setBackground(new Color(248, 250, 252));
            setPreferredSize(new Dimension(700, 600));
            // Coordinates are relative to a 760 x 620 drawing area.
            put("Central Station", 75, 280);
            put("Old Town", 175, 155);
            put("Museum", 280, 90);
            put("University", 300, 220);
            put("City Hospital", 260, 355);
            put("River Park", 410, 130);
            put("Market Square", 430, 265);
            put("Tech Park", 435, 410);
            put("Stadium", 555, 100);
            put("City Mall", 570, 220);
            put("Bus Terminal", 565, 345);
            put("Lake View", 570, 475);
            put("Industrial Area", 690, 160);
            put("Green Gardens", 700, 285);
            put("Suburb", 705, 420);
            put("Airport", 815, 330);
        }

        private void put(String name, int x, int y) { positions.put(name, new Point(x, y)); }

        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double sx = Math.max(0.72, (getWidth() - 35.0) / 900.0);
            double sy = Math.max(0.72, (getHeight() - 35.0) / 570.0);
            // Use uniform scale so the graph does not become distorted.
            double scale = Math.min(sx, sy);
            int ox = 12, oy = 10;
            g.translate(ox, oy);
            g.scale(scale, scale);

            Set<String> pathEdges = new HashSet<>();
            List<CityNode> path = search == null ? Collections.emptyList() : search.getPath();
            for (int i = 0; i + 1 < path.size(); i++) {
                pathEdges.add(edgeKey(path.get(i).name, path.get(i + 1).name));
            }

            // Edges and edge weights
            for (CityNode n : graph.nodes) {
                for (Edge edge : n.edges) {
                    if (n.name.compareTo(edge.to.name) >= 0) continue;
                    Point a = positions.get(n.name), b = positions.get(edge.to.name);
                    boolean selected = pathEdges.contains(edgeKey(n.name, edge.to.name));
                    g.setStroke(new BasicStroke(selected ? 5f : 1.6f));
                    g.setColor(selected ? new Color(22, 163, 74) : new Color(177, 188, 201));
                    g.drawLine(a.x, a.y, b.x, b.y);
                    int mx = (a.x + b.x) / 2, my = (a.y + b.y) / 2;
                    g.setFont(new Font("SansSerif", Font.PLAIN, 11));
                    g.setColor(new Color(71, 85, 105));
                    g.drawString(formatCost(edge.cost), mx + 3, my - 3);
                }
            }

            // Nodes
            for (CityNode n : graph.nodes) {
                Point p = positions.get(n.name);
                Color fill = new Color(226, 232, 240);
                if (search != null && search.closed.contains(n.name)) fill = new Color(203, 213, 225);
                if (search != null && search.open.containsKey(n.name)) fill = new Color(191, 219, 254);
                if (n.name.equals((String) sourceBox.getSelectedItem())) fill = new Color(252, 165, 165);
                if (n.name.equals((String) goalBox.getSelectedItem())) fill = new Color(253, 186, 116);
                if (search != null && search.getPathNames().contains(n.name)) fill = new Color(134, 239, 172);
                g.setColor(fill);
                g.fillOval(p.x - 13, p.y - 13, 26, 26);
                g.setColor(new Color(51, 65, 85));
                g.setStroke(new BasicStroke(1.4f));
                g.drawOval(p.x - 13, p.y - 13, 26, 26);
                g.setFont(new Font("SansSerif", Font.BOLD, 11));
                g.drawString(n.name, p.x - 22, p.y + 30);
                if (search != null && search.gScore.containsKey(n.name)) {
                    String score = "g=" + formatCost(search.gScore.get(n.name)) + ", h=" + formatCost(search.h(n));
                    g.setFont(new Font("SansSerif", Font.PLAIN, 10));
                    g.setColor(new Color(30, 64, 175));
                    g.drawString(score, p.x - 25, p.y - 19);
                }
            }
            g.dispose();
        }

        private String edgeKey(String a, String b) {
            return a.compareTo(b) < 0 ? a + "|" + b : b + "|" + a;
        }
    }

    private static class CityNode {
        final String name;
        final int x, y;
        final List<Edge> edges = new ArrayList<>();
        CityNode(String name, int x, int y) { this.name = name; this.x = x; this.y = y; }
    }

    private static class Edge {
        final CityNode to;
        final double cost;
        Edge(CityNode to, double cost) { this.to = to; this.cost = cost; }
    }

    private static class CityGraph {
        final List<CityNode> nodes = new ArrayList<>();
        final Map<String, CityNode> byName = new LinkedHashMap<>();

        CityGraph() {
            add("Central Station", 75, 280);
            add("Old Town", 175, 155);
            add("Museum", 280, 90);
            add("University", 300, 220);
            add("City Hospital", 260, 355);
            add("River Park", 410, 130);
            add("Market Square", 430, 265);
            add("Tech Park", 435, 410);
            add("Stadium", 555, 100);
            add("City Mall", 570, 220);
            add("Bus Terminal", 565, 345);
            add("Lake View", 570, 475);
            add("Industrial Area", 690, 160);
            add("Green Gardens", 700, 285);
            add("Suburb", 705, 420);
            add("Airport", 815, 330);

            connect("Central Station", "Old Town");
            connect("Central Station", "University");
            connect("Central Station", "City Hospital");
            connect("Old Town", "Museum");
            connect("Old Town", "University");
            connect("Museum", "University");
            connect("Museum", "River Park");
            connect("Museum", "Stadium");
            connect("University", "City Hospital");
            connect("University", "River Park");
            connect("University", "Market Square");
            connect("City Hospital", "Market Square");
            connect("City Hospital", "Tech Park");
            connect("River Park", "Stadium");
            connect("River Park", "Market Square");
            connect("River Park", "City Mall");
            connect("Market Square", "City Mall");
            connect("Market Square", "Tech Park");
            connect("Market Square", "Bus Terminal");
            connect("Stadium", "Industrial Area");
            connect("Stadium", "City Mall");
            connect("City Mall", "Industrial Area");
            connect("City Mall", "Bus Terminal");
            connect("City Mall", "Green Gardens");
            connect("Tech Park", "Bus Terminal");
            connect("Tech Park", "Lake View");
            connect("Bus Terminal", "Lake View");
            connect("Bus Terminal", "Green Gardens");
            connect("Lake View", "Suburb");
            connect("Green Gardens", "Suburb");
            connect("Industrial Area", "Green Gardens");
            connect("Green Gardens", "Airport");
            connect("Suburb", "Airport");
            connect("Bus Terminal", "Airport");
        }

        private void add(String name, int x, int y) {
            CityNode n = new CityNode(name, x, y);
            nodes.add(n);
            byName.put(name, n);
        }

        // Scale coordinate distances by 10 so scores remain readable.
        // Euclidean edge costs and the straight-line heuristic use the same units.
        private void connect(String a, String b) {
            CityNode na = byName.get(a), nb = byName.get(b);
            double cost = Math.hypot(na.x - nb.x, na.y - nb.y) / 10.0;
            na.edges.add(new Edge(nb, cost));
            nb.edges.add(new Edge(na, cost));
        }
    }

    private static class AStarSearch {
        final CityGraph graph;
        final CityNode start, goal;
        final Map<String, Double> gScore = new HashMap<>();
        final Map<String, String> parent = new HashMap<>();
        final Map<String, CityNode> open = new LinkedHashMap<>();
        final Set<String> closed = new LinkedHashSet<>();
        final List<String> traceLines = new ArrayList<>();
        boolean done = false;
        boolean found = false;
        int stepNumber = 0;
        String lastMessage = "";

        AStarSearch(CityGraph graph, String source, String destination) {
            this.graph = graph;
            start = graph.byName.get(source);
            goal = graph.byName.get(destination);
            gScore.put(start.name, 0.0);
            open.put(start.name, start);
            traceLines.add("A* SEARCH TRACE");
            traceLines.add("Source: " + start.name);
            traceLines.add("Goal:   " + goal.name);
            traceLines.add("Rule: f(n) = g(n) + h(n)");
            traceLines.add("h(n) = straight-line distance to goal");
            traceLines.add("");
            traceLines.add("Step 0: OPEN = {" + start.name + "}");
        }

        // Straight-line distance is an admissible heuristic for this graph.
        double h(CityNode n) {
            return Math.hypot(n.x - goal.x, n.y - goal.y) / 10.0;
        }

        double f(CityNode n) {
            return gScore.getOrDefault(n.name, Double.POSITIVE_INFINITY) + h(n);
        }

        String step() {
            if (done) return lastMessage;
            CityNode current = open.values().stream()
                    .min(Comparator.comparingDouble((CityNode n) -> gScore.getOrDefault(n.name, Double.POSITIVE_INFINITY) + h(n))
                            .thenComparing(n -> n.name)).orElse(null);
            if (current == null) {
                done = true;
                found = false;
                lastMessage = "No route exists between these locations.";
                traceLines.add("OPEN is empty. No route found.");
                return lastMessage;
            }

            open.remove(current.name);
            if (current.name.equals(goal.name)) {
                closed.add(current.name);
                done = true;
                found = true;
                stepNumber++;
                traceLines.add("");
                traceLines.add("Step " + stepNumber + ": Goal selected: " + current.name);
                traceLines.add("Destination reached. Reconstructing route.");
                lastMessage = "Destination reached. Optimal route cost = " + formatCost(gScore.get(goal.name));
                traceLines.add(lastMessage);
                return lastMessage;
            }

            closed.add(current.name);
            stepNumber++;
            traceLines.add("");
            traceLines.add("Step " + stepNumber + ": Expand " + current.name);
            traceLines.add("  g=" + formatCost(gScore.get(current.name)) + ", h=" + formatCost(h(current))
                    + ", f=" + formatCost(f(current)));
            for (Edge edge : current.edges) {
                CityNode neighbour = edge.to;
                double tentativeG = gScore.get(current.name) + edge.cost;
                double previousG = gScore.getOrDefault(neighbour.name, Double.POSITIVE_INFINITY);
                if (tentativeG < previousG) {
                    // A better path was found. Update the parent and path cost.
                    parent.put(neighbour.name, current.name);
                    gScore.put(neighbour.name, tentativeG);

                    // Reopen a node if a better path to it is discovered.
                    // This makes the implementation robust if the heuristic changes.
                    closed.remove(neighbour.name);
                    open.put(neighbour.name, neighbour);

                    traceLines.add("  Update " + neighbour.name + ": g=" + formatCost(tentativeG)
                            + ", h=" + formatCost(h(neighbour))
                            + ", f=" + formatCost(tentativeG + h(neighbour)));
                } else if (closed.contains(neighbour.name)) {
                    traceLines.add("  " + neighbour.name + ": already closed; skip");
                } else {
                    traceLines.add("  " + neighbour.name + ": no better route");
                }
            }
            lastMessage = "Expanded " + current.name + ". OPEN nodes: " + open.size()
                    + " | CLOSED nodes: " + closed.size();
            traceLines.add("OPEN: " + open.keySet());
            traceLines.add("CLOSED: " + closed);
            return lastMessage;
        }

        boolean isFinished() {
            return done;
        }

        List<CityNode> getPath() {
            List<CityNode> result = new ArrayList<>();
            if (!found) {
                return result;
            }

            String at = goal.name;
            while (at != null) {
                CityNode node = graph.byName.get(at);
                if (node == null) {
                    // Defensive guard against a broken parent chain.
                    return Collections.emptyList();
                }
                result.add(node);
                if (at.equals(start.name)) {
                    break;
                }
                at = parent.get(at);
            }

            if (result.isEmpty() || !result.get(result.size() - 1).name.equals(start.name)) {
                return Collections.emptyList();
            }
            Collections.reverse(result);
            return result;
        }

        List<String> getPathNames() {
            List<String> names = new ArrayList<>();
            for (CityNode n : getPath()) names.add(n.name);
            return names;
        }

        String getResultSummary() {
            if (!found) return "Search finished: no route found.";
            return "Route cost: " + formatCost(gScore.get(goal.name)) + " | Path: " + String.join(" → ", getPathNames());
        }

        String getTrace() {
            StringBuilder sb = new StringBuilder();
            for (String line : traceLines) sb.append(line).append('\n');
            if (done && found) {
                sb.append("\nFINAL ROUTE\n");
                sb.append(String.join(" -> ", getPathNames())).append('\n');
                sb.append("Total cost: ").append(formatCost(gScore.get(goal.name))).append('\n');
                sb.append("Nodes expanded: ").append(closed.size()).append('\n');
            }
            return sb.toString();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AStarCityRoutePlanner().setVisible(true));
    }
}
