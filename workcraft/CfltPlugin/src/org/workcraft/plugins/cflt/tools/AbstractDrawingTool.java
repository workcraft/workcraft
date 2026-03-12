package org.workcraft.plugins.cflt.tools;

import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.cflt.graph.Clique;
import org.workcraft.plugins.cflt.graph.Graph;
import org.workcraft.plugins.cflt.graph.Vertex;
import org.workcraft.plugins.cflt.node.NodeCollection;
import org.workcraft.utils.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.*;

import static org.workcraft.plugins.cflt.utils.EdgeCliqueCoverUtils.getEdgeCliqueCover;

public abstract class AbstractDrawingTool implements VisualModelDrawingTool {

    protected final Map<String, VisualNode> transitionMap = new HashMap<>();
    protected final NodeCollection nodeCollection;

    protected AbstractDrawingTool(NodeCollection nodeCollection) {
        this.nodeCollection = nodeCollection;
    }

    protected abstract AbstractVisualModel getModel(WorkspaceEntry we);

    protected abstract VisualNode createPlace(
            AbstractVisualModel model,
            boolean hasToken,
            Positioning positioning
    );

    protected abstract VisualNode createTransition(
            AbstractVisualModel model,
            String name
    );

    protected void afterDraw(AbstractVisualModel model) {
        // default: do nothing
    }

    protected void connect(
            AbstractVisualModel model,
            VisualNode place,
            VisualNode transition,
            ConnectionDirection direction) {

        try {
            if (direction == ConnectionDirection.PLACE_TO_TRANSITION) {
                model.connect(place, transition);
            } else {
                model.connect(transition, place);
            }
        } catch (InvalidConnectionException e) {
            LogUtils.logError("Invalid connection of place and transition");
            e.printStackTrace();
        }
    }

    @Override
    public void drawSingleTransition(String name, WorkspaceEntry we) {

        AbstractVisualModel model = getModel(we);

        VisualNode place = createPlace(model, true, Positioning.LEFT);
        VisualNode transition = createTransition(model, name);

        connect(
                model,
                place,
                transition,
                ConnectionDirection.PLACE_TO_TRANSITION
        );
    }

    @Override
    public void drawVisualObjects(DrawVisualObjectsRequest request) {

        List<Clique> edgeCliqueCover = getEdgeCliqueCover(
                request.inputGraph(),
                request.outputGraph(),
                request.isSequence(),
                request.mode()
        );

        Set<Vertex> inputVertices = request.isSequence()
                ? new HashSet<>(request.inputGraph().getVertices())
                : new HashSet<>();

        AbstractVisualModel model = getModel(request.workspaceEntry());

        if (request.inputGraph().getIsolatedVertices() != null) {
            drawIsolatedVisualObjects(
                    request.inputGraph(),
                    model,
                    request.isSequence(),
                    request.isRoot()
            );
        }

        drawRemainingVisualObjects(
                edgeCliqueCover,
                model,
                inputVertices,
                request.isRoot()
        );

        afterDraw(model);
    }

    private void drawRemainingVisualObjects(
            List<Clique> edgeCliqueCover,
            AbstractVisualModel model,
            Set<Vertex> inputVertices,
            boolean isRoot) {

        for (Clique clique : edgeCliqueCover) {

            VisualNode place = createPlace(model, isRoot, Positioning.LEFT);

            for (Vertex vertex : clique.getVertices()) {

                String name = vertex.name();
                boolean isClone = vertex.isClone();

                VisualNode transition = transitionMap.computeIfAbsent(
                        name,
                        n -> createTransition(model, n)
                );

                ConnectionDirection direction =
                        inputVertices.contains(vertex) || isClone
                                ? ConnectionDirection.TRANSITION_TO_PLACE
                                : ConnectionDirection.PLACE_TO_TRANSITION;

                connect(model, place, transition, direction);
            }
        }
    }

    private void drawIsolatedVisualObjects(
            Graph inputGraph,
            AbstractVisualModel model,
            boolean isSequence,
            boolean isRoot) {

        for (Vertex vertex : inputGraph.getIsolatedVertices()) {

            String name = vertex.name();

            boolean transitionExists = transitionMap.containsKey(name);
            boolean shouldCreateNewTransition = !transitionExists && !isSequence;

            if (!shouldCreateNewTransition && !isRoot) {
                continue;
            }

            VisualNode place;
            VisualNode transition;

            if (shouldCreateNewTransition) {
                place = createPlace(model, true, Positioning.LEFT);
                transition = createTransition(model, name);
            } else {
                place = createPlace(model, true, Positioning.TOP);
                transition = transitionMap.get(name);
            }

            transitionMap.put(name, transition);

            connect(
                    model,
                    place,
                    transition,
                    ConnectionDirection.PLACE_TO_TRANSITION
            );
        }
    }
}