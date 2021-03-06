import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


/**
 * This class provides a shortestPath method for finding routes between two points
 * on the map. It uses A* to find the route quickly
 */
public class Router {
    /**
     * Return a List of longs representing the shortest path from the node
     * closest to a start location and the node closest to the destination
     * location.
     *
     * @param g       The graph to use.
     * @param stlon   The longitude of the start location.
     * @param stlat   The latitude of the start location.
     * @param destlon The longitude of the destination location.
     * @param destlat The latitude of the destination location.
     * @return A list of node id's in the order visited on the shortest path.
     */

    // Start by implementing Dijkstra's Algorithm
    public static List<Long> shortestPath(GraphDB g, double stlon, double stlat,
                                          double destlon, double destlat) {
        // @Source JHug's approach 2 to A* was extremely helpful in building this out!
        // Also the slides on Dijsktra's pseudocode.

        long startID = g.closest(stlon, stlat);
        long endID = g.closest(destlon, destlat);

        // Needed to create a special class, NodeComparable, such that
        // we could use a PQ implementation w/ distance as comparable
        // --much like in HW 4
        PriorityQueue<NodeComparable> fringe = new PriorityQueue<>();
        fringe.add(new NodeComparable(startID, 0));

        // Same idea as edgeTo
        HashMap<Long, Long> bestPath = new HashMap<>();

        // Tracks distance from starting node to where we are
        // in algorithm iteration.
        HashMap<Long, Double> distTo = new HashMap<>();
        distTo.put(startID, 0.0);

        // A* Algorithm implementation:
        while (true) {
            // Get smallest element in fringe
            NodeComparable v = fringe.peek();

            // Iterate through its adjacent neighbours
            for (long w : g.adj.get(v.id)) {
                double distanceFromStart = distTo.get(v.id)
                        + distance(g.lat(v.id), g.lon(v.id), g.lat(w), g.lon(w));
//                System.out.println(distTo.get(w));

                // Check if the distance from start is better OR if there is a null value (meaning
                // it's never been checked)
                if ((distTo.get(w) == null) || (distanceFromStart < distTo.get(w))) {
                    distTo.put(w, distanceFromStart);
//                    System.out.println(v.id);
                    bestPath.put(w, v.id);

                    // don't forget to add heuristic (aka g.distance(endID, w))
                    fringe.add(new NodeComparable(w, distanceFromStart + g.distance(endID, w)));
                }
            }
            // Remove node if we have considered it
            // NOTE: there are some roads that do NOT
            // connect to others so we must handle it w catch-try
            try {
                fringe.remove();
            } catch (NullPointerException e) {
                System.out.println("No valid path to that destination; try flying");
            }

            // End iteration if we hit the end
            if (v.id == endID) {
                break;
            }
            // remove was here (in case weird error but its ok i think)
        }

        // Now need to go backwards through our best list until
        // we hit the starting node. This gives us desired path
        LinkedList<Long> optimalPath = new LinkedList<>();
        optimalPath.addFirst(endID);
        long previousNode = bestPath.get(endID);
        while (previousNode != startID) {
            optimalPath.addFirst(previousNode);
            previousNode = bestPath.get(previousNode);
        }
        optimalPath.addFirst(startID);
        return optimalPath;
    }


    /* Need this for our PQ to actually work with the ID -> distance tuple
    /* Idea sourced from our lecture on D's Algo & HW4*/
    private static class NodeComparable implements Comparable<NodeComparable> {
        long id;
        double dist;

        NodeComparable(long id, double dist) {
            this.id = id;
            this.dist = dist;
        }

        @Override
        public int compareTo(NodeComparable otherNode) {
            if (this.dist > otherNode.dist) {
                return 1;
            } else if (this.dist == otherNode.dist) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    private static double distance(double lat1, double lon1, double lat2, double lon2) {
        // Added to support distance for coordinates NOT in a graph
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double dphi = Math.toRadians(lat2 - lat1);
        double dlambda = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dphi / 2.0) * Math.sin(dphi / 2.0);
        a += Math.cos(phi1) * Math.cos(phi2) * Math.sin(dlambda / 2.0) * Math.sin(dlambda / 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 3963 * c;
    }

    /**
     * Create the list of directions corresponding to a route on the graph.
     *
     * @param g     The graph to use.
     * @param route The route to translate into directions. Each element
     *              corresponds to a node from the graph in the route.
     * @return A list of NavigatiionDirection objects corresponding to the input
     * route.
     */
    public static List<NavigationDirection> routeDirections(GraphDB g, List<Long> route) {
        return null; // FIXME
    }


    /**
     * Class to represent a navigation direction, which consists of 3 attributes:
     * a direction to go, a way, and the distance to travel for.
     */
    public static class NavigationDirection {

        /**
         * Integer constants representing directions.
         */
        public static final int START = 0;
        public static final int STRAIGHT = 1;
        public static final int SLIGHT_LEFT = 2;
        public static final int SLIGHT_RIGHT = 3;
        public static final int RIGHT = 4;
        public static final int LEFT = 5;
        public static final int SHARP_LEFT = 6;
        public static final int SHARP_RIGHT = 7;

        /**
         * Number of directions supported.
         */
        public static final int NUM_DIRECTIONS = 8;

        /**
         * A mapping of integer values to directions.
         */
        public static final String[] DIRECTIONS = new String[NUM_DIRECTIONS];

        /**
         * Default name for an unknown way.
         */
        public static final String UNKNOWN_ROAD = "unknown road";

        /** Static initializer. */
        static {
            DIRECTIONS[START] = "Start";
            DIRECTIONS[STRAIGHT] = "Go straight";
            DIRECTIONS[SLIGHT_LEFT] = "Slight left";
            DIRECTIONS[SLIGHT_RIGHT] = "Slight right";
            DIRECTIONS[LEFT] = "Turn left";
            DIRECTIONS[RIGHT] = "Turn right";
            DIRECTIONS[SHARP_LEFT] = "Sharp left";
            DIRECTIONS[SHARP_RIGHT] = "Sharp right";
        }

        /**
         * The direction a given NavigationDirection represents.
         */
        int direction;
        /**
         * The name of the way I represent.
         */
        String way;
        /**
         * The distance along this way I represent.
         */
        double distance;

        /**
         * Create a default, anonymous NavigationDirection.
         */
        public NavigationDirection() {
            this.direction = STRAIGHT;
            this.way = UNKNOWN_ROAD;
            this.distance = 0.0;
        }

        public String toString() {
            return String.format("%s on %s and continue for %.3f miles.",
                    DIRECTIONS[direction], way, distance);
        }

        /**
         * Takes the string representation of a navigation direction and converts it into
         * a Navigation Direction object.
         * @param dirAsString The string representation of the NavigationDirection.
         * @return A NavigationDirection object representing the input string.
         */
        public static NavigationDirection fromString(String dirAsString) {
            String regex = "([a-zA-Z\\s]+) on ([\\w\\s]*) and continue for ([0-9\\.]+) miles\\.";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(dirAsString);
            NavigationDirection nd = new NavigationDirection();
            if (m.matches()) {
                String direction = m.group(1);
                if (direction.equals("Start")) {
                    nd.direction = NavigationDirection.START;
                } else if (direction.equals("Go straight")) {
                    nd.direction = NavigationDirection.STRAIGHT;
                } else if (direction.equals("Slight left")) {
                    nd.direction = NavigationDirection.SLIGHT_LEFT;
                } else if (direction.equals("Slight right")) {
                    nd.direction = NavigationDirection.SLIGHT_RIGHT;
                } else if (direction.equals("Turn right")) {
                    nd.direction = NavigationDirection.RIGHT;
                } else if (direction.equals("Turn left")) {
                    nd.direction = NavigationDirection.LEFT;
                } else if (direction.equals("Sharp left")) {
                    nd.direction = NavigationDirection.SHARP_LEFT;
                } else if (direction.equals("Sharp right")) {
                    nd.direction = NavigationDirection.SHARP_RIGHT;
                } else {
                    return null;
                }

                nd.way = m.group(2);
                try {
                    nd.distance = Double.parseDouble(m.group(3));
                } catch (NumberFormatException e) {
                    return null;
                }
                return nd;
            } else {
                // not a valid nd
                return null;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof NavigationDirection) {
                return direction == ((NavigationDirection) o).direction
                        && way.equals(((NavigationDirection) o).way)
                        && distance == ((NavigationDirection) o).distance;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(direction, way, distance);
        }
    }
}
