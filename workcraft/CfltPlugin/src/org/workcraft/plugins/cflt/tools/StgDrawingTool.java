package org.workcraft.plugins.cflt.tools;

import org.workcraft.dom.visual.Positioning;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.cflt.Graph;
import org.workcraft.plugins.cflt.presets.ExpressionParameters.Mode;
import org.workcraft.plugins.cflt.utils.EdgeCliqueCoverUtils;
import org.workcraft.plugins.cflt.utils.ExpressionUtils;
import org.workcraft.plugins.stg.Signal.Type;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.stg.VisualStgPlace;
import org.workcraft.utils.WorkspaceUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static org.workcraft.plugins.cflt.utils.ExpressionUtils.*;

public class StgDrawingTool {

    private final HashMap<String, VisualSignalTransition> transitionNameToVisualSignalTransition = new HashMap<>();

    public void drawStg(Graph inputGraph, Graph outputGraph, boolean isSequence, boolean isRoot, Mode mode) {
        VisualStg visualStg = WorkspaceUtils.getAs(ExpressionUtils.we, VisualStg.class);
        ArrayList<ArrayList<String>> edgeCliqueCover = EdgeCliqueCoverUtils.getEdgeCliqueCover(isSequence, mode, inputGraph, outputGraph);
        HashSet<String> inputVertices = new HashSet<>(isSequence ? inputGraph.getVertices() : new ArrayList<>());
        this.drawIsolatedVisualObjects(inputGraph, visualStg, isSequence, isRoot);
        this.drawRemainingVisualObjects(edgeCliqueCover, visualStg, inputVertices, isRoot);
        makePlacesImplicit(visualStg);
    }

    private void drawIsolatedVisualObjects(Graph inputGraph, VisualStg visualStg, boolean isSequence, boolean isRoot) {
        if (inputGraph.getIsolatedVertices() != null) {
            for (String vertex : inputGraph.getIsolatedVertices()) {
                VisualStgPlace visualStgPlace = visualStg.createVisualPlace(null);
                VisualSignalTransition visualSignalTransition = !transitionNameToVisualSignalTransition.containsKey(vertex) && !isSequence ?
                        visualStg.createVisualSignalTransition(
                                ExpressionUtils.labelToName.get(vertex), Type.INTERNAL, getDirection(vertex)):
                        isRoot ? transitionNameToVisualSignalTransition.get(vertex) : null;

                if (!transitionNameToVisualSignalTransition.containsKey(vertex) && !isSequence) {
                    visualStgPlace.setNamePositioning(Positioning.LEFT);
                    visualSignalTransition.setLabelPositioning(Positioning.BOTTOM);
                    visualSignalTransition.setNamePositioning(Positioning.LEFT);
                }

                transitionNameToVisualSignalTransition.put(vertex, visualSignalTransition);
                visualStgPlace.getReferencedComponent().setTokens(1);
                connectVisualPlaceAndVisualSignalTransition(visualStg, visualStgPlace, visualSignalTransition, ConnectionDirection.PLACE_TO_TRANSITION);

            }
        }
    }

    private void drawRemainingVisualObjects(
            ArrayList<ArrayList<String>> edgeCliqueCover,
            VisualStg visualStg,
            HashSet<String> inputVertices,
            boolean isRoot) {

        for (ArrayList<String> clique : edgeCliqueCover) {
            if (!clique.isEmpty() && clique != null) {
                VisualStgPlace visualStgPlace = visualStg.createVisualPlace(null);
                visualStgPlace.setNamePositioning(Positioning.LEFT);
                boolean connectionsOnlyFromPlaceToTransitions = true;

                for (String vertexName : clique) {
                    boolean isClone = false;
                    String cleanVertexName;

                    if (vertexName.contains(Graph.SPECIAL_CLONE_CHARACTER)) {
                        int charIndex = vertexName.indexOf(Graph.SPECIAL_CLONE_CHARACTER);
                        cleanVertexName = vertexName.substring(0, charIndex);
                        isClone = true;
                    } else {
                        cleanVertexName = vertexName;
                    }

                    VisualSignalTransition visualSignalTransition;
                    if (!transitionNameToVisualSignalTransition.containsKey(cleanVertexName)) {
                        visualSignalTransition = visualStg.createVisualSignalTransition(ExpressionUtils.labelToName.get(cleanVertexName),
                                Type.INTERNAL, getDirection(cleanVertexName));

                        transitionNameToVisualSignalTransition.put(cleanVertexName, visualSignalTransition);
                        visualSignalTransition.setLabelPositioning(Positioning.BOTTOM);
                        visualSignalTransition.setNamePositioning(Positioning.LEFT);
                    } else {
                        visualSignalTransition = transitionNameToVisualSignalTransition.get(cleanVertexName);
                    }

                    if (inputVertices.contains(cleanVertexName) || isClone) {
                        connectVisualPlaceAndVisualSignalTransition(visualStg, visualStgPlace,
                                visualSignalTransition, ConnectionDirection.TRANSITION_TO_PLACE);
                        connectionsOnlyFromPlaceToTransitions = false;
                        visualStgPlace.getReferencedComponent().setTokens(isRoot ? 1 : 0);
                    } else {
                        connectVisualPlaceAndVisualSignalTransition(visualStg, visualStgPlace,
                                visualSignalTransition, ConnectionDirection.PLACE_TO_TRANSITION);
                    }
                }
                visualStgPlace.getReferencedComponent().setTokens(connectionsOnlyFromPlaceToTransitions ? 1 : 0);
            }
        }
    }

    private void makePlacesImplicit(VisualStg visualStg) {
        for (VisualStgPlace visualStgPlace : visualStg.getVisualPlaces()) {
            visualStg.makeImplicitIfPossible(visualStgPlace, true);
        }
    }

    public void drawSingleTransition(String transitionName) {
        VisualStg visualStg = WorkspaceUtils.getAs(ExpressionUtils.we, VisualStg.class);
        VisualStgPlace visualStgPlace = visualStg.createVisualPlace(null);
        visualStgPlace.setNamePositioning(Positioning.LEFT);
        visualStgPlace.getReferencedComponent().setTokens(1);

        char lastChar = transitionName.charAt(transitionName.length() - 1);
        transitionName = transitionName.substring(0, transitionName.length() - 1);
        switch (lastChar) {
        case PLUS_DIR:
            ExpressionUtils.nameToDirection.put(transitionName, PLUS_DIR);
        case MINUS_DIR:
            ExpressionUtils.nameToDirection.put(transitionName, ExpressionUtils.MINUS_DIR);
        default:
            ExpressionUtils.nameToDirection.put(transitionName, ExpressionUtils.TOGGLE_DIR);
        }

        VisualSignalTransition visualSignalTransition =
                visualStg.createVisualSignalTransition(transitionName, Type.INTERNAL, getDirection(transitionName));

        connectVisualPlaceAndVisualSignalTransition(visualStg, visualStgPlace, visualSignalTransition, ConnectionDirection.PLACE_TO_TRANSITION);
    }

    private Direction getDirection(String name) {
        char dir = ExpressionUtils.nameToDirection.get(name);
        switch (dir) {
        case PLUS_DIR:
            return Direction.PLUS;
        case MINUS_DIR:
            return Direction.MINUS;
        case TOGGLE_DIR:
            return Direction.TOGGLE;
        default:
            return null;
        }
    }
    private void connectVisualPlaceAndVisualSignalTransition(
            VisualStg visualStg,
            VisualStgPlace visualStgPlace,
            VisualSignalTransition visualSignalTransition,
            ConnectionDirection connectionDirection) {
        try {
            switch (connectionDirection) {
            case PLACE_TO_TRANSITION:
                visualStg.connect(visualStgPlace, visualSignalTransition);
            case TRANSITION_TO_PLACE:
                visualStg.connect(visualSignalTransition, visualStgPlace);
            }
        } catch (InvalidConnectionException ignored) {
        }
    }

}
