package org.workcraft.plugins.cflt.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.cflt.graph.Clique;
import org.workcraft.plugins.cflt.graph.Vertex;
import org.workcraft.plugins.cflt.node.NodeCollection;
import org.workcraft.utils.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

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
    public void renderGraph(RenderGraphRequest request) {

        AbstractVisualModel model = getModel(request.workspaceEntry());

        if (request.isolatedVertices() != null && !request.isolatedVertices().isEmpty()) {
            drawIsolatedVisualObjects(
                    request.isolatedVertices(),
                    model,
                    request.isRoot()
            );
        }

        drawRemainingVisualObjects(
                request.cliques(),
                model,
                request.inputVertices(),
                request.isRoot()
        );

        afterDraw(model);
    }

    private void drawRemainingVisualObjects(
            List<Clique> cliques,
            AbstractVisualModel model,
            Set<Vertex> inputVertices,
            boolean isRoot) {

        for (Clique clique : cliques) {

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
            List<Vertex> isolatedVertices,
            AbstractVisualModel model,
            boolean isRoot) {

        for (Vertex vertex : isolatedVertices) {

            String name = vertex.name();

            boolean transitionExists = transitionMap.containsKey(name);

            if (!transitionExists && !isRoot) {
                continue;
            }

            VisualNode place;
            VisualNode transition;

            if (!transitionExists) {
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