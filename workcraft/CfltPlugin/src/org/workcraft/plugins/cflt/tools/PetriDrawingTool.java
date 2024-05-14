package org.workcraft.plugins.cflt.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.workcraft.dom.visual.Positioning;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.cflt.Graph;
import org.workcraft.plugins.cflt.presets.ExpressionParameters.Mode;
import org.workcraft.plugins.cflt.utils.EdgeCliqueCoverUtils;
import org.workcraft.plugins.cflt.utils.ExpressionUtils;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.utils.WorkspaceUtils;

import static org.workcraft.plugins.cflt.utils.GraphUtils.SPECIAL_CLONE_CHARACTER;

public class PetriDrawingTool {
    private final HashMap<String, VisualTransition> transitionNameToVisualTransition = new HashMap<>();

    public void drawPetri(Graph inputGraph, Graph outputGraph, boolean isSequence, boolean isRoot, Mode mode) {
        VisualPetri visualPetri = WorkspaceUtils.getAs(ExpressionUtils.we, VisualPetri.class);
        ArrayList<ArrayList<String>> edgeCliqueCover = EdgeCliqueCoverUtils.getEdgeCliqueCover(isSequence, mode, inputGraph, outputGraph);
        HashSet<String> inputVertexNames = new HashSet<>(isSequence ? inputGraph.getVertices() : new ArrayList<>());

        this.drawIsolatedVisualObjects(inputGraph, visualPetri, isSequence, isRoot);
        this.drawRemainingVisualObjects(edgeCliqueCover, visualPetri, inputVertexNames, isRoot);
    }
    public void drawSingleTransition(String name) {
        VisualPetri visualPetri = WorkspaceUtils.getAs(ExpressionUtils.we, VisualPetri.class);
        VisualPlace visualPlace = createVisualPlace(visualPetri, true, Positioning.LEFT);
        VisualTransition visualTransition = createVisualTransition(visualPetri, name);
        connectVisualPlaceAndVisualTransition(visualPetri, visualPlace, visualTransition, ConnectionDirection.PLACE_TO_TRANSITION);
    }
    private void drawRemainingVisualObjects(
            ArrayList<ArrayList<String>> edgeCliqueCover,
            VisualPetri visualPetri,
            HashSet<String> inputVertexNames,
            boolean isRoot) {
        for (ArrayList<String> clique : edgeCliqueCover) {
            if (clique != null) {
                VisualPlace visualPlace = createVisualPlace(visualPetri, isRoot, Positioning.LEFT);

                for (String vertexName : clique) {
                    boolean isClone = vertexName.contains(SPECIAL_CLONE_CHARACTER);
                    String cleanVertexName = isClone ? vertexName.substring(0, vertexName.indexOf(SPECIAL_CLONE_CHARACTER)) :
                            vertexName;

                    boolean isTransitionPresent = transitionNameToVisualTransition.containsKey(cleanVertexName);
                    VisualTransition visualTransition = isTransitionPresent ? transitionNameToVisualTransition.get(cleanVertexName) :
                            createVisualTransition(visualPetri, ExpressionUtils.labelToName.get(cleanVertexName));

                    transitionNameToVisualTransition.put(cleanVertexName, visualTransition);
                    connectVisualPlaceAndVisualTransition(visualPetri, visualPlace, visualTransition,
                            inputVertexNames.contains(cleanVertexName) || isClone ?
                            ConnectionDirection.TRANSITION_TO_PLACE :
                            ConnectionDirection.PLACE_TO_TRANSITION);
                }
            }
        }
    }

    private void drawIsolatedVisualObjects(Graph inputGraph, VisualPetri visualPetri, Boolean isSequence, Boolean isRoot) {
        if (inputGraph.getIsolatedVertices() != null) {
            for (String vertex : inputGraph.getIsolatedVertices()) {
                boolean isTransitionNamePresent = transitionNameToVisualTransition.containsKey(vertex);

                VisualPlace visualPlace = !isTransitionNamePresent && !isSequence ?
                        createVisualPlace(visualPetri, true, Positioning.LEFT) :
                        isRoot ? createVisualPlace(visualPetri, true, Positioning.TOP) :
                                null;

                VisualTransition visualTransition = !isTransitionNamePresent && !isSequence ?
                        createVisualTransition(visualPetri, ExpressionUtils.labelToName.get(vertex)) :
                        isRoot ?  transitionNameToVisualTransition.get(vertex) :
                                null;

                if (visualPlace != null && visualTransition != null) {
                    transitionNameToVisualTransition.put(vertex, visualTransition);
                    connectVisualPlaceAndVisualTransition(visualPetri, visualPlace, visualTransition, ConnectionDirection.PLACE_TO_TRANSITION);
                }
            }
        }
    }

    private VisualPlace createVisualPlace(VisualPetri visualPetri, boolean hasToken, Positioning positioning) {
        VisualPlace visualPlace = visualPetri.createPlace(null, null);
        visualPlace.getReferencedComponent().setTokens(hasToken ? 1 : 0);
        visualPlace.setNamePositioning(positioning);
        return visualPlace;
    }
    private VisualTransition createVisualTransition(VisualPetri visualPetri, String name) {
        VisualTransition visualTransition = visualPetri.createTransition(null, null);
        visualTransition.setLabel(name);
        visualTransition.setLabelPositioning(Positioning.BOTTOM);
        visualTransition.setNamePositioning(Positioning.LEFT);
        return visualTransition;
    }
    private void connectVisualPlaceAndVisualTransition(
            VisualPetri visualPetri,
            VisualPlace visualPlace,
            VisualTransition visualTransition,
            ConnectionDirection connectionDirection) {
        try {
            switch (connectionDirection) {
            case PLACE_TO_TRANSITION:
                visualPetri.connect(visualPlace, visualTransition);
                break;
            case TRANSITION_TO_PLACE:
                visualPetri.connect(visualTransition, visualPlace);
                break;
            }
        } catch (InvalidConnectionException invalidConnectionException) {
        }
    }
}
