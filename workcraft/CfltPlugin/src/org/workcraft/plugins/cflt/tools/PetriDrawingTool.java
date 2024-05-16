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
        VisualPlace visualPlace = createVisualPlace(visualPetri);
        visualPlace.setNamePositioning(Positioning.LEFT);
        visualPlace.getReferencedComponent().setTokens(1);

        VisualTransition visualTransition = createVisualTransition(visualPetri);
        visualTransition.setLabel(name);
        visualTransition.setLabelPositioning(Positioning.BOTTOM);
        visualTransition.setNamePositioning(Positioning.LEFT);

        connectVisualPlaceAndVisualTransition(visualPetri, visualPlace, visualTransition, ConnectionDirection.PLACE_TO_TRANSITION);
    }
    private void drawRemainingVisualObjects(
            ArrayList<ArrayList<String>> edgeCliqueCover,
            VisualPetri visualPetri,
            HashSet<String> inputVertexNames,
            boolean isRoot) {

        for (ArrayList<String> clique : edgeCliqueCover) {
            if ((clique != null) && !clique.isEmpty()) {
                VisualPlace place = createVisualPlace(visualPetri);
                place.setNamePositioning(Positioning.LEFT);
                boolean connectsOnlyFromPlaceToTransitions = true;

                for (String vertexName : clique) {

                    boolean isClone = false;
                    String cleanVertexName;

                    if (vertexName.contains(SPECIAL_CLONE_CHARACTER)) {
                        int charIndex = vertexName.indexOf(SPECIAL_CLONE_CHARACTER);
                        cleanVertexName = vertexName.substring(0, charIndex);
                        isClone = true;
                    } else {
                        cleanVertexName = vertexName;
                    }

                    VisualTransition visualTransition;
                    if (!transitionNameToVisualTransition.containsKey(cleanVertexName)) {
                        visualTransition = createVisualTransition(visualPetri);
                        transitionNameToVisualTransition.put(cleanVertexName, visualTransition);

                        visualTransition.setLabel(ExpressionUtils.labelToName.get(cleanVertexName));
                        visualTransition.setLabelPositioning(Positioning.BOTTOM);
                        visualTransition.setNamePositioning(Positioning.LEFT);

                    } else {
                        visualTransition = transitionNameToVisualTransition.get(cleanVertexName);
                    }

                    if (inputVertexNames.contains(cleanVertexName) || isClone) {
                        connectVisualPlaceAndVisualTransition(visualPetri, place, visualTransition, ConnectionDirection.TRANSITION_TO_PLACE);
                        connectsOnlyFromPlaceToTransitions = false;
                        place.getReferencedComponent().setTokens(isRoot ? 1 : 0);
                    } else {
                        connectVisualPlaceAndVisualTransition(visualPetri, place, visualTransition, ConnectionDirection.PLACE_TO_TRANSITION);
                    }
                }
                place.getReferencedComponent().setTokens(connectsOnlyFromPlaceToTransitions ? 1 : 0);
            }
        }
    }

    private void drawIsolatedVisualObjects(Graph inputGraph, VisualPetri visualPetri, Boolean isSequence, Boolean isRoot) {
        if (inputGraph.getIsolatedVertices() != null && !inputGraph.getVertices().isEmpty()) {
            for (String vertex : inputGraph.getIsolatedVertices()) {
                VisualPlace visualPlace = createVisualPlace(visualPetri);
                visualPlace.getReferencedComponent().setTokens(1);

                VisualTransition visualTransition = !transitionNameToVisualTransition.containsKey(vertex) && !isSequence ?
                        createVisualTransition(visualPetri) :
                        isRoot ? transitionNameToVisualTransition.get(vertex) : null;

                if (visualTransition != null) {
                    visualTransition.setLabel(ExpressionUtils.labelToName.get(vertex));
                    visualTransition.setLabelPositioning(Positioning.BOTTOM);
                    visualTransition.setNamePositioning(Positioning.LEFT);
                    visualPlace.setNamePositioning(Positioning.LEFT);
                }

                transitionNameToVisualTransition.put(vertex, visualTransition);
                connectVisualPlaceAndVisualTransition(visualPetri, visualPlace, visualTransition, ConnectionDirection.PLACE_TO_TRANSITION);
            }
        }
    }

    private VisualPlace createVisualPlace(VisualPetri visualPetri) {
        return visualPetri.createPlace(null, null);
    }
    private VisualTransition createVisualTransition(VisualPetri visualPetri) {
        return visualPetri.createTransition(null, null);
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
        } catch (InvalidConnectionException ignored) {
        }
    }
}
