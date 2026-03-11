package org.workcraft.plugins.cflt.tools;

import java.util.*;

import org.workcraft.dom.visual.Positioning;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.cflt.graph.Clique;
import org.workcraft.plugins.cflt.graph.Graph;
import org.workcraft.plugins.cflt.graph.Vertex;
import org.workcraft.plugins.cflt.node.NodeCollection;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

import static org.workcraft.plugins.cflt.utils.EdgeCliqueCoverUtils.getEdgeCliqueCover;

public class PetriDrawingTool implements VisualModelDrawingTool {
    private final Map<String, VisualTransition> transitionNameToVisualTransition = new HashMap<>();
    private final NodeCollection nodeCollection;

    public PetriDrawingTool(final NodeCollection nodeCollection) {
        this.nodeCollection = nodeCollection;
    }

    @Override
    public void drawVisualObjects(DrawVisualObjectsRequest request) {

        List<Clique> edgeCliqueCover = getEdgeCliqueCover(
                request.inputGraph(),
                request.outputGraph(),
                request.isSequence(),
                request.mode()
        );

        List<Vertex> vertices = request.isSequence()
                ? request.inputGraph().getVertices()
                : new ArrayList<>();

        HashSet<Vertex> inputVertices = new HashSet<>(vertices);

        VisualPetri visualPetri = WorkspaceUtils.getAs(request.workspaceEntry(), VisualPetri.class);

        if (request.inputGraph().getIsolatedVertices() != null) {
            this.drawIsolatedVisualObjects(
                    request.inputGraph(),
                    visualPetri,
                    request.isSequence(),
                    request.isRoot()
            );
        }

        this.drawRemainingVisualObjects(
                edgeCliqueCover,
                visualPetri,
                inputVertices,
                request.isRoot()
        );
    }

    @Override
    public void drawSingleTransition(String name, WorkspaceEntry we) {
        VisualPetri visualPetri = WorkspaceUtils.getAs(we, VisualPetri.class);
        VisualPlace visualPlace = createVisualPlace(visualPetri, true, Positioning.LEFT);
        VisualTransition visualTransition = createVisualTransition(visualPetri, name);
        connectVisualPlaceAndVisualTransition(visualPetri, visualPlace, visualTransition, ConnectionDirection.PLACE_TO_TRANSITION);
    }

    private void drawRemainingVisualObjects(List<Clique> edgeCliqueCover, VisualPetri visualPetri,
            Set<Vertex> inputVertices, boolean isRoot) {
        for (Clique clique : edgeCliqueCover) {
            VisualPlace visualPlace = createVisualPlace(visualPetri, isRoot, Positioning.LEFT);

            for (Vertex vertex : clique.getVertices()) {
                String name = vertex.name();
                boolean isClone = vertex.isClone();
                boolean isTransitionPresent = transitionNameToVisualTransition.containsKey(name);

                VisualTransition visualTransition = isTransitionPresent
                        ? transitionNameToVisualTransition.get(name)
                        : createVisualTransition(visualPetri, name);

                transitionNameToVisualTransition.put(name, visualTransition);

                ConnectionDirection connectionDirection = inputVertices.contains(vertex) || isClone
                        ? ConnectionDirection.TRANSITION_TO_PLACE
                        : ConnectionDirection.PLACE_TO_TRANSITION;

                connectVisualPlaceAndVisualTransition(visualPetri, visualPlace, visualTransition, connectionDirection);
            }
        }
    }

    private void drawIsolatedVisualObjects(Graph inputGraph, VisualPetri visualPetri, boolean isSequence, boolean isRoot) {
        for (Vertex vertex : inputGraph.getIsolatedVertices()) {
            String name = vertex.name();
            boolean isTransitionNamePresent = transitionNameToVisualTransition.containsKey(name);

            VisualPlace visualPlace = null;
            if (!isTransitionNamePresent && !isSequence) {
                visualPlace = createVisualPlace(visualPetri, true, Positioning.LEFT);
            } else if (isRoot) {
                visualPlace = createVisualPlace(visualPetri, true, Positioning.TOP);
            }

            VisualTransition visualTransition = null;
            if (!isTransitionNamePresent && !isSequence) {
                visualTransition = createVisualTransition(visualPetri, name);
            } else if (isRoot) {
                visualTransition = transitionNameToVisualTransition.get(name);
            }

            if (visualPlace != null && visualTransition != null) {
                transitionNameToVisualTransition.put(name, visualTransition);
                connectVisualPlaceAndVisualTransition(visualPetri, visualPlace, visualTransition, ConnectionDirection.PLACE_TO_TRANSITION);
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
                case PLACE_TO_TRANSITION -> visualPetri.connect(visualPlace, visualTransition);
                case TRANSITION_TO_PLACE -> visualPetri.connect(visualTransition, visualPlace);
            }
        } catch (InvalidConnectionException e) {
            LogUtils.logError("Invalid connection of VisualPlace and VisualTransition");
            e.printStackTrace();
        }
    }
}
