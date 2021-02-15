package org.workcraft.plugins.policy.tools;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.tools.ContainerDecoration;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.gui.tools.Decorator;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.plugins.builtin.settings.SimulationDecorationSettings;
import org.workcraft.plugins.petri.PetriModel;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.petri.tools.PetriSimulationTool;
import org.workcraft.plugins.petri.tools.PlaceDecoration;
import org.workcraft.plugins.policy.Bundle;
import org.workcraft.plugins.policy.Policy;
import org.workcraft.plugins.policy.VisualBundledTransition;
import org.workcraft.plugins.policy.VisualPolicy;
import org.workcraft.plugins.policy.converters.PolicyToPetriConverter;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Collection;

public class PolicySimulationTool extends PetriSimulationTool {
    private PolicyToPetriConverter converter;

    @Override
    public void activated(final GraphEditor editor) {
        super.activated(editor);
        setStatePaneVisibility(false);
    }

    @Override
    public void generateUnderlyingModel(WorkspaceEntry we) {
        converter = new PolicyToPetriConverter(WorkspaceUtils.getAs(we, VisualPolicy.class));
    }

    @Override
    public PetriModel getUnderlyingModel() {
        return converter.getPetriNet().getMathModel();
    }

    @Override
    public VisualModel getUnderlyingVisualModel() {
        return converter.getPetriNet();
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
    public boolean isContainerExcited(VisualModel model, Container container) {
        if (excitedContainers.containsKey(container)) {
            return excitedContainers.get(container);
        }
        boolean result = false;
        for (Node node: container.getChildren()) {

            if (node instanceof VisualBundledTransition) {
                result = result || (getExcitedTransitionOfNode(node) != null);
            }

            if (node instanceof Container) {
                result = result || isContainerExcited(model, (Container) node);
            }

            if (result) break;
        }

        excitedContainers.put(container, result);
        return result;
    }

    @Override
    public Decorator getDecorator(final GraphEditor editor) {
        return node -> {

            if ((node instanceof VisualPage) || (node instanceof VisualGroup)) {
                if (node.getParent() == null) return null; // do not work with the root node
                VisualModel model = editor.getModel();
                final boolean ret = isContainerExcited(model, (Container) node);
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

            if (node instanceof VisualBundledTransition) {
                final boolean isExcited = getExcitedTransitionOfNode(node) != null;
                Node transition = getCurrentUnderlyingNode();
                final boolean isSuggested = isExcited && converter.isRelated(node, transition);
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

            if (node instanceof VisualPlace) {
                final VisualPlace p = converter.getRelatedPlace((VisualPlace) node);
                return new PlaceDecoration() {
                    @Override
                    public Color getColorisation() {
                        return null;
                    }
                    @Override
                    public Color getBackground() {
                        return null;
                    }
                    @Override
                    public int getTokens() {
                        return p == null ? 0 : p.getReferencedComponent().getTokens();
                    }
                    @Override
                    public Color getTokenColor() {
                        return p.getTokenColor();
                    }
                };
            }

            return null;
        };
    }

    private Transition getExcitedTransitionOfNode(Node node) {
        Collection<VisualTransition> ts = null;
        if (node instanceof VisualBundledTransition) {
            ts = converter.getRelatedTransitions((VisualBundledTransition) node);
        }
        return getExcitedTransitionOfCollection(ts);
    }

    private Transition getExcitedTransitionOfCollection(Collection<VisualTransition> ts) {
        if (ts != null) {
            for (VisualTransition t: ts) {
                if (t == null) continue;
                Transition transition = t.getReferencedComponent();
                if (isEnabledUnderlyingNode(transition)) {
                    return transition;
                }
            }
        }
        return null;
    }

    @Override
    public String getTraceLabelByReference(String ref) {
        String label = null;
        if (ref != null) {
            Policy policy = converter.getPolicyNet().getMathModel();
            Node node = policy.getNodeByReference(ref);
            if (node instanceof Bundle) {
                Bundle bundle = (Bundle) node;
                label = policy.getTransitionsOfBundleAsString(bundle);
            } else {
                label = ref;
            }
        }
        if (label == null) {
            label = super.getTraceLabelByReference(ref);
        }
        return label;
    }

}
