package org.workcraft.plugins.dfs.tools;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.gui.tools.Decorator;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.plugins.builtin.settings.SimulationDecorationSettings;
import org.workcraft.plugins.dfs.BinaryRegister.Marking;
import org.workcraft.plugins.dfs.*;
import org.workcraft.plugins.dfs.decorations.*;
import org.workcraft.plugins.dfs.stg.*;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.tools.StgSimulationTool;
import org.workcraft.utils.Hierarchy;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class DfsSimulationTool extends StgSimulationTool {
    private DfsToStgConverter converter;

    @Override
    public void deactivated(GraphEditor editor) {
        super.deactivated(editor);
        this.converter = null;
    }

    @Override
    public void generateUnderlyingModel(VisualModel model) {
        converter = new DfsToStgConverter((VisualDfs) model);
        setUnderlyingModel(converter.getStgModel());
    }

    private VisualPlace getVisualPlace(Place place) {
        VisualPlace result = null;
        for (VisualPlace vp: Hierarchy.getDescendantsOfType(getUnderlyingModel().getRoot(), VisualPlace.class)) {
            if (vp.getReferencedComponent() == place) {
                result = vp;
                break;
            }
        }
        return result;
    }

    private void copyTokenColor(VisualAbstractRegister r, Node nodeM) {
        VisualPlace vp = getVisualPlace((Place) nodeM);
        if (vp != null) {
            r.setTokenColor(vp.getTokenColor());
        }
    }

    @Override
    public void applySavedState(final GraphEditor editor) {
        if ((savedState == null) || savedState.isEmpty()) {
            return;
        }
        editor.getWorkspaceEntry().saveMemento();
        VisualDfs dfs = (VisualDfs) editor.getModel();
        for (VisualLogic node : dfs.getVisualLogics()) {
            String c1Ref = DfsToStgConverter.getLogicStgNodeReference(dfs.getMathReference(node), true);
            Node c1Node = getUnderlyingStg().getNodeByReference(c1Ref);
            if ((c1Node instanceof Place) && savedState.containsKey(c1Node)) {
                boolean computed = savedState.get(c1Node) > 0;
                node.getReferencedComponent().setComputed(computed);
            }
        }
        for (VisualRegister node : dfs.getVisualRegisters()) {
            String m1Ref = DfsToStgConverter.getRegisterStgNodeReference(dfs.getMathReference(node), true);
            Node m1Node = getUnderlyingStg().getNodeByReference(m1Ref);
            if ((m1Node instanceof Place) && savedState.containsKey(m1Node)) {
                boolean marked = savedState.get(m1Node) > 0;
                node.getReferencedComponent().setMarked(marked);
                copyTokenColor(node, m1Node);
            }
        }
        for (VisualCounterflowLogic node : dfs.getVisualCounterflowLogics()) {
            String fwC1Ref = DfsToStgConverter.getCounterflowLogicStgNodeReference(dfs.getMathReference(node), true, true);
            Node fwC1Node = getUnderlyingStg().getNodeByReference(fwC1Ref);
            if ((fwC1Node instanceof Place) && savedState.containsKey(fwC1Node)) {
                boolean forwardComputed = savedState.get(fwC1Node) > 0;
                node.getReferencedComponent().setForwardComputed(forwardComputed);
            }
            String bwC1Ref = DfsToStgConverter.getCounterflowLogicStgNodeReference(dfs.getMathReference(node), false, true);
            Node bwC1Node = getUnderlyingStg().getNodeByReference(bwC1Ref);
            if ((bwC1Node instanceof Place) && savedState.containsKey(bwC1Node)) {
                boolean backwardComputed = savedState.get(bwC1Node) > 0;
                node.getReferencedComponent().setBackwardComputed(backwardComputed);
            }
        }
        for (VisualCounterflowRegister node : dfs.getVisualCounterflowRegisters()) {
            String orM1Ref = DfsToStgConverter.getCounterflowRegisterStgNodeReference(dfs.getMathReference(node), true, true);
            Node orM1Node = getUnderlyingStg().getNodeByReference(orM1Ref);
            if ((orM1Node instanceof Place) && savedState.containsKey(orM1Node)) {
                boolean orMarked = savedState.get(orM1Node) > 0;
                node.getReferencedComponent().setOrMarked(orMarked);
                copyTokenColor(node, orM1Node);
            }
            String andM1Ref = DfsToStgConverter.getCounterflowRegisterStgNodeReference(dfs.getMathReference(node), false, true);
            Node andM1Node = getUnderlyingStg().getNodeByReference(andM1Ref);
            if ((andM1Node instanceof Place) && savedState.containsKey(andM1Node)) {
                boolean andMarked = savedState.get(andM1Node) > 0;
                node.getReferencedComponent().setAndMarked(andMarked);
                copyTokenColor(node, andM1Node);
            }
        }
        Collection<VisualBinaryRegister> binaryRegisters = new HashSet<>();
        binaryRegisters.addAll(dfs.getVisualControlRegisters());
        binaryRegisters.addAll(dfs.getVisualPushRegisters());
        binaryRegisters.addAll(dfs.getVisualPopRegisters());
        for (VisualBinaryRegister node :  binaryRegisters) {
            node.getReferencedComponent().setMarking(Marking.EMPTY);
            String trueM1Ref = DfsToStgConverter.getBinaryRegisterStgNodeReference(dfs.getMathReference(node), true, true);
            Node trueM1Node = getUnderlyingStg().getNodeByReference(trueM1Ref);
            if ((trueM1Node instanceof Place) && savedState.containsKey(trueM1Node)) {
                if (savedState.get(trueM1Node) > 0) {
                    node.getReferencedComponent().setMarking(Marking.TRUE_TOKEN);
                }
                copyTokenColor(node, trueM1Node);
            }
            String falseM1Ref = DfsToStgConverter.getBinaryRegisterStgNodeReference(dfs.getMathReference(node), false, true);
            Node falseM1Node = getUnderlyingStg().getNodeByReference(falseM1Ref);
            if ((falseM1Node instanceof Place) && savedState.containsKey(falseM1Node)) {
                if (savedState.get(falseM1Node) > 0) {
                    node.getReferencedComponent().setMarking(Marking.FALSE_TOKEN);
                }
                copyTokenColor(node, falseM1Node);
            }
        }
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            VisualModel model = e.getModel();
            Node deepestNode = HitMan.hitDeepest(e.getPosition(), model.getRoot(),
                    node -> getExcitedTransitionOfNode(node) != null);

            Transition transition = null;
            if (deepestNode instanceof VisualTransformableNode) {
                Point2D nodespacePosition = model.getNodeSpacePosition(e.getPosition(), (VisualTransformableNode) deepestNode);
                if (deepestNode instanceof VisualCounterflowLogic) {
                    CounterflowLogicStg nodeStg = converter.getCounterflowLogicStg((VisualCounterflowLogic) deepestNode);
                    if (nodespacePosition.getY() < 0) {
                        transition = getExcitedTransitionOfCollection(nodeStg.getForwardTransitions());
                    } else {
                        transition = getExcitedTransitionOfCollection(nodeStg.getBackwardTransitions());
                    }
                } else if (deepestNode instanceof VisualCounterflowRegister) {
                    CounterflowRegisterStg nodeStg = converter.getCounterflowRegisterStg((VisualCounterflowRegister) deepestNode);
                    if (nodespacePosition.getY() < 0) {
                        transition = getExcitedTransitionOfCollection(nodeStg.getOrTransitions());
                    } else {
                        transition = getExcitedTransitionOfCollection(nodeStg.getAndTransitions());
                    }
                } else if (deepestNode instanceof VisualControlRegister) {
                    BinaryRegisterStg nodeStg = converter.getControlRegisterStg((VisualControlRegister) deepestNode);
                    if (nodespacePosition.getY() < 0) {
                        transition = getExcitedTransitionOfCollection(nodeStg.getTrueTransitions());
                    } else {
                        transition = getExcitedTransitionOfCollection(nodeStg.getFalseTransitions());
                    }
                } else if (deepestNode instanceof VisualPushRegister) {
                    BinaryRegisterStg nodeStg = converter.getPushRegisterStg((VisualPushRegister) deepestNode);
                    if (nodespacePosition.getY() < 0) {
                        transition = getExcitedTransitionOfCollection(nodeStg.getTrueTransitions());
                    } else {
                        transition = getExcitedTransitionOfCollection(nodeStg.getFalseTransitions());
                    }
                } else if (deepestNode instanceof VisualPopRegister) {
                    BinaryRegisterStg nodeStg = converter.getPopRegisterStg((VisualPopRegister) deepestNode);
                    if (nodespacePosition.getY() < 0) {
                        transition = getExcitedTransitionOfCollection(nodeStg.getTrueTransitions());
                    } else {
                        transition = getExcitedTransitionOfCollection(nodeStg.getFalseTransitions());
                    }
                }
            }

            if (transition == null) {
                transition = getExcitedTransitionOfNode(deepestNode);
            }

            if (transition != null) {
                executeTransition(e.getEditor(), transition);
            }
        }
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        return "Click on a highlighted node to progress.";
    }

    @Override
    public Decorator getDecorator(final GraphEditor editor) {
        return new Decorator() {
            @Override
            public Decoration getDecoration(Node node) {
                Node transition = getTraceCurrentNode();
                final boolean isExcited = getExcitedTransitionOfNode(node) != null;
                final boolean isSuggested = isExcited && converter.isRelated(node, transition);

                if (node instanceof VisualLogic) {
                    final LogicStg nodeStg = converter.getLogicStg((VisualLogic) node);

                    return new LogicDecoration() {
                        @Override
                        public Color getColorisation() {
                            return isExcited ? SimulationDecorationSettings.getExcitedComponentColor() : null;
                        }

                        @Override
                        public Color getBackground() {
                            return isSuggested ? SimulationDecorationSettings.getSuggestedComponentColor() : null;
                        }

                        @Override
                        public boolean isComputed() {
                            return nodeStg.c0.getReferencedComponent().getTokens() == 0;
                        }
                    };
                }

                if (node instanceof VisualRegister) {
                    final RegisterStg nodeStg = converter.getRegisterStg((VisualRegister) node);

                    return new RegisterDecoration() {
                        @Override
                        public Color getColorisation() {
                            return isExcited ? SimulationDecorationSettings.getExcitedComponentColor() : null;
                        }

                        @Override
                        public Color getBackground() {
                            return isSuggested ? SimulationDecorationSettings.getSuggestedComponentColor() : null;
                        }

                        @Override
                        public boolean isMarked() {
                            return nodeStg.m0.getReferencedComponent().getTokens() == 0;
                        }

                        @Override
                        public boolean isExcited() {
                            return getExcitedTransitionOfCollection(Arrays.asList(nodeStg.mR, nodeStg.mF)) != null;
                        }

                        @Override
                        public Color getTokenColor() {
                            return nodeStg.m1.getTokenColor();
                        }
                    };
                }

                if (node instanceof VisualCounterflowLogic) {
                    final CounterflowLogicStg nodeStg = converter.getCounterflowLogicStg((VisualCounterflowLogic) node);

                    return new CounterflowLogicDecoration() {
                        @Override
                        public Color getColorisation() {
                            return isExcited ? SimulationDecorationSettings.getExcitedComponentColor() : null;
                        }

                        @Override
                        public Color getBackground() {
                            return isSuggested ? SimulationDecorationSettings.getSuggestedComponentColor() : null;
                        }

                        @Override
                        public boolean isForwardComputed() {
                            return nodeStg.fwC0.getReferencedComponent().getTokens() == 0;
                        }

                        @Override
                        public boolean isBackwardComputed() {
                            return nodeStg.bwC0.getReferencedComponent().getTokens() == 0;
                        }

                        @Override
                        public boolean isForwardComputedExcited() {
                            return getExcitedTransitionOfCollection(nodeStg.getForwardTransitions()) != null;
                        }

                        @Override
                        public boolean isBackwardComputedExcited() {
                            return getExcitedTransitionOfCollection(nodeStg.getBackwardTransitions()) != null;
                        }
                    };
                }

                if (node instanceof VisualCounterflowRegister) {
                    final CounterflowRegisterStg nodeStg = converter.getCounterflowRegisterStg((VisualCounterflowRegister) node);

                    return new CounterflowRegisterDecoration() {
                        @Override
                        public Color getColorisation() {
                            return isExcited ? SimulationDecorationSettings.getExcitedComponentColor() : null;
                        }

                        @Override
                        public Color getBackground() {
                            return isSuggested ? SimulationDecorationSettings.getSuggestedComponentColor() : null;
                        }

                        @Override
                        public boolean isForwardExcited() {
                            return getExcitedTransitionOfCollection(nodeStg.getForwardTransitions()) != null;
                        }

                        @Override
                        public boolean isBackwardExcited() {
                            return getExcitedTransitionOfCollection(nodeStg.getBackwardTransitions()) != null;
                        }

                        @Override
                        public boolean isOrMarked() {
                            return nodeStg.orM0.getReferencedComponent().getTokens() == 0;
                        }

                        @Override
                        public boolean isAndMarked() {
                            return nodeStg.andM0.getReferencedComponent().getTokens() == 0;
                        }

                        @Override
                        public boolean isOrExcited() {
                            return getExcitedTransitionOfCollection(nodeStg.getOrTransitions()) != null;
                        }

                        @Override
                        public boolean isAndExcited() {
                            return getExcitedTransitionOfCollection(nodeStg.getAndTransitions()) != null;
                        }

                        @Override
                        public Color getTokenColor() {
                            return nodeStg.orM1.getTokenColor();
                        }
                    };
                }

                if (node instanceof VisualControlRegister || node instanceof VisualPushRegister || node instanceof VisualPopRegister) {
                    BinaryRegisterStg tmpStg = null;
                    if (node instanceof VisualControlRegister) {
                        tmpStg = converter.getControlRegisterStg((VisualControlRegister) node);
                    }
                    if (node instanceof VisualPushRegister) {
                        tmpStg = converter.getPushRegisterStg((VisualPushRegister) node);
                    }
                    if (node instanceof VisualPopRegister) {
                        tmpStg = converter.getPopRegisterStg((VisualPopRegister) node);
                    }
                    final BinaryRegisterStg nodeStg = tmpStg;

                    return new BinaryRegisterDecoration() {
                        @Override
                        public Color getColorisation() {
                            return isExcited ? SimulationDecorationSettings.getExcitedComponentColor() : null;
                        }

                        @Override
                        public Color getBackground() {
                            return isSuggested ? SimulationDecorationSettings.getSuggestedComponentColor() : null;
                        }

                        @Override
                        public boolean isTrueMarked() {
                            return nodeStg.tM0.getReferencedComponent().getTokens() == 0;
                        }

                        @Override
                        public boolean isTrueExcited() {
                            return getExcitedTransitionOfCollection(nodeStg.getTrueTransitions()) != null;
                        }

                        @Override
                        public boolean isFalseMarked() {
                            return nodeStg.fM0.getReferencedComponent().getTokens() == 0;
                        }

                        @Override
                        public boolean isFalseExcited() {
                            return getExcitedTransitionOfCollection(nodeStg.getFalseTransitions()) != null;
                        }

                        @Override
                        public Color getTokenColor() {
                            return nodeStg.m1.getTokenColor();
                        }
                    };
                }

                return null;
            }
        };
    }

    private Transition getExcitedTransitionOfNode(Node node) {
        List<VisualSignalTransition> ts = null;
        if (node != null) {
            if (node instanceof VisualLogic) {
                ts = converter.getLogicStg((VisualLogic) node).getAllTransitions();
            } else if (node instanceof VisualRegister) {
                ts = converter.getRegisterStg((VisualRegister) node).getAllTransitions();
            } else if (node instanceof VisualCounterflowLogic) {
                ts = converter.getCounterflowLogicStg((VisualCounterflowLogic) node).getAllTransitions();
            } else if (node instanceof VisualCounterflowRegister) {
                ts = converter.getCounterflowRegisterStg((VisualCounterflowRegister) node).getAllTransitions();
            } else if (node instanceof VisualControlRegister) {
                ts = converter.getControlRegisterStg((VisualControlRegister) node).getAllTransitions();
            } else if (node instanceof VisualPushRegister) {
                ts = converter.getPushRegisterStg((VisualPushRegister) node).getAllTransitions();
            } else if (node instanceof VisualPopRegister) {
                ts = converter.getPopRegisterStg((VisualPopRegister) node).getAllTransitions();
            }
        }
        return getExcitedTransitionOfCollection(ts);
    }

    private Transition getExcitedTransitionOfCollection(List<VisualSignalTransition> ts) {
        if (ts != null) {
            for (VisualSignalTransition t: ts) {
                if (t == null) continue;
                Transition transition = t.getReferencedComponent();
                if (isEnabledNode(transition)) {
                    return transition;
                }
            }
        }
        return null;
    }

}
