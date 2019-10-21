package org.workcraft.plugins.cpog.untangling;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.plugins.cpog.Cpog;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.plugins.cpog.VisualVertex;
import org.workcraft.plugins.cpog.VisualVertex.RenderType;
import org.workcraft.plugins.cpog.commands.PetriToCpogParameters;
import org.workcraft.plugins.cpog.untangling.UntanglingNode.NodeType;
import org.workcraft.plugins.petri.Petri;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualPetri;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class PetriToCpogConverter {

    private final Petri pn;
    private final VisualCpog visualCpog;
    private int xRightmostVertex;

    /** constructor **/
    public PetriToCpogConverter(VisualPetri vpn) {
        this.pn = vpn.getMathModel();
        this.visualCpog = new VisualCpog(new Cpog());
        this.xRightmostVertex = -1;
    }

    /** function which performs the conversion
     * @param settings **/
    public VisualCpog run(PetriToCpogParameters settings) {

        Untanglings untangling = new Untanglings(settings);

        /*****************************************************
         * Unpack Petri net and stream it into the converter *
         *****************************************************/

        // insert places
        for (Place p : pn.getPlaces()) {
            untangling.addPlace(pn.getNodeReference(p));
        }

        // insert transitions
        for (Transition t : pn.getTransitions()) {
            untangling.addTransition(pn.getNodeReference(t));
        }

        // insert connections
        for (Place p : pn.getPlaces()) {

            for (Node n : pn.getPostset(p)) {
                if (n instanceof Transition) {
                    Transition t = (Transition) n;

                    untangling.placeToTransition(pn.getNodeReference(p), pn.getNodeReference(t));
                    // debug printing
                    // System.out.println(pn.getNodeReference(p) + " -> " + pn.getNodeReference(t));
                }
            }
        }

        for (Transition t : pn.getTransitions()) {
            for (Node n : pn.getPostset(t)) {
                if (n instanceof Place) {
                    Place p = (Place) n;

                    untangling.transitionToPlace(pn.getNodeReference(t), pn.getNodeReference(p));
                    // debug printing
                    // System.out.println(pn.getNodeReference(t) + " -> " + pn.getNodeReference(p));
                }
            }
        }

        // insert tokens
        for (Place p : pn.getPlaces()) {
            if (p.getTokens() > 0) {
                if (!untangling.insertTokens(pn.getNodeReference(p), p.getTokens())) {
                    System.out.println("Place with this name not found. Tokens not inserted");
                    return null;
                }
            }
        }

        /*****************************************************
         * Convert the Petri net into a Cpog                 *
         *****************************************************/

        // start conversion from Petri net to Cpog
        if (!untangling.startConversion()) {
            return null;
        }

        // getting the partial orders from the untangling
        ArrayList<PartialOrder> partialOrders = untangling.getPartialOrders(settings);

        // building the cpog from the partial orders
        buildCpog(partialOrders);

        return visualCpog;

    }

    /** building the cpog model from the string partial orders **/
    @SuppressWarnings("deprecation")
    private void buildCpog(ArrayList<PartialOrder> partialOrders) {

        // Positions inside the workspace
        int xPos = 0;
        int yPos = 0;
        int i = 0;

        // looping over partial orders
        for (PartialOrder po : partialOrders) {
            i++;

            // move the vertically every partial order
            xPos = 0;
            yPos = i * 10;

            // debug printing: number of the partial order
            // System.out.println("Partial Order " + (i + 1) + ":");

            LinkedHashMap<String, VisualVertex> nodes = new LinkedHashMap<>();
            Container container = visualCpog.getCurrentLevel();
            visualCpog.selectNone();

            // looping over the edges
            for (UntanglingEdge edge : po) {

                // creating source vertex
                UntanglingNode sourceNode = edge.getFirst();
                VisualVertex sourceVertex = visualCpog.createVisualVertex(container);
                String sourceName = sourceNode.getLabel();
                sourceVertex.setLabel(sourceName);
                if (sourceNode.getType() == NodeType.PLACE) {
                    sourceVertex.setRenderType(RenderType.CIRCLE);
                } else {
                    sourceVertex.setRenderType(RenderType.SQUARE);
                }

                // creating target vertex
                UntanglingNode targetNode = edge.getSecond();
                VisualVertex targetVertex = visualCpog.createVisualVertex(container);
                String targetName = targetNode.getLabel();
                targetVertex.setLabel(targetName);
                if (targetNode.getType() == NodeType.PLACE) {
                    targetVertex.setRenderType(RenderType.CIRCLE);
                } else {
                    targetVertex.setRenderType(RenderType.SQUARE);
                }

                // checking if they are already present
                // if so do not create a new one but connect
                // the one already available
                if (nodes.containsKey(sourceName)) {
                    sourceVertex = nodes.get(sourceName);
                } else {
                    sourceVertex.setPosition(new Point2D.Double(xPos, yPos));
                    xPos = xPos + 5;
                    nodes.put(sourceName, sourceVertex);
                }

                if (nodes.containsKey(targetName)) {
                    targetVertex = nodes.get(targetName);
                } else {
                    targetVertex.setPosition(new Point2D.Double(xPos, yPos));
                    xPos = xPos + 5;
                    nodes.put(targetName, targetVertex);
                }

                // connection
                visualCpog.connect(sourceVertex, targetVertex);

                // debug: printing partial order
                // System.out.println(source.getLabel() + " -> " + target.getLabel());

            }

            // removing the duplicates
            for (VisualVertex v : visualCpog.getVertices()) {
                if (visualCpog.getConnections(v).isEmpty()) {
                    visualCpog.removeWithoutNotify(v);
                }
            }

            //rearrange vertices
            VisualVertex first = null;
            for (VisualVertex v : visualCpog.getVertices()) {
                if (visualCpog.getPreset(v).isEmpty()) {
                    first = v;
                    break;
                }
            }

            xPos = 0;
            yPos = i * 10;
            xRightmostVertex = -1;
            visitGraph(first, xPos, yPos);

            // renaming the partial order
            visualCpog.selectAll();
            visualCpog.groupSelection("Partial Order " + (i + 1));

        }

    }

    /** visit the graph rearranging the vertices by causality relationships **/
    private void visitGraph(VisualVertex vertex, int xPos, int yPos) {

        // termination condition:
        // if node has no more connection the path
        // has been visited
        if (visualCpog.getPostset(vertex).isEmpty()) {

            // last vertex must be place after all the other ones
            xRightmostVertex = xRightmostVertex + 5;

            int numberPreConnections = visualCpog.getConnections(vertex).size();

            // if last vertex has got more than one pre vertices
            // it must be placed on the right of the rightmost
            // pre vertex
            if (numberPreConnections > 1) {
                xPos = xRightmostVertex;
            }

            // set position of last vertex
            vertex.setPosition(new Point2D.Double(xPos, yPos));
            return;
        }

        // counting postSet of a vertex
        int numberPostConnections = 0;
        for (Connection connection : visualCpog.getConnections(vertex)) {
            if (connection.getFirst().equals(vertex)) {
                numberPostConnections++;
            }
        }

        // variables for the rearranging the positions
        int x = xPos;
        int y = yPos;
        int nc = 0;
        if (xRightmostVertex < x) {
            xRightmostVertex = x;
        }

        // looping over the post vertices
        for (Connection connection : visualCpog.getConnections(vertex)) {
            if (connection.getFirst().equals(vertex)) {

                // number of post vertices
                nc++;

                // select the post vertex
                VisualVertex postVertex = (VisualVertex) connection.getSecond();

                // set the position to current vertex
                vertex.setPosition(new Point2D.Double(x, y));

                // increment horizontal position
                x = x + 5;

                // if more post vertices exist, modify the vertical
                // position of them (next vertices)
                if (numberPostConnections > 1 && nc == 1) {
                    y = y - numberPostConnections;
                } else {
                    if (numberPostConnections != 1) {
                        for (int j = 0; j < nc; j++) {
                            y = y + numberPostConnections;
                        }
                    }
                }

                // call recursively the function
                visitGraph(postVertex, x, y);

                // backtrack in case of more post vertices
                x = x - 5;

            }
        }

    }

}
