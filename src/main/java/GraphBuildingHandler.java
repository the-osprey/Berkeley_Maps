import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Parses OSM XML files using an XML SAX parser. Used to construct the graph of roads for
 * pathfinding, under some constraints.
 * See OSM documentation on
 * <a href="http://wiki.openstreetmap.org/wiki/Key:highway">the highway tag</a>,
 * <a href="http://wiki.openstreetmap.org/wiki/Way">the way XML element</a>,
 * <a href="http://wiki.openstreetmap.org/wiki/Node">the node XML element</a>,
 * and the java
 * <a href="https://docs.oracle.com/javase/tutorial/jaxp/sax/parsing.html">SAX parser tutorial</a>.
 * <p>
 * You may find the CSCourseGraphDB and CSCourseGraphDBHandler examples useful.
 * <p>
 * The idea here is that some external library is going to walk through the XML
 * file, and your override method tells Java what to do every time it gets to the next
 * element in the file. This is a very common but strange-when-you-first-see it pattern.
 * It is similar to the Visitor pattern we discussed for graphs.
 *
 * @author Alan Yao, Maurice Lee
 */
public class GraphBuildingHandler extends DefaultHandler {
    /**
     * Only allow for non-service roads; this prevents going on pedestrian streets as much as
     * possible. Note that in Berkeley, many of the campus roads are tagged as motor vehicle
     * roads, but in practice we walk all over them with such impunity that we forget cars can
     * actually drive on them.
     */
    private static final Set<String> ALLOWED_HIGHWAY_TYPES = new HashSet<>(Arrays.asList
            ("motorway", "trunk", "primary", "secondary", "tertiary", "unclassified",
                    "residential", "living_street", "motorway_link", "trunk_link", "primary_link",
                    "secondary_link", "tertiary_link"));
    private final GraphDB g;
    private String activeState = "";
    // @Source: Flag inspired by project videos
    // Use it to indicate if we hit all the conditions such that we add edges to route graph
//    private boolean flag = false;
//    ArrayList<Long> potentialEdges = new ArrayList<>();
    private ArrayList<Long> potentialEdges = new ArrayList<>();
    private boolean isHighway = false;


    /**
     * Create a new GraphBuildingHandler.
     *
     * @param g The graph to populate with the XML data.
     */
    public GraphBuildingHandler(GraphDB g) {
        this.g = g;
    }

    /**
     * Called at the beginning of an element. Typically, you will want to handle each element in
     * here, and you may want to track the parent element.
     *
     * @param uri        The Namespace URI, or empty string if the element has no Namespace URI or
     *                   if Namespace processing is not being performed.
     * @param localName  The local name (without prefix), or the empty string if Namespace
     *                   processing is not being performed.
     * @param qName      The qualified name (w prefix), or the empty string if qualified names are
     *                   not available. This tells us which element we're looking at.
     * @param attributes The attributes attached to the element. If there are no attributes, it
     *                   shall be an empty Attributes object.
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     * @see Attributes
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        /* Some example code on how you might begin to parse XML files. */
        if (qName.equals("node")) {
            /* We encountered a new <node...> tag. */
            activeState = "node";
//            System.out.println("Node id: " + attributes.getValue("id"));
//            System.out.println("Node lon: " + attributes.getValue("lon"));
//            System.out.println("Node lat: " + attributes.getValue("lat"));
            /* TODO ~DONE~ Use the above information to save a "node" to somewhere. */
            /* Hint: A graph-like structure would be nice. */
            double[] coordinates = {Double.parseDouble(attributes.getValue("lat")),
                    Double.parseDouble(attributes.getValue("lon"))};
            // ADD NODE OVERWRITES ADJ NOTES REMEMBER IF YOU ADD SOMEWHERE ELSE!
            g.addNode(Long.parseLong(attributes.getValue("id")),
                    Double.parseDouble(attributes.getValue("lat")),
                    Double.parseDouble(attributes.getValue("lon")));
//            g.nodes.put(Long.parseLong(attributes.getValue("id")), coordinates);


        } else if (qName.equals("way")) {
            /* We encountered a new <way...> tag. */
            activeState = "way";

            // Need to reset our flag and data structure of potential edges
            potentialEdges = new ArrayList<>();
            isHighway = false;

//            System.out.println("Beginning a way...");
        } else if (activeState.equals("way") && qName.equals("nd")) {
            /* While looking at a way, we found a <nd...> tag. */
//            System.out.println("Id of a node in this way: " + attributes.getValue("ref"));

            /* TODO Use the above id to make "possible" connections between the nodes in this way */
            /* Hint1: It would be useful to remember what was the last node in this way. */
            /* Hint2: Not all ways are valid. So, directly connecting the nodes here would be
            cumbersome since you might have to remove the connections if you later see a tag that
            makes this way invalid. Instead, think of keeping a list of possible connections and
            remember whether this way is valid or not. */
//            ArrayList<Long> potentialEdges = new ArrayList<>();
            this.potentialEdges.add(Long.parseLong(attributes.getValue("ref")));

//            System.out.println("added " + Long.parseLong(attributes.getValue("ref"))); // my stmt

        } else if (activeState.equals("way") && qName.equals("tag")) {
            /* While looking at a way, we found a <tag...> tag. */
            String k = attributes.getValue("k");
            String v = attributes.getValue("v");
//            if (k.equals("maxspeed")) {
//                //System.out.println("Max Speed: " + v);
//                /* TODO set the max speed of the "current way" here. */
//                // TODO Come back to this for pathfinding
            if (k.equals("highway")) { //was else if
                //System.out.println("Highway type: " + v);
                /* TODO Figure out whether this way and its connections are valid. */
                for (String allowedType : ALLOWED_HIGHWAY_TYPES) {
                    if (v.equals(allowedType)) {
                        /* TODO add to graph */
                        isHighway = true;
//                        g.adj.put(Long.parseLong(attributes.getValue("id")), potentialEdges);
//                        System.out.println("Did something!");
                    }
                }
                /* Hint: Setting a "flag" is good enough! */
//            } else if (k.equals("name")) {
////                System.out.println("Way Name: " + v);
//            }
//            System.out.println("Tag with k=" + k + ", v=" + v + ".");
            }
            else if (activeState.equals("node") && qName.equals("tag")
             && attributes.getValue("k")
                   .equals("name")) {
                    /* While looking at a node, we found a <tag...> with k="name". */
                    /* TODO Create a location. */
            /* Hint: Since we found this <tag...>
            INSIDattributes.getValue("lon")E a node, we should probably remember which
            node this tag belongs to. Remember XML is parsed top-to-bottom, so probably it's the
            last node that you looked at (check the first if-case). */
//                    System.out.println("Node's name: " + attributes.getValue("v"));
                g.addLocation((attributes.getValue("v")), Double.parseDouble(attributes.getValue("lat")),
                        Double.parseDouble(attributes.getValue("lon")));
                }
        }
    }

    /**
     * Receive notification of the end of an element. You may want to take specific terminating
     * actions here, like finalizing vertices or edges found.
     *
     * @param uri       Namespace URI, or the empty string if the element has no Namespace URI or
     *                  if Namespace processing is not being performed.
     * @param localName The local name (without prefix), or the empty string if Namespace
     *                  processing is not being performed.
     * @param qName     The qualified name (with prefix), or the empty string if qualified names are
     *                  not available.
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("way")) {
            // TODO
            /* We are done looking at a way. (We finished looking at the nodes, speeds, etc...)*/
            /* Hint1: If you have stored the possible connections for this way, here's your
            chance to actually connect the nodes together if the way is valid. */
//            System.out.println("Finishing a way...");
//            System.out.println(isHighway);
            if (isHighway) {
//                g.adj.put(currentID, potentialEdges);
                int i = 0;
                while (i < potentialEdges.size() - 1) {
//                    System.out.println("adding " + potentialEdges.get(i) + " and " +
//                    potentialEdges.get(i + 1) +" together");
                    g.addEdge(potentialEdges.get(i), potentialEdges.get(i + 1));
                    i++;
                }
//                potentialEdges = new ArrayList<>();
//                isHighway = false;
            }
        }
    }

}
