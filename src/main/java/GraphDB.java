import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Alan Yao, Josh Hug
 */
public class GraphDB {
    /** Your instance variables for storing the graph. You should consider
     * creating helper classes, e.g. Node, Edge, etc. */

    /**
     * Example constructor shows how to create and start an XML parser.
     * You do not need to modify this constructor, but you're welcome to do so.
     *
     * @param dbPath Path to the XML file to be parsed.
     */
    // @Source Princeton Graph Implementation in our textbook for inspiration

    // HashMap of all Node IDs and their adjacent Nodes (key: ID, value: List of adj IDs)
    HashMap<Long, ArrayList<Long>> adj = new HashMap<>();

    // HashMap of all Nodes and their coordinates(key: ID, value: List (2 items) of lat, lon)
    HashMap<Long, double[]> nodes = new HashMap<>();

    // Attempt at a spacial hashmap
    // @Source jHug on a piazza comment gave me the idea
    private HashMap<Integer, double[]> spatialHash = new HashMap<>();

    HashMap<String, LinkedList<double[]>> locations = new HashMap<>();

    Trie locationNameTrie = new Trie();

    private int numVerts;


    // Deprecated
//    private class Node {
//        long id;
//        double lon;
//        double lat;
//
//        Node(long id, int lat, int lon) {
//            this.id = id;
//            this.lat = lat;
//            this.lon = lon;
//        }
//    }

    public GraphDB(String dbPath) {
        try {
            File inputFile = new File(dbPath);
            FileInputStream inputStream = new FileInputStream(inputFile);
//             GZIPInputStream stream = new GZIPInputStream(inputStream);

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            saxParser.parse(inputStream, gbh);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }

    // Use this in distance as lazy way to instantiate a node
    public GraphDB(double lat, double lon) {
        this.addNode(1, lat, lon);
    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     *
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     * Remove nodes with no connections from the graph.
     * While this does not guarantee that any two nodes in the remaining graph are connected,
     * we can reasonably assume this since typically roads are connected.
     */
    void clean() {
        // hint from vid look at adj list
        for (Long x : adj.keySet()) {
            if (adj.get(x).size() == 0 || adj.get(x) == null) {
                deleteNode(x);
            }
        }
    }

    /**
     * Returns an iterable of all vertex IDs in the graph.
     *
     * @return An iterable of id's of all vertices in the graph.
     */
    Iterable<Long> vertices() {
        //YOUR CODE HERE, this currently returns only an empty list.
//        return new ArrayList<Long>();
        return new ArrayList<Long>(nodes.keySet());
    }

    int V() {
        return numVerts;
    }

    /**
     * Returns ids of all vertices adjacent to v.
     *
     * @param v The id of the vertex we are looking adjacent to.
     * @return An iterable of the ids of the neighbors of v.
     */
    Iterable<Long> adjacent(long v) {
        return new ArrayList<Long>(adj.get(v));

    }

    /**
     * Returns the great-circle distance between vertices v and w in miles.
     * Assumes the lon/lat methods are implemented properly.
     * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
     *
     * @param v The id of the first vertex.
     * @param w The id of the second vertex.
     * @return The great-circle distance between the two locations from the graph.
     */
    double distance(long v, long w) {
        return distance(lon(v), lat(v), lon(w), lat(w));
    }

    static double distance(double lonV, double latV, double lonW, double latW) {
        double phi1 = Math.toRadians(latV);
        double phi2 = Math.toRadians(latW);
        double dphi = Math.toRadians(latW - latV);
        double dlambda = Math.toRadians(lonW - lonV);

        double a = Math.sin(dphi / 2.0) * Math.sin(dphi / 2.0);
        a += Math.cos(phi1) * Math.cos(phi2) * Math.sin(dlambda / 2.0) * Math.sin(dlambda / 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 3963 * c;
    }

    private double distance2(long v, double lat, double lon) {
        double phi1 = Math.toRadians(lat(v));
        double phi2 = Math.toRadians(lat);
        double dphi = Math.toRadians(lat - lat(v));
        double dlambda = Math.toRadians(lon - lon(v));

        double a = Math.sin(dphi / 2.0) * Math.sin(dphi / 2.0);
        a += Math.cos(phi1) * Math.cos(phi2) * Math.sin(dlambda / 2.0) * Math.sin(dlambda / 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 3963 * c;
    }

    /**
     * Returns the initial bearing (angle) between vertices v and w in degrees.
     * The initial bearing is the angle that, if followed in a straight line
     * along a great-circle arc from the starting point, would take you to the
     * end point.
     * Assumes the lon/lat methods are implemented properly.
     * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
     *
     * @param v The id of the first vertex.
     * @param w The id of the second vertex.
     * @return The initial bearing between the vertices.
     */
    double bearing(long v, long w) {
        return bearing(lon(v), lat(v), lon(w), lat(w));
    }

    static double bearing(double lonV, double latV, double lonW, double latW) {
        double phi1 = Math.toRadians(latV);
        double phi2 = Math.toRadians(latW);
        double lambda1 = Math.toRadians(lonV);
        double lambda2 = Math.toRadians(lonW);

        double y = Math.sin(lambda2 - lambda1) * Math.cos(phi2);
        double x = Math.cos(phi1) * Math.sin(phi2);
        x -= Math.sin(phi1) * Math.cos(phi2) * Math.cos(lambda2 - lambda1);
        return Math.toDegrees(Math.atan2(y, x));
    }

    /**
     * Returns the vertex closest to the given longitude and latitude.
     *
     * @param lon The target longitude.
     * @param lat The target latitude.
     * @return The id of the node in the graph closest to the target.
     */
    long closest(double lon, double lat) {
        // This is the dumb brute force method for now; not suave and cool
        // but works hey it works (☞ﾟヮﾟ)☞
        // Will use a spatial hash after if performance is needed! Cool idea
        double closestDist = 999999999;
        long closestID = 1;
        for (long node : nodes.keySet()) {
            double dist = distance2(node, lat, lon);
            if (dist < closestDist) {
                closestID = node;
                closestDist = dist;
            }
        }
        return closestID;
    }

    /**
     * Gets the longitude of a vertex.
     *
     * @param v The id of the vertex.
     * @return The longitude of the vertex.
     */
    double lon(long v) {
        return nodes.get(v)[1];
    }

    /**
     * Gets the latitude of a vertex.
     *
     * @param v The id of the vertex.
     * @return The latitude of the vertex.
     */
    double lat(long v) {
        return nodes.get(v)[0];
    }

    public void addNode(long id, double lat, double lon) {
        // 0 is Lat, 1 is Lon
        double[] coordinates = {lat, lon};
        nodes.put(id, coordinates);

        // This ALWAYS must be called first otherwise overwrites!
        ArrayList<Long> adjNodeInit = new ArrayList<>();
        adj.put(id, adjNodeInit);

        numVerts++;
    }

    void addEdge(long v, long w) {
        // Add to both adj lists
        adj.get(v).add(w);
        adj.get(w).add(v);
    }

    private void deleteNode(long id) {
        // Just remove key from HashMap
        // MAJOR NOTE: This deleteNode method assumes the
        // node we're deleting has NO ADJACENT NODES
        nodes.remove(id);
        numVerts--;
        // adj.remove(id); // deleted for efficiency see note
    }

    void addLocation(String s, double lat, double lon) {
        String cleaned = cleanString(s);
        double[] coords = {lat, lon};

        if(locations.containsKey(cleaned)) {
            locations.get(cleaned).add(coords);
        } else {
            LinkedList<double[]> placeLocs = new LinkedList<>();
            placeLocs.add(coords);
            locations.put(cleaned, placeLocs);
        }
        this.locationNameTrie.insert(s);
    }
}
