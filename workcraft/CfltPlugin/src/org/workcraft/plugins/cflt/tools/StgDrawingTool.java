package org.workcraft.plugins.cflt.tools;

import org.workcraft.dom.visual.Positioning;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.cflt.Clique;
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

import java.util.*;

import static org.workcraft.plugins.cflt.utils.GraphUtils.SPECIAL_CLONE_CHARACTER;

public class StgDrawingTool {

    private final Map<String, VisualSignalTransition> transitionNameToVisualSignalTransition = new HashMap<>();

    public void drawStg(Graph inputGraph, Graph outputGraph, boolean isSequence, boolean isRoot, Mode mode) {
        VisualStg visualStg = WorkspaceUtils.getAs(ExpressionUtils.we, VisualStg.class);
        List<Clique> edgeCliqueCover = EdgeCliqueCoverUtils.getEdgeCliqueCover(isSequence, mode, inputGraph, outputGraph);
        HashSet<String> inputVertices = new HashSet<>(isSequence ? inputGraph.getVertexNames() : new ArrayList<>());

        this.drawIsolatedVisualObjects(inputGraph, visualStg, isSequence, isRoot);
        this.drawRemainingVisualObjects(edgeCliqueCover, visualStg, inputVertices, isRoot);
        makePlacesImplicit(visualStg);
    }

    private void drawRemainingVisualObjects(
            List<Clique> edgeCliqueCover,
            VisualStg visualStg,
            Set<String> inputVertexNames,
            boolean isRoot) {

        for (Clique clique : edgeCliqueCover) {
            if (clique != null) {
                VisualStgPlace visualStgPlace = createVisualStgPlace(visualStg, isRoot, Positioning.LEFT);

                for (String vertexName : clique.getVertexNames()) {
                    boolean isClone = vertexName.contains(SPECIAL_CLONE_CHARACTER);
                    String cleanVertexName = isClone ? vertexName.substring(0, vertexName.indexOf(SPECIAL_CLONE_CHARACTER)) :
                            vertexName;

                    boolean isTransitionPresent = transitionNameToVisualSignalTransition.containsKey(cleanVertexName);
                    VisualSignalTransition visualSignalTransition = isTransitionPresent ? transitionNameToVisualSignalTransition.get(cleanVertexName) :
                            createVisualSignalTransition(visualStg, cleanVertexName);

                    transitionNameToVisualSignalTransition.put(cleanVertexName, visualSignalTransition);
                    connectVisualPlaceAndVisualSignalTransition(visualStg, visualStgPlace, visualSignalTransition,
                            inputVertexNames.contains(cleanVertexName) || isClone ?
                                    ConnectionDirection.TRANSITION_TO_PLACE :
                                    ConnectionDirection.PLACE_TO_TRANSITION);
                }
            }
        }
    }

    private void makePlacesImplicit(VisualStg visualStg) {
        for (VisualStgPlace visualStgPlace : visualStg.getVisualPlaces()) {
            visualStg.makeImplicitIfPossible(visualStgPlace, true);
        }
    }

    public void drawSingleTransition(String label) {
        VisualStg visualStg = WorkspaceUtils.getAs(ExpressionUtils.we, VisualStg.class);
        VisualStgPlace visualStgPlace = createVisualStgPlace(visualStg, true, Positioning.LEFT);

        switch (label.charAt(label.length() - 1)) {
        case ExpressionUtils.PLUS_DIR:
            ExpressionUtils.nameToDirection.put(label, Direction.PLUS);
            label = label.substring(0, label.length() - 1);
            break;
        case ExpressionUtils.MINUS_DIR:
            ExpressionUtils.nameToDirection.put(label, Direction.MINUS);
            label = label.substring(0, label.length() - 1);
            break;
        default:
            ExpressionUtils.nameToDirection.put(label, Direction.TOGGLE);
            break;
        }
        VisualSignalTransition visualSignalTransition = createVisualSignalTransition(visualStg, label);
        connectVisualPlaceAndVisualSignalTransition(visualStg, visualStgPlace, visualSignalTransition, ConnectionDirection.PLACE_TO_TRANSITION);
    }

    private void drawIsolatedVisualObjects(Graph inputGraph, VisualStg visualStg, boolean isSequence, boolean isRoot) {
        if (inputGraph.getIsolatedVertices() != null) {
            for (String vertexName : inputGraph.getIsolatedVertices()) {
                boolean isTransitionNamePresent = transitionNameToVisualSignalTransition.containsKey(vertexName);

                VisualStgPlace visualStgPlace = !isTransitionNamePresent && !isSequence ?
                        createVisualStgPlace(visualStg, true, Positioning.LEFT) :
                        isRoot ? createVisualStgPlace(visualStg, true, Positioning.TOP) :
                                null;

                VisualSignalTransition visualSignalTransition = !isTransitionNamePresent && !isSequence ?
                        createVisualSignalTransition(visualStg, vertexName) :
                        isRoot ?  transitionNameToVisualSignalTransition.get(vertexName) :
                                null;

                if (visualStgPlace != null && visualSignalTransition != null) {
                    transitionNameToVisualSignalTransition.put(vertexName, visualSignalTransition);
                    connectVisualPlaceAndVisualSignalTransition(visualStg, visualStgPlace, visualSignalTransition, ConnectionDirection.PLACE_TO_TRANSITION);
                }
            }
        }
    }

    private VisualStgPlace createVisualStgPlace(VisualStg visualStg, boolean hasToken, Positioning positioning) {
        VisualStgPlace visualStgPlace = visualStg.createVisualPlace(null, null);
        visualStgPlace.getReferencedComponent().setTokens(hasToken ? 1 : 0);
        visualStgPlace.setNamePositioning(positioning);
        return visualStgPlace;
    }
    private VisualSignalTransition createVisualSignalTransition(VisualStg visualStg, String label) {
        VisualSignalTransition visualSignalTransition = visualStg.createVisualSignalTransition(
                ExpressionUtils.labelToName.get(label), Type.INTERNAL, ExpressionUtils.nameToDirection.get(label));
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
            case PLACE_TO_TRANSITION:
                visualStg.connect(visualStgPlace, visualSignalTransition);
                break;
            case TRANSITION_TO_PLACE:
                visualStg.connect(visualSignalTransition, visualStgPlace);
                break;
            }
        } catch (InvalidConnectionException invalidConnectionException) {
            invalidConnectionException.printStackTrace();
        }
    }
}
