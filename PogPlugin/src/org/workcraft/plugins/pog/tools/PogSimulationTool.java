package org.workcraft.plugins.pog.tools;

import java.awt.Color;
import java.awt.geom.Point2D;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.ContainerDecoration;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.petri.tools.PetriNetSimulationTool;
import org.workcraft.plugins.pog.VisualPog;
import org.workcraft.plugins.pog.VisualVertex;
import org.workcraft.plugins.shared.CommonSimulationSettings;
import org.workcraft.util.Func;

public class PogSimulationTool extends PetriNetSimulationTool {

    private PogToPnConverter generator;

    @Override
    public void activated(final GraphEditor editor) {
        super.activated(editor);
        setStatePaneVisibility(false);
    }

    @Override
    public String getTraceLabelByReference(String ref) {
        String label = null;
        if (ref != null) {
            label = generator.getSymbol(ref);
            if (label == "") {
                label = Character.toString(VisualVertex.EPSILON_SYMBOL);
            }
        }
        if (label == null) {
            label = super.getTraceLabelByReference(ref);
        }
        return label;
    }

    @Override
    public VisualModel getUnderlyingModel(VisualModel model) {
        final VisualPog pog = (VisualPog) model;
        final VisualPetriNet pn = new VisualPetriNet(new PetriNet());
        generator = new PogToPnConverter(pog, pn);
        return generator.getDstModel();
    }

    @Override
    public void applyInitState(final GraphEditor editor) {
        // Not applicable to this model
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent e) {
        Point2D posRoot = e.getPosition();
        Node node = HitMan.hitDeepest(posRoot, e.getModel().getRoot(),
                new Func<Node, Boolean>() {
                    @Override
                    public Boolean eval(Node node) {
                        return getExcitedTransitionOfNode(node) != null;
                    }
                });

        Transition transition = getExcitedTransitionOfNode(node);
        if (transition != null) {
            executeTransition(e.getEditor(), transition);
        }
    }

    @Override
    protected boolean isContainerExcited(Container container) {
        if (excitedContainers.containsKey(container)) return excitedContainers.get(container);
        boolean ret = false;
        for (Node node: container.getChildren()) {
            if (node instanceof VisualVertex) {
                ret = ret || (getExcitedTransitionOfNode(node) != null);
            }
            if (node instanceof Container) {
                ret = ret || isContainerExcited((Container) node);
            }
            if (ret) break;
        }
        excitedContainers.put(container, ret);
        return ret;
    }

    @Override
    public Decorator getDecorator(final GraphEditor editor) {
        return new Decorator() {
            @Override
            public Decoration getDecoration(Node node) {
                Node transition = getTraceCurrentNode();
                final boolean isExcited = getExcitedTransitionOfNode(node) != null;
                final boolean isHighlighted = generator.isRelated(node, transition);

                if (node instanceof VisualVertex) {
                    return new Decoration() {
                        @Override
                        public Color getColorisation() {
                            if (isHighlighted) return CommonSimulationSettings.getEnabledBackgroundColor();
                            if (isExcited) return CommonSimulationSettings.getEnabledForegroundColor();
                            return null;
                        }

                        @Override
                        public Color getBackground() {
                            if (isHighlighted) return CommonSimulationSettings.getEnabledForegroundColor();
                            if (isExcited) return CommonSimulationSettings.getEnabledBackgroundColor();
                            return null;
                        }
                    };
                }

                if (node instanceof VisualPage || node instanceof VisualGroup) {
                    if (node.getParent() == null) return null; // do not work with the root node
                    final boolean ret = isContainerExcited((Container) node);
                    return new ContainerDecoration() {
                        @Override
                        public Color getColorisation() {
                            return null;
                        }
                        @Override
                        public Color getBackground() {
                            return null;
                        }
                        @Override
                        public boolean isContainerExcited() {
                            return ret;
                        }
                    };

                }

                return null;
            }
        };
    }

    private Transition getExcitedTransitionOfNode(Node node) {
        if ((node != null) && (node instanceof VisualVertex)) {
            VisualTransition vTransition = generator.getRelatedTransition((VisualVertex) node);
            if (vTransition != null) {
                Transition transition = vTransition.getReferencedTransition();
                if (net.isEnabled(transition)) {
                    return transition;
                }
            }
        }
        return null;
    }

}
