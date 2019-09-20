package org.workcraft.plugins.graph.tools;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.gui.tools.Decorator;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.plugins.builtin.settings.SimulationDecorationSettings;
import org.workcraft.plugins.graph.VisualGraph;
import org.workcraft.plugins.graph.VisualVertex;
import org.workcraft.plugins.graph.converters.GraphToPetriConverter;
import org.workcraft.plugins.petri.*;
import org.workcraft.plugins.petri.tools.PetriSimulationTool;

import java.awt.*;
import java.awt.event.MouseEvent;

public class GraphSimulationTool extends PetriSimulationTool {

    private GraphToPetriConverter converter;

    @Override
    public void activated(final GraphEditor editor) {
        super.activated(editor);
        setStatePaneVisibility(false);
    }

    @Override
    public String getTraceLabelByReference(String ref) {
        String label = null;
        if (ref != null) {
            label = converter.getSymbol(ref);
            if (label.isEmpty()) {
                label = Character.toString(VisualVertex.EPSILON_SYMBOL);
            }
        }
        if (label == null) {
            label = super.getTraceLabelByReference(ref);
        }
        return label;
    }

    @Override
    public void generateUnderlyingModel(VisualModel model) {
        final VisualGraph graph = (VisualGraph) model;
        final VisualPetri petri = new VisualPetri(new Petri());
        converter = new GraphToPetriConverter(graph, petri);
        setUnderlyingModel(converter.getDstModel());
    }

    @Override
    public void applySavedState(final GraphEditor editor) {
        // Not applicable to this model
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            VisualModel model = e.getModel();
            Node deepestNode = HitMan.hitDeepest(e.getPosition(), model.getRoot(),
                    node -> getExcitedTransitionOfNode(node) != null);

            Transition transition = getExcitedTransitionOfNode(deepestNode);
            if (transition != null) {
                executeTransition(e.getEditor(), transition);
            }
        }
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        return "Click on a highlighted vertex to progress.";
    }

    @Override
    public Decorator getDecorator(final GraphEditor editor) {
        return new Decorator() {
            @Override
            public Decoration getDecoration(Node node) {
                if (converter == null) return null;
                if (node instanceof VisualVertex) {
                    return getVertexDecoration((VisualVertex) node);
                } else if (node instanceof VisualConnection) {
                    return getConnectionDecorator((VisualConnection) node);
                } else if (node instanceof VisualPage || node instanceof VisualGroup) {
                    return getContainerDecoration((Container) node);
                }

                return null;
            }
        };
    }

    private Decoration getVertexDecoration(VisualVertex vertex) {
        Node transition = getTraceCurrentNode();
        final boolean isExcited = isVertexExcited(vertex);
        final boolean isSuggested = isExcited && converter.isRelated(vertex, transition);
        return new Decoration() {
            @Override
            public Color getColorisation() {
                return isExcited ? SimulationDecorationSettings.getExcitedComponentColor() : null;
            }

            @Override
            public Color getBackground() {
                return isSuggested ? SimulationDecorationSettings.getSuggestedComponentColor() : null;
            }
        };
    }

    protected Decoration getConnectionDecorator(VisualConnection connection) {
        final boolean isExcited = isConnectionExcited(connection);
        return new Decoration() {
            @Override
            public Color getColorisation() {
                return isExcited ? SimulationDecorationSettings.getExcitedComponentColor() : null;
            }

            @Override
            public Color getBackground() {
                return null;
            }
        };
    }

    @Override
    public boolean isConnectionExcited(VisualConnection connection) {
        VisualPlace place = converter.getRelatedPlace(connection);
        return (place == null) ? false : place.getReferencedPlace().getTokens() != 0;
    }

    private boolean isVertexExcited(VisualVertex vertex) {
        VisualTransition transition = converter.getRelatedTransition(vertex);
        return (transition == null) ? false : isEnabledNode(transition.getReferencedTransition());
    }

    private Transition getExcitedTransitionOfNode(Node node) {
        if ((node != null) && (node instanceof VisualVertex)) {
            VisualTransition vTransition = converter.getRelatedTransition((VisualVertex) node);
            if (vTransition != null) {
                Transition transition = vTransition.getReferencedTransition();
                if (isEnabledNode(transition)) {
                    return transition;
                }
            }
        }
        return null;
    }

}
