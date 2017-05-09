package org.workcraft.plugins.dfs.tools;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.dfs.BinaryRegister.Marking;
import org.workcraft.plugins.dfs.VisualAbstractRegister;
import org.workcraft.plugins.dfs.VisualBinaryRegister;
import org.workcraft.plugins.dfs.VisualControlRegister;
import org.workcraft.plugins.dfs.VisualCounterflowLogic;
import org.workcraft.plugins.dfs.VisualCounterflowRegister;
import org.workcraft.plugins.dfs.VisualDfs;
import org.workcraft.plugins.dfs.VisualLogic;
import org.workcraft.plugins.dfs.VisualPopRegister;
import org.workcraft.plugins.dfs.VisualPushRegister;
import org.workcraft.plugins.dfs.VisualRegister;
import org.workcraft.plugins.dfs.decorations.BinaryRegisterDecoration;
import org.workcraft.plugins.dfs.decorations.CounterflowLogicDecoration;
import org.workcraft.plugins.dfs.decorations.CounterflowRegisterDecoration;
import org.workcraft.plugins.dfs.decorations.LogicDecoration;
import org.workcraft.plugins.dfs.decorations.RegisterDecoration;
import org.workcraft.plugins.dfs.stg.BinaryRegisterStg;
import org.workcraft.plugins.dfs.stg.CounterflowLogicStg;
import org.workcraft.plugins.dfs.stg.CounterflowRegisterStg;
import org.workcraft.plugins.dfs.stg.DfsToStgConverter;
import org.workcraft.plugins.dfs.stg.LogicStg;
import org.workcraft.plugins.dfs.stg.RegisterStg;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.shared.CommonSimulationSettings;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.tools.StgSimulationTool;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;

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
            if (vp.getReferencedPlace() == place) {
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
        for (VisualLogic l : Hierarchy.getDescendantsOfType(dfs.getRoot(), VisualLogic.class)) {
            String refC = DfsToStgConverter.nameC + dfs.getNodeMathReference(l) + DfsToStgConverter.name1;
            Node nodeC = getUnderlyingStg().getNodeByReference(refC);
            if ((nodeC instanceof Place) && savedState.containsKey(nodeC)) {
                boolean computed = savedState.get(nodeC) > 0;
                l.getReferencedLogic().setComputed(computed);
            }
        }
        for (VisualRegister r : Hierarchy.getDescendantsOfType(dfs.getRoot(), VisualRegister.class)) {
            String refM = DfsToStgConverter.nameM + dfs.getNodeMathReference(r) + DfsToStgConverter.name1;
            Node nodeM = getUnderlyingStg().getNodeByReference(refM);
            if ((nodeM instanceof Place) && savedState.containsKey(nodeM)) {
                boolean marked = savedState.get(nodeM) > 0;
                r.getReferencedRegister().setMarked(marked);
                copyTokenColor(r, nodeM);
            }
        }
        for (VisualCounterflowLogic l : Hierarchy.getDescendantsOfType(dfs.getRoot(), VisualCounterflowLogic.class)) {
            String refFwC = DfsToStgConverter.nameFwC + dfs.getNodeMathReference(l) + DfsToStgConverter.name1;
            Node nodeFwC = getUnderlyingStg().getNodeByReference(refFwC);
            if ((nodeFwC instanceof Place) && savedState.containsKey(nodeFwC)) {
                boolean forwardComputed = savedState.get(nodeFwC) > 0;
                l.getReferencedCounterflowLogic().setForwardComputed(forwardComputed);
            }
            String refBwC = DfsToStgConverter.nameBwC + dfs.getNodeMathReference(l) + DfsToStgConverter.name1;
            Node nodeBwC = getUnderlyingStg().getNodeByReference(refBwC);
            if ((nodeBwC instanceof Place) && savedState.containsKey(nodeBwC)) {
                boolean backwardComputed = savedState.get(nodeBwC) > 0;
                l.getReferencedCounterflowLogic().setBackwardComputed(backwardComputed);
            }
        }
        for (VisualCounterflowRegister r : Hierarchy.getDescendantsOfType(dfs.getRoot(), VisualCounterflowRegister.class)) {
            String refOrM = DfsToStgConverter.nameOrM + dfs.getNodeMathReference(r) + DfsToStgConverter.name1;
            Node nodeOrM = getUnderlyingStg().getNodeByReference(refOrM);
            if ((nodeOrM instanceof Place) && savedState.containsKey(nodeOrM)) {
                boolean orMarked = savedState.get(nodeOrM) > 0;
                r.getReferencedCounterflowRegister().setOrMarked(orMarked);
                copyTokenColor(r, nodeOrM);
            }
            String refAndM = DfsToStgConverter.nameAndM + dfs.getNodeMathReference(r) + DfsToStgConverter.name1;
            Node nodeAndM = getUnderlyingStg().getNodeByReference(refAndM);
            if ((nodeAndM instanceof Place) && savedState.containsKey(nodeAndM)) {
                boolean andMarked = savedState.get(nodeAndM) > 0;
                r.getReferencedCounterflowRegister().setAndMarked(andMarked);
                copyTokenColor(r, nodeAndM);
            }
        }
        for (VisualBinaryRegister r : Hierarchy.getDescendantsOfType(dfs.getRoot(), VisualBinaryRegister.class)) {
            r.getReferencedBinaryRegister().setMarking(Marking.EMPTY);
            String refTrueM = DfsToStgConverter.nameTrueM + dfs.getNodeMathReference(r) + DfsToStgConverter.name1;
            Node nodeTrueM = getUnderlyingStg().getNodeByReference(refTrueM);
            if ((nodeTrueM instanceof Place) && savedState.containsKey(nodeTrueM)) {
                if (savedState.get(nodeTrueM) > 0) {
                    r.getReferencedBinaryRegister().setMarking(Marking.TRUE_TOKEN);
                }
                copyTokenColor(r, nodeTrueM);
            }
            String refFalseM = DfsToStgConverter.nameFalseM + dfs.getNodeMathReference(r) + DfsToStgConverter.name1;
            Node nodeFalseM = getUnderlyingStg().getNodeByReference(refFalseM);
            if ((nodeFalseM instanceof Place) && savedState.containsKey(nodeFalseM)) {
                if (savedState.get(nodeFalseM) > 0) {
                    r.getReferencedBinaryRegister().setMarking(Marking.FALSE_TOKEN);
                }
                copyTokenColor(r, nodeFalseM);
            }
        }
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            VisualModel model = e.getModel();
            Node node = HitMan.hitDeepest(e.getPosition(), model.getRoot(),
                    new Func<Node, Boolean>() {
                        @Override
                        public Boolean eval(Node node) {
                            return getExcitedTransitionOfNode(node) != null;
                        }
                    });

            Transition transition = null;
            if (node instanceof VisualTransformableNode) {
                AffineTransform rootToLocalTransform = TransformHelper.getTransform(e.getModel().getRoot(), node);
                Point2D posLocal = rootToLocalTransform.transform(e.getPosition(), null);
                Point2D posNode = ((VisualTransformableNode) node).getParentToLocalTransform().transform(posLocal, null);
                if (node instanceof VisualCounterflowLogic) {
                    CounterflowLogicStg lstg = converter.getCounterflowLogicStg((VisualCounterflowLogic) node);
                    if (posNode.getY() < 0) {
                        transition = getExcitedTransitionOfCollection(lstg.getForwardTransitions());
                    } else {
                        transition = getExcitedTransitionOfCollection(lstg.getBackwardTransitions());
                    }
                } else if (node instanceof VisualCounterflowRegister) {
                    CounterflowRegisterStg rstg = converter.getCounterflowRegisterStg((VisualCounterflowRegister) node);
                    if (posNode.getY() < 0) {
                        transition = getExcitedTransitionOfCollection(rstg.getOrTransitions());
                    } else {
                        transition = getExcitedTransitionOfCollection(rstg.getAndTransitions());
                    }
                } else if (node instanceof VisualControlRegister) {
                    BinaryRegisterStg rstg = converter.getControlRegisterStg((VisualControlRegister) node);
                    if (posNode.getY() < 0) {
                        transition = getExcitedTransitionOfCollection(rstg.getTrueTransitions());
                    } else {
                        transition = getExcitedTransitionOfCollection(rstg.getFalseTransitions());
                    }
                } else if (node instanceof VisualPushRegister) {
                    BinaryRegisterStg rstg = converter.getPushRegisterStg((VisualPushRegister) node);
                    if (posNode.getY() < 0) {
                        transition = getExcitedTransitionOfCollection(rstg.getTrueTransitions());
                    } else {
                        transition = getExcitedTransitionOfCollection(rstg.getFalseTransitions());
                    }
                } else if (node instanceof VisualPopRegister) {
                    BinaryRegisterStg rstg = converter.getPopRegisterStg((VisualPopRegister) node);
                    if (posNode.getY() < 0) {
                        transition = getExcitedTransitionOfCollection(rstg.getTrueTransitions());
                    } else {
                        transition = getExcitedTransitionOfCollection(rstg.getFalseTransitions());
                    }
                }
            }

            if (transition == null) {
                transition = getExcitedTransitionOfNode(node);
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
                    final LogicStg lstg = converter.getLogicStg((VisualLogic) node);

                    return new LogicDecoration() {
                        @Override
                        public Color getColorisation() {
                            return isExcited ? CommonSimulationSettings.getExcitedComponentColor() : null;
                        }

                        @Override
                        public Color getBackground() {
                            return isSuggested ? CommonSimulationSettings.getSuggestedComponentColor() : null;
                        }

                        @Override
                        public boolean isComputed() {
                            return lstg.c0.getReferencedPlace().getTokens() == 0;
                        }
                    };
                }

                if (node instanceof VisualRegister) {
                    final RegisterStg rstg = converter.getRegisterStg((VisualRegister) node);

                    return new RegisterDecoration() {
                        @Override
                        public Color getColorisation() {
                            return isExcited ? CommonSimulationSettings.getExcitedComponentColor() : null;
                        }

                        @Override
                        public Color getBackground() {
                            return isSuggested ? CommonSimulationSettings.getSuggestedComponentColor() : null;
                        }

                        @Override
                        public boolean isMarked() {
                            return rstg.m0.getReferencedPlace().getTokens() == 0;
                        }

                        @Override
                        public boolean isExcited() {
                            return getExcitedTransitionOfCollection(Arrays.asList(rstg.mR, rstg.mF)) != null;
                        }

                        @Override
                        public Color getTokenColor() {
                            return rstg.m1.getTokenColor();
                        }
                    };
                }

                if (node instanceof VisualCounterflowLogic) {
                    final CounterflowLogicStg lstg = converter.getCounterflowLogicStg((VisualCounterflowLogic) node);

                    return new CounterflowLogicDecoration() {
                        @Override
                        public Color getColorisation() {
                            return isExcited ? CommonSimulationSettings.getExcitedComponentColor() : null;
                        }

                        @Override
                        public Color getBackground() {
                            return isSuggested ? CommonSimulationSettings.getSuggestedComponentColor() : null;
                        }

                        @Override
                        public boolean isForwardComputed() {
                            return lstg.fwC0.getReferencedPlace().getTokens() == 0;
                        }

                        @Override
                        public boolean isBackwardComputed() {
                            return lstg.bwC0.getReferencedPlace().getTokens() == 0;
                        }

                        @Override
                        public boolean isForwardComputedExcited() {
                            return getExcitedTransitionOfCollection(lstg.getForwardTransitions()) != null;
                        }

                        @Override
                        public boolean isBackwardComputedExcited() {
                            return getExcitedTransitionOfCollection(lstg.getBackwardTransitions()) != null;
                        }
                    };
                }

                if (node instanceof VisualCounterflowRegister) {
                    final CounterflowRegisterStg rstg = converter.getCounterflowRegisterStg((VisualCounterflowRegister) node);

                    return new CounterflowRegisterDecoration() {
                        @Override
                        public Color getColorisation() {
                            return isExcited ? CommonSimulationSettings.getExcitedComponentColor() : null;
                        }

                        @Override
                        public Color getBackground() {
                            return isSuggested ? CommonSimulationSettings.getSuggestedComponentColor() : null;
                        }

                        @Override
                        public boolean isForwardExcited() {
                            return getExcitedTransitionOfCollection(rstg.getForwardTransitions()) != null;
                        }

                        @Override
                        public boolean isBackwardExcited() {
                            return getExcitedTransitionOfCollection(rstg.getBackwardTransitions()) != null;
                        }

                        @Override
                        public boolean isOrMarked() {
                            return rstg.orM0.getReferencedPlace().getTokens() == 0;
                        }

                        @Override
                        public boolean isAndMarked() {
                            return rstg.andM0.getReferencedPlace().getTokens() == 0;
                        }

                        @Override
                        public boolean isOrExcited() {
                            return getExcitedTransitionOfCollection(rstg.getOrTransitions()) != null;
                        }

                        @Override
                        public boolean isAndExcited() {
                            return getExcitedTransitionOfCollection(rstg.getAndTransitions()) != null;
                        }

                        @Override
                        public Color getTokenColor() {
                            return rstg.orM1.getTokenColor();
                        }
                    };
                }

                if (node instanceof VisualControlRegister || node instanceof VisualPushRegister || node instanceof VisualPopRegister) {
                    BinaryRegisterStg tmp = null;
                    if (node instanceof VisualControlRegister) {
                        tmp = converter.getControlRegisterStg((VisualControlRegister) node);
                    }
                    if (node instanceof VisualPushRegister) {
                        tmp = converter.getPushRegisterStg((VisualPushRegister) node);
                    }
                    if (node instanceof VisualPopRegister) {
                        tmp = converter.getPopRegisterStg((VisualPopRegister) node);
                    }
                    final BinaryRegisterStg rstg = tmp;

                    return new BinaryRegisterDecoration() {
                        @Override
                        public Color getColorisation() {
                            return isExcited ? CommonSimulationSettings.getExcitedComponentColor() : null;
                        }

                        @Override
                        public Color getBackground() {
                            return isSuggested ? CommonSimulationSettings.getSuggestedComponentColor() : null;
                        }

                        @Override
                        public boolean isTrueMarked() {
                            return rstg.tM0.getReferencedPlace().getTokens() == 0;
                        }

                        @Override
                        public boolean isTrueExcited() {
                            return getExcitedTransitionOfCollection(rstg.getTrueTransitions()) != null;
                        }

                        @Override
                        public boolean isFalseMarked() {
                            return rstg.fM0.getReferencedPlace().getTokens() == 0;
                        }

                        @Override
                        public boolean isFalseExcited() {
                            return getExcitedTransitionOfCollection(rstg.getFalseTransitions()) != null;
                        }

                        @Override
                        public Color getTokenColor() {
                            return rstg.m1.getTokenColor();
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
                Transition transition = t.getReferencedTransition();
                if (isEnabledNode(transition)) {
                    return transition;
                }
            }
        }
        return null;
    }

}
