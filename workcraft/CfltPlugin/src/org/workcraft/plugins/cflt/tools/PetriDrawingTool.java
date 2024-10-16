package org.workcraft.plugins.cflt.tools;

import java.util.*;

import org.workcraft.dom.visual.Positioning;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.cflt.graph.Clique;
import org.workcraft.plugins.cflt.graph.Graph;
import org.workcraft.plugins.cflt.node.NodeCollection;
import org.workcraft.plugins.cflt.presets.ExpressionParameters.Mode;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

import static org.workcraft.plugins.cflt.utils.EdgeCliqueCoverUtils.getEdgeCliqueCover;
import static org.workcraft.plugins.cflt.utils.GraphUtils.SPECIAL_CLONE_CHARACTER;

public class PetriDrawingTool {
    private final Map<String, VisualTransition> transitionNameToVisualTransition = new HashMap<>();
    private final NodeCollection nodeCollection = NodeCollection.getInstance();

    public void drawPetri(Graph inputGraph, Graph outputGraph,
            boolean isSequence, boolean isRoot, Mode mode, WorkspaceEntry we) {
        VisualPetri visualPetri = WorkspaceUtils.getAs(we, VisualPetri.class);
        List<Clique> edgeCliqueCover = getEdgeCliqueCover(inputGraph, outputGraph, isSequence, mode);

        HashSet<String> inputVertexNames = new HashSet<>(isSequence
                ? inputGraph.getVertexNames()
                : new ArrayList<>());

        this.drawIsolatedVisualObjects(inputGraph, visualPetri, isSequence, isRoot);
        this.drawRemainingVisualObjects(edgeCliqueCover, visualPetri, inputVertexNames, isRoot);
    }

    public void drawSingleTransition(String name, WorkspaceEntry we) {
        VisualPetri visualPetri = WorkspaceUtils.getAs(we, VisualPetri.class);
        VisualPlace visualPlace = createVisualPlace(visualPetri, true, Positioning.LEFT);
        VisualTransition visualTransition = createVisualTransition(visualPetri, name);

        connectVisualPlaceAndVisualTransition(visualPetri, visualPlace, visualTransition, ConnectionDirection.PLACE_TO_TRANSITION);
    }

    private void drawRemainingVisualObjects(List<Clique> edgeCliqueCover, VisualPetri visualPetri,
            HashSet<String> inputVertexNames, boolean isRoot) {

        for (Clique clique : edgeCliqueCover) {
            VisualPlace visualPlace = createVisualPlace(visualPetri, isRoot, Positioning.LEFT);
            for (String vertexName : clique.getVertexNames()) {
                boolean isClone = vertexName.contains(SPECIAL_CLONE_CHARACTER);

                String cleanVertexName = isClone
                        ? vertexName.substring(0, vertexName.indexOf(SPECIAL_CLONE_CHARACTER))
                        : vertexName;

                boolean isTransitionPresent = transitionNameToVisualTransition.containsKey(cleanVertexName);

                VisualTransition visualTransition = isTransitionPresent
                        ? transitionNameToVisualTransition.get(cleanVertexName)
                        : createVisualTransition(visualPetri, cleanVertexName);

                transitionNameToVisualTransition.put(cleanVertexName, visualTransition);

                ConnectionDirection connectionDirection = inputVertexNames.contains(cleanVertexName) || isClone
                        ? ConnectionDirection.TRANSITION_TO_PLACE
                        : ConnectionDirection.PLACE_TO_TRANSITION;

                connectVisualPlaceAndVisualTransition(visualPetri, visualPlace, visualTransition, connectionDirection);
            }
        }
    }

    private void drawIsolatedVisualObjects(Graph inputGraph, VisualPetri visualPetri, boolean isSequence, boolean isRoot) {
        if (inputGraph.getIsolatedVertices() != null) {
            for (String vertex : inputGraph.getIsolatedVertices()) {
                boolean isTransitionNamePresent = transitionNameToVisualTransition.containsKey(vertex);

                VisualPlace visualPlace = null;

                if (!isTransitionNamePresent && !isSequence) {
                    visualPlace = createVisualPlace(visualPetri, true, Positioning.LEFT);
                } else if (isRoot) {
                    visualPlace = createVisualPlace(visualPetri, true, Positioning.TOP);
                }

                VisualTransition visualTransition = null;

                if (!isTransitionNamePresent && !isSequence) {
                    visualTransition = createVisualTransition(visualPetri, vertex);
                } else if (isRoot) {
                    visualTransition = transitionNameToVisualTransition.get(vertex);
                }

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
        String label = nodeCollection.getNodeDetails(name).getLabel();
        VisualTransition visualTransition = visualPetri.createTransition(null, null);

        visualTransition.setLabel(label);
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
        } catch (InvalidConnectionException e) {
            LogUtils.logError("Invalid connection of VisualPlace and VisualTransition");
            e.printStackTrace();
        }
    }
}
