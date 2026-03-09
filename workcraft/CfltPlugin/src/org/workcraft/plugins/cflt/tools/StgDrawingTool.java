package org.workcraft.plugins.cflt.tools;

import org.workcraft.dom.visual.Positioning;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.cflt.graph.Clique;
import org.workcraft.plugins.cflt.graph.Graph;
import org.workcraft.plugins.cflt.graph.Vertex;
import org.workcraft.plugins.cflt.node.NodeCollection;
import org.workcraft.plugins.cflt.node.NodeDetails;
import org.workcraft.plugins.cflt.presets.ExpressionParameters.Mode;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.VisualStgPlace;
import org.workcraft.plugins.stg.Signal.Type;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.*;

import static org.workcraft.plugins.cflt.utils.EdgeCliqueCoverUtils.getEdgeCliqueCover;

public class StgDrawingTool implements VisualModelDrawingTool {
    private final Map<String, VisualSignalTransition> transitionNameToVisualTransition = new HashMap<>();
    private final NodeCollection nodeCollection;

    public StgDrawingTool(final NodeCollection nodeCollection) {
        this.nodeCollection = nodeCollection;
    }

    @Override
    public void drawVisualObjects(Graph inputGraph, Graph outputGraph,
            boolean isSequence, boolean isRoot, Mode mode, WorkspaceEntry we) {

        List<Clique> edgeCliqueCover = getEdgeCliqueCover(inputGraph, outputGraph, isSequence, mode);
        List<Vertex> vertexNames = isSequence
                ? inputGraph.getVertices()
                : new ArrayList<>();
        HashSet<Vertex> inputVertices = new HashSet<>(vertexNames);

        VisualStg visualStg = WorkspaceUtils.getAs(we, VisualStg.class);
        if (inputGraph.getIsolatedVertices() != null) {
            this.drawIsolatedVisualObjects(inputGraph, visualStg, isSequence, isRoot);
        }
        this.drawRemainingVisualObjects(edgeCliqueCover, visualStg, inputVertices, isRoot);
        makePlacesImplicit(visualStg);
    }

    @Override
    public void drawSingleTransition(String name, WorkspaceEntry we) {
        VisualStg visualStg = WorkspaceUtils.getAs(we, VisualStg.class);
        VisualStgPlace visualStgPlace = createVisualStgPlace(visualStg, true, Positioning.LEFT);
        VisualSignalTransition visualSignalTransition = createVisualSignalTransition(visualStg, name);
        connectVisualPlaceAndVisualSignalTransition(visualStg, visualStgPlace, visualSignalTransition, ConnectionDirection.PLACE_TO_TRANSITION);
    }

    private void drawRemainingVisualObjects(List<Clique> edgeCliqueCover, VisualStg visualStg,
            Set<Vertex> inputVertices, boolean isRoot) {
        for (Clique clique : edgeCliqueCover) {
            VisualStgPlace visualStgPlace = createVisualStgPlace(visualStg, isRoot, Positioning.LEFT);

            for (Vertex vertex : clique.getVertices()) {
                String name = vertex.name();
                boolean isClone = vertex.isClone();
                boolean isTransitionPresent = transitionNameToVisualTransition.containsKey(name);

                VisualSignalTransition visualSignalTransition = isTransitionPresent
                        ? transitionNameToVisualTransition.get(name)
                        : createVisualSignalTransition(visualStg, name);

                transitionNameToVisualTransition.put(name, visualSignalTransition);

                ConnectionDirection connectionDirection = inputVertices.contains(vertex) || isClone
                        ? ConnectionDirection.TRANSITION_TO_PLACE
                        : ConnectionDirection.PLACE_TO_TRANSITION;

                connectVisualPlaceAndVisualSignalTransition(visualStg, visualStgPlace, visualSignalTransition, connectionDirection);
            }
        }
    }

    private void drawIsolatedVisualObjects(Graph inputGraph, VisualStg visualStg, boolean isSequence, boolean isRoot) {
        for (Vertex vertex : inputGraph.getIsolatedVertices()) {
            String name = vertex.name();
            boolean isTransitionNamePresent = transitionNameToVisualTransition.containsKey(name);

            VisualStgPlace visualStgPlace = null;
            if (!isTransitionNamePresent && !isSequence) {
                visualStgPlace = createVisualStgPlace(visualStg, true, Positioning.LEFT);
            } else if (isRoot) {
                visualStgPlace = createVisualStgPlace(visualStg, true, Positioning.TOP);
            }

            VisualSignalTransition visualSignalTransition = null;
            if (!isTransitionNamePresent && !isSequence) {
                visualSignalTransition = createVisualSignalTransition(visualStg, name);
            } else if (isRoot) {
                visualSignalTransition = transitionNameToVisualTransition.get(name);
            }

            if (visualStgPlace != null && visualSignalTransition != null) {
                transitionNameToVisualTransition.put(name, visualSignalTransition);
                connectVisualPlaceAndVisualSignalTransition(visualStg, visualStgPlace, visualSignalTransition, ConnectionDirection.PLACE_TO_TRANSITION);
            }
        }
    }

    private VisualStgPlace createVisualStgPlace(VisualStg visualStg, boolean hasToken, Positioning positioning) {
        VisualStgPlace visualStgPlace = visualStg.createVisualPlace(null, null);
        visualStgPlace.getReferencedComponent().setTokens(hasToken ? 1 : 0);
        visualStgPlace.setNamePositioning(positioning);
        return visualStgPlace;
    }

    private VisualSignalTransition createVisualSignalTransition(VisualStg visualStg, String name) {
        NodeDetails nodeDetails = nodeCollection.getNodeDetails(name);
        String label = nodeDetails.getLabel();
        SignalTransition.Direction dir = nodeDetails.getDirection();

        VisualSignalTransition visualSignalTransition = visualStg.createVisualSignalTransition(label, Type.INTERNAL, dir);
        visualSignalTransition.setLabelPositioning(Positioning.BOTTOM);
        visualSignalTransition.setNamePositioning(Positioning.LEFT);
        return visualSignalTransition;
    }

    private void connectVisualPlaceAndVisualSignalTransition(
            VisualStg visualStg,
            VisualStgPlace visualStgPlace,
            VisualSignalTransition visualSignalTransition,
            ConnectionDirection connectionDirection) {
        try {
            switch (connectionDirection) {
                case PLACE_TO_TRANSITION -> visualStg.connect(visualStgPlace, visualSignalTransition);
                case TRANSITION_TO_PLACE -> visualStg.connect(visualSignalTransition, visualStgPlace);
            }
        } catch (InvalidConnectionException e) {
            LogUtils.logError("Invalid connection of VisualStgPlace and VisualSignalTransition");
            e.printStackTrace();
        }
    }

    private void makePlacesImplicit(VisualStg visualStg) {
        visualStg.getVisualPlaces().forEach(p -> visualStg.makeImplicitIfPossible(p, true));
    }
}
