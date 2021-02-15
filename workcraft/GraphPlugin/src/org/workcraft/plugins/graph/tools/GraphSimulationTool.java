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
import org.workcraft.plugins.petri.PetriModel;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.petri.tools.PetriSimulationTool;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

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
                label = VisualVertex.EPSILON_SYMBOL;
            }
        }
        if (label == null) {
            label = super.getTraceLabelByReference(ref);
        }
        return label;
    }

    @Override
    public void generateUnderlyingModel(WorkspaceEntry we) {
        converter = new GraphToPetriConverter(WorkspaceUtils.getAs(we, VisualGraph.class));
    }

    @Override
    public PetriModel getUnderlyingModel() {
        return converter.getDstModel().getMathModel();
    }

    @Override
    public VisualModel getUnderlyingVisualModel() {
        return converter.getDstModel();
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
                executeUnderlyingNode(e.getEditor(), transition);
            }
        }
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        return "Click on a highlighted vertex to progress.";
    }

    @Override
    public Decorator getDecorator(final GraphEditor editor) {
        return node -> {
            if (converter == null) {
                return null;
            }
            VisualModel model = editor.getModel();
            if ((node instanceof VisualPage) || (node instanceof VisualGroup)) {
                return getContainerDecoration(model, (Container) node);
            }
            if (node instanceof VisualVertex) {
                return getVertexDecoration((VisualVertex) node);
            }
            if (node instanceof VisualConnection) {
                return getConnectionDecoration(model, (VisualConnection) node);
            }

            return null;
        };
    }

    private Decoration getVertexDecoration(VisualVertex vertex) {
        final boolean isExcited = isVertexExcited(vertex);
        Node transition = getCurrentUnderlyingNode();
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

    @Override
    public boolean isConnectionExcited(VisualModel model, VisualConnection connection) {
        VisualPlace place = converter.getRelatedPlace(connection);
        return (place != null) && (place.getReferencedComponent().getTokens() != 0);
    }

    private boolean isVertexExcited(VisualVertex vertex) {
        VisualTransition transition = converter.getRelatedTransition(vertex);
        return (transition != null) && isEnabledUnderlyingNode(transition.getReferencedComponent());
    }

    private Transition getExcitedTransitionOfNode(Node node) {
        if (node instanceof VisualVertex) {
            VisualTransition vTransition = converter.getRelatedTransition((VisualVertex) node);
            if (vTransition != null) {
                Transition transition = vTransition.getReferencedComponent();
                if (isEnabledUnderlyingNode(transition)) {
                    return transition;
                }
            }
        }
        return null;
    }

}
