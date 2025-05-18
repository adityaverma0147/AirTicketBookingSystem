package org.javalab;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class AirTicketBookingSystem {

    static final String[] cities = {"New York", "London", "Paris", "Tokyo", "Sydney"};
    static final int[][] flightPrices = {
            {0, 500, 700, 600, 800},
            {500, 0, 200, 450, 600},
            {700, 200, 0, 300, 500},
            {600, 450, 300, 0, 400},
            {800, 600, 500, 400, 0}
    };

    public static void main(String[] args) {
        Frame frame = new Frame("Air Ticket Booking System");
        frame.setSize(650, 600);
        frame.setLayout(null);

        Label titleLabel = new Label("Air Ticket Booking System", Label.CENTER);
        titleLabel.setBounds(150, 40, 300, 30);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        frame.add(titleLabel);

        Label sourceLabel = new Label("Source City:");
        sourceLabel.setBounds(50, 80, 100, 20);
        frame.add(sourceLabel);

        Choice sourceChoice = new Choice();
        sourceChoice.setBounds(180, 80, 150, 20);
        for (String city : cities) sourceChoice.add(city);
        frame.add(sourceChoice);

        Label destLabel = new Label("Destination City:");
        destLabel.setBounds(50, 110, 120, 20);
        frame.add(destLabel);

        Choice destChoice = new Choice();
        destChoice.setBounds(180, 110, 150, 20);
        for (String city : cities) destChoice.add(city);
        frame.add(destChoice);

        Label dateLabel = new Label("Travel Date:");
        dateLabel.setBounds(50, 140, 100, 20);
        frame.add(dateLabel);

        TextField dateField = new TextField("DD/MM/YYYY");
        dateField.setBounds(180, 140, 150, 20);
        frame.add(dateField);

        Label seatLabel = new Label("Seat Type:");
        seatLabel.setBounds(50, 170, 100, 20);
        frame.add(seatLabel);

        Choice seatChoice = new Choice();
        seatChoice.setBounds(180, 170, 150, 20);
        seatChoice.add("Window");
        seatChoice.add("Aisle");
        seatChoice.add("Middle");
        frame.add(seatChoice);

        Label routeLabel = new Label("Route Preference:");
        routeLabel.setBounds(50, 200, 120, 20);
        frame.add(routeLabel);

        Choice routeChoice = new Choice();
        routeChoice.setBounds(180, 200, 150, 20);
        routeChoice.add("Shortest Path (Faster, Expensive)");
        routeChoice.add("General Path (Slower, Cheaper)");
        frame.add(routeChoice);

        Button bookButton = new Button("Book Flight");
        bookButton.setBounds(180, 230, 120, 30);
        frame.add(bookButton);

        Label confirmationLabel = new Label();
        confirmationLabel.setBounds(50, 270, 550, 30);
        frame.add(confirmationLabel);

        GraphCanvas graphCanvas = new GraphCanvas(cities, flightPrices);
        graphCanvas.setBounds(20, 310, 600, 230);
        frame.add(graphCanvas);

        bookButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String sourceCity = sourceChoice.getSelectedItem();
                String destCity = destChoice.getSelectedItem();
                String seatType = seatChoice.getSelectedItem();
                String routePref = routeChoice.getSelectedItem();
                String date = dateField.getText();

                int source = Arrays.asList(cities).indexOf(sourceCity);
                int dest = Arrays.asList(cities).indexOf(destCity);

                if (source == dest) {
                    confirmationLabel.setText("Source and destination cannot be the same.");
                    graphCanvas.setHighlightedPath(new ArrayList<>());
                    return;
                }

                List<String> path;
                int basePrice;

                if (routePref.startsWith("Shortest")) {
                    path = findShortestPath(source, dest);
                    basePrice = calculatePathPrice(path);
                } else {
                    path = findGeneralPath(source, dest);
                    if (path.isEmpty()) {
                        confirmationLabel.setText("No path found for General route.");
                        graphCanvas.setHighlightedPath(new ArrayList<>());
                        return;
                    }
                    basePrice = (int) (calculatePathPrice(path) * 0.7); // 30% cheaper
                }

                int seatPrice = seatType.equals("Window") ? 50 : seatType.equals("Aisle") ? 30 : 10;
                int totalPrice = basePrice + seatPrice;

                graphCanvas.setHighlightedPath(path);

                confirmationLabel.setText("Booked: " + sourceCity + " → " + destCity +
                        " on " + date + " | " + routePref +
                        " | Seat: " + seatType + " | Price: Rs." + totalPrice);
            }
        });

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                frame.dispose();
            }
        });

        frame.setVisible(true);
    }

    static List<String> findShortestPath(int source, int dest) {
        int n = cities.length;
        int[] dist = new int[n];
        boolean[] visited = new boolean[n];
        int[] prev = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(prev, -1);
        dist[source] = 0;

        for (int i = 0; i < n; i++) {
            int u = -1;
            for (int j = 0; j < n; j++) {
                if (!visited[j] && (u == -1 || dist[j] < dist[u])) u = j;
            }
            if (u == -1 || dist[u] == Integer.MAX_VALUE) break;
            visited[u] = true;

            for (int v = 0; v < n; v++) {
                if (flightPrices[u][v] > 0 && dist[u] + flightPrices[u][v] < dist[v]) {
                    dist[v] = dist[u] + flightPrices[u][v];
                    prev[v] = u;
                }
            }
        }

        LinkedList<String> path = new LinkedList<>();
        for (int at = dest; at != -1; at = prev[at]) path.addFirst(cities[at]);
        return path;
    }

    // Fixed findGeneralPath to mark visited upon enqueueing, not upon dequeuing
    static List<String> findGeneralPath(int source, int dest) {
        boolean[] visited = new boolean[cities.length];
        Queue<List<Integer>> queue = new LinkedList<>();
        queue.offer(Collections.singletonList(source));

        while (!queue.isEmpty()) {
            List<Integer> path = queue.poll();
            int last = path.get(path.size() - 1);

            if (last == dest && path.size() >= 3) { // Path has at least one intermediate city
                List<String> result = new ArrayList<>();
                for (int node : path) result.add(cities[node]);
                return result;
            }

            visited[last] = true;

            for (int i = 0; i < cities.length; i++) {
                if (flightPrices[last][i] > 0 && !visited[i]) {
                    if (!path.contains(i)) {
                        List<Integer> newPath = new ArrayList<>(path);
                        newPath.add(i);
                        queue.offer(newPath);
                    }
                }
            }
        }
        // If no path with intermediate city found, try direct path (length 2)
        if (flightPrices[source][dest] > 0) {
            return Arrays.asList(cities[source], cities[dest]);
        }
        return Collections.emptyList(); // No path found at all
    }
    static int calculatePathPrice(List<String> path) {
        int total = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            int from = Arrays.asList(cities).indexOf(path.get(i));
            int to = Arrays.asList(cities).indexOf(path.get(i + 1));
            total += flightPrices[from][to];
        }
        return total;
    }
}

class GraphCanvas extends Canvas {
    private final String[] cities;
    private final int[][] edges;
    private final Map<String, Point> cityPoints = new HashMap<>();
    private List<String> highlightedPath = new ArrayList<>();

    public GraphCanvas(String[] cities, int[][] edges) {
        this.cities = cities;
        this.edges = edges;
        setSize(600, 200);
    }

    public void setHighlightedPath(List<String> path) {
        this.highlightedPath = path;
        repaint();
    }

    public void paint(Graphics g) {
        int radius = 30;
        int centerX = 300;
        int centerY = 100;
        int numCities = cities.length;
        int circleRadius = 100;

        cityPoints.clear();
        for (int i = 0; i < numCities; i++) {
            double angle = 2 * Math.PI * i / numCities;
            int x = centerX + (int)(circleRadius * Math.cos(angle));
            int y = centerY + (int)(circleRadius * Math.sin(angle));
            cityPoints.put(cities[i], new Point(x, y));
        }

        // Draw all edges in gray
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < numCities; i++) {
            for (int j = i + 1; j < numCities; j++) {
                if (edges[i][j] > 0) {
                    Point p1 = cityPoints.get(cities[i]);
                    Point p2 = cityPoints.get(cities[j]);
                    g.drawLine(p1.x, p1.y, p2.x, p2.y);
                }
            }
        }

        // Draw highlighted path
        g.setColor(Color.RED);
        for (int i = 0; i < highlightedPath.size() - 1; i++) {
            Point p1 = cityPoints.get(highlightedPath.get(i));
            Point p2 = cityPoints.get(highlightedPath.get(i + 1));
            g.drawLine(p1.x, p1.y, p2.x, p2.y);
        }

        // Draw cities
        for (String city : cities) {
            Point p = cityPoints.get(city);
            g.setColor(Color.BLUE);
            g.fillOval(p.x - radius / 2, p.y - radius / 2, radius, radius);
            g.setColor(Color.WHITE);
            g.drawString(city, p.x - city.length() * 3, p.y + 5);
        }
    }
}
