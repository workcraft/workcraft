package org.workcraft.plugins.xmas.tools;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.gui.tools.Decorator;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.plugins.builtin.settings.SimulationDecorationSettings;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.tools.StgSimulationTool;
import org.workcraft.plugins.xmas.VisualXmas;
import org.workcraft.plugins.xmas.components.*;
import org.workcraft.plugins.xmas.stg.*;
import org.workcraft.utils.ColorUtils;
import org.workcraft.utils.Hierarchy;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashSet;

public class XmasSimulationTool extends StgSimulationTool {

    private static final Color COLOR_IRDY = Color.RED;
    private static final Color COLOR_TRDY = Color.BLUE;
    private static final Color COLOR_BOTH_READY = Color.MAGENTA;
    private static final Color COLOR_NONE_READY = Color.BLACK;
    private static final Color COLOR_CONTACT_IRDY = COLOR_IRDY;
    private static final Color COLOR_CONTACT_TRDY = COLOR_TRDY;
    private static final Color COLOR_CONTACT_NOT_READY = Color.WHITE;
    private XmasToStgConverter converter;
    private HashSet<VisualSignalTransition> skipTransitions = null;

    @Override
    public void activated(GraphEditor editor) {
        super.activated(editor);
        setStatePaneVisibility(false);
    }

    @Override
    public void deactivated(GraphEditor editor) {
        super.deactivated(editor);
        this.converter = null;
    }

    @Override
    public void generateUnderlyingModel(VisualModel model) {
        converter = new XmasToStgConverter((VisualXmas) model);
        skipTransitions = getSkipTransitions((VisualXmas) model);
        setUnderlyingModel(converter.getStgModel());
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        String msg = "Click on a highlighted element to activate it.";
        if (getExcitedTransition(converter.getClockStg().fallList) != null) {
            msg += " Right-click for the next clock tick.";
        }
        return msg;
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent e) {
        Transition transition = null;
        if (e.getButton() == MouseEvent.BUTTON3) {
            transition = getExcitedTransition(converter.getClockStg().fallList);
        }
        if (e.getButton() == MouseEvent.BUTTON1) {
            Point2D rootspacePosition = e.getPosition();
            VisualModel model = e.getModel();
            Node deepestNode = HitMan.hitDeepest(rootspacePosition, model.getRoot(),
                    node -> node instanceof VisualTransformableNode);

            if (deepestNode instanceof VisualTransformableNode) {
                Point2D nodespacePosition = model.getNodeSpacePosition(rootspacePosition,
                        (VisualTransformableNode) deepestNode);

                transition = getClickedComponentTransition(deepestNode, nodespacePosition);
            }
        }
        if (transition != null) {
            executeTransition(e.getEditor(), transition);
            Transition t = null;
            while ((t = getExcitedTransition(skipTransitions)) != null) {
                executeTransition(e.getEditor(), t);
            }
        }
    }

    private Transition getClickedComponentTransition(Node node, Point2D nodespacePosition) {
        Transition result = null;
        if (node instanceof VisualXmasContact) {
            ContactStg contactStg = converter.getContactStg((VisualXmasContact) node);
            result = getExcitedTransition(contactStg.rdy.getAllTransitions());
        } else if (node instanceof VisualSourceComponent) {
            SourceStg sourceStg = converter.getSourceStg((VisualSourceComponent) node);
            result = getExcitedTransition(sourceStg.oracle.getAllTransitions());
        } else if (node instanceof VisualSinkComponent) {
            SinkStg sinkStg = converter.getSinkStg((VisualSinkComponent) node);
            result = getExcitedTransition(sinkStg.oracle.getAllTransitions());
        } else if (node instanceof VisualSwitchComponent) {
            SwitchStg switchStg = converter.getSwitchStg((VisualSwitchComponent) node);
            result = getExcitedTransition(switchStg.oracle.getAllTransitions());
        } else if (node instanceof VisualQueueComponent) {
            VisualQueueComponent queue = (VisualQueueComponent) node;
            QueueStg queueStg = converter.getQueueStg(queue);
            int capacity = queue.getReferencedComponent().getCapacity();
            int idx = (int) Math.floor(0.5 * capacity + nodespacePosition.getX() * queue.SLOT_WIDTH);
            if (idx >= capacity) idx = capacity - 1;
            if (idx < 0) idx = 0;
            SlotStg slot = queueStg.slotList.get(idx);
            double headThreshold = 0.5 * queue.SLOT_HEIGHT - queue.HEAD_SIZE;
            double tailThreshold = 0.5 * queue.SLOT_HEIGHT - queue.TAIL_SIZE;
            if (nodespacePosition.getY() < -headThreshold) {
                result = getExcitedTransition(slot.hd.rdy.getAllTransitions());
            } else if (nodespacePosition.getY() > tailThreshold) {
                result = getExcitedTransition(slot.tl.rdy.getAllTransitions());
            } else {
                result = getExcitedTransition(slot.mem.getAllTransitions());
            }
        }
        return result;
    }

    @Override
    public Decorator getDecorator(final GraphEditor editor) {
        return new Decorator() {
            @Override
            public Decoration getDecoration(Node node) {
                if (node instanceof VisualXmasContact) {
                    return getContactDecoration((VisualXmasContact) node);
                }
                if (node instanceof VisualXmasConnection) {
                    return getConnectionDecoration((VisualXmasConnection) node);
                }
                if (node instanceof VisualSourceComponent) {
                    return getSourceDecoration((VisualSourceComponent) node);
                }
                if (node instanceof VisualSinkComponent) {
                    return getSinkDecoration((VisualSinkComponent) node);
                }
                if (node instanceof VisualSwitchComponent) {
                    return getSwitchDecoration((VisualSwitchComponent) node);
                }
                if (node instanceof VisualQueueComponent) {
                    return getQueueComponent((VisualQueueComponent) node);
                }
                return null;
            }
        };
    }

    private Decoration getContactDecoration(VisualXmasContact contact) {
        final Node traceCurrentNode = getTraceCurrentNode();
        final ContactStg contactStg = converter.getContactStg(contact);
        final boolean isExcited = getExcitedTransition(contactStg.rdy.getAllTransitions()) != null;
        final boolean isSuggested = isExcited && converter.isRelated(contact, traceCurrentNode);
        final boolean isReady = contactStg.rdy.zero.getReferencedComponent().getTokens() == 0;
        final boolean isInput = contact.isInput();

        return new Decoration() {
            @Override
            public Color getColorisation() {
                return isExcited ? SimulationDecorationSettings.getExcitedComponentColor() : null;
            }
            @Override
            public Color getBackground() {
                Color  colorisation = isSuggested ? SimulationDecorationSettings.getExcitedComponentColor() : null;
                Color contactColor = isReady ? (isInput ? COLOR_CONTACT_TRDY : COLOR_CONTACT_IRDY) : COLOR_CONTACT_NOT_READY;
                return ColorUtils.colorise(contactColor, colorisation);
            }
        };
    }

    private Decoration getConnectionDecoration(final VisualXmasConnection connection) {
        final VisualXmasContact firstContact = (VisualXmasContact) connection.getFirst();
        final VisualXmasContact secondContact = (VisualXmasContact) connection.getSecond();
        final ContactStg firstStg = converter.getContactStg(firstContact);
        final ContactStg secondStg = converter.getContactStg(secondContact);
        final boolean firstReady = firstStg.rdy.zero.getReferencedComponent().getTokens() == 0;
        final boolean secondReady = secondStg.rdy.zero.getReferencedComponent().getTokens() == 0;

        return new Decoration() {
            @Override
            public Color getColorisation() {
                if (firstReady && secondReady) {
                    return COLOR_BOTH_READY;
                }
                if (firstReady) {
                    return COLOR_IRDY;
                }
                if (secondReady) {
                    return COLOR_TRDY;
                }
                return COLOR_NONE_READY;
            }
            @Override
            public Color getBackground() {
                return null;
            }
        };
    }

    private Decoration getSourceDecoration(VisualSourceComponent sourceComponent) {
        final Node traceCurrentNode = getTraceCurrentNode();
        final SourceStg sourceStg = converter.getSourceStg(sourceComponent);
        final boolean isExcited = getExcitedTransition(sourceStg.oracle.getAllTransitions()) != null;
        final boolean isSuggested = isExcited && converter.isRelated(sourceComponent, traceCurrentNode);
        final boolean isActive = sourceStg.oracle.one.getReferencedComponent().getTokens() != 0;

        return new StateDecoration() {
            @Override
            public boolean getState() {
                return isActive;
            }
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

    private Decoration getSinkDecoration(VisualSinkComponent sinkComponent) {
        final Node traceCurrentNode = getTraceCurrentNode();
        final SinkStg sinkStg = converter.getSinkStg(sinkComponent);
        final boolean isExcited = getExcitedTransition(sinkStg.oracle.getAllTransitions()) != null;
        final boolean isSuggested = isExcited && converter.isRelated(sinkComponent, traceCurrentNode);
        final boolean isActive = sinkStg.oracle.one.getReferencedComponent().getTokens() != 0;

        return new StateDecoration() {
            @Override
            public boolean getState() {
                return isActive;
            }
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

    private Decoration getSwitchDecoration(VisualSwitchComponent switchComponent) {
        final Node traceCurrentNode = getTraceCurrentNode();
        final SwitchStg switchStg = converter.getSwitchStg(switchComponent);
        final boolean isExcited = getExcitedTransition(switchStg.oracle.getAllTransitions()) != null;
        final boolean isSuggested = isExcited && converter.isRelated(switchComponent, traceCurrentNode);
        final boolean isActive = switchStg.oracle.one.getReferencedComponent().getTokens() != 0;

        return new StateDecoration() {
            @Override
            public boolean getState() {
                return isActive;
            }
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

    private Decoration getQueueComponent(final VisualQueueComponent queue) {
        final QueueStg queueStg = converter.getQueueStg(queue);

        return new QueueDecoration() {
            @Override
            public SlotState getSlotState(int i) {
                SlotState result = null;
                int capacity = queue.getReferencedComponent().getCapacity();
                if ((i >= 0) && (i < capacity)) {
                    SlotStg slot = queueStg.slotList.get(i);
                    boolean isFull = slot.mem.one.getReferencedComponent().getTokens() != 0;
                    boolean isHead = slot.hd.rdy.one.getReferencedComponent().getTokens() != 0;
                    boolean isTail = slot.tl.rdy.one.getReferencedComponent().getTokens() != 0;
                    boolean isMemExcited = getExcitedTransition(slot.mem.getAllTransitions()) != null;
                    boolean isHeadExcited = getExcitedTransition(slot.hd.rdy.getAllTransitions()) != null;
                    boolean isTailExcited = getExcitedTransition(slot.tl.rdy.getAllTransitions()) != null;
                    result = new SlotState(isFull, isHead, isTail, isMemExcited, isHeadExcited, isTailExcited);
                }
                return result;
            }
            @Override
            public Color getColorisation() {
                return null;
            }
            @Override
            public Color getBackground() {
                return null;
            }
        };
    }

    private Transition getExcitedTransition(Collection<VisualSignalTransition> ts) {
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

    private HashSet<VisualSignalTransition> getSkipTransitions(VisualXmas xmas) {
        HashSet<VisualSignalTransition> result = new HashSet<>();
        result.addAll(converter.getClockStg().riseList);
        for (VisualSourceComponent component : Hierarchy.getDescendantsOfType(xmas.getRoot(), VisualSourceComponent.class)) {
            SourceStg sourceStg = converter.getSourceStg(component);
            if (sourceStg != null) {
                result.addAll(sourceStg.o.dn.getAllTransitions());
            }
        }
        for (VisualSinkComponent component : Hierarchy.getDescendantsOfType(xmas.getRoot(), VisualSinkComponent.class)) {
            SinkStg sinkStg = converter.getSinkStg(component);
            if (sinkStg != null) {
                result.addAll(sinkStg.i.dn.getAllTransitions());
            }
        }
        for (VisualFunctionComponent component : Hierarchy.getDescendantsOfType(xmas.getRoot(), VisualFunctionComponent.class)) {
            FunctionStg funcStg = converter.getFunctionStg(component);
            if (funcStg != null) {
                result.addAll(funcStg.i.dn.getAllTransitions());
                result.addAll(funcStg.o.dn.getAllTransitions());
            }
        }
        for (VisualForkComponent component : Hierarchy.getDescendantsOfType(xmas.getRoot(), VisualForkComponent.class)) {
            ForkStg forkStg = converter.getForkStg(component);
            if (forkStg != null) {
                result.addAll(forkStg.i.dn.getAllTransitions());
                result.addAll(forkStg.a.dn.getAllTransitions());
                result.addAll(forkStg.b.dn.getAllTransitions());
            }
        }
        for (VisualJoinComponent component : Hierarchy.getDescendantsOfType(xmas.getRoot(), VisualJoinComponent.class)) {
            JoinStg joinStg = converter.getJoinStg(component);
            if (joinStg != null) {
                result.addAll(joinStg.a.dn.getAllTransitions());
                result.addAll(joinStg.b.dn.getAllTransitions());
                result.addAll(joinStg.o.dn.getAllTransitions());
            }
        }
        for (VisualSwitchComponent component : Hierarchy.getDescendantsOfType(xmas.getRoot(), VisualSwitchComponent.class)) {
            SwitchStg switchStg = converter.getSwitchStg(component);
            if (switchStg != null) {
                result.addAll(switchStg.i.dn.getAllTransitions());
                result.addAll(switchStg.a.dn.getAllTransitions());
                result.addAll(switchStg.b.dn.getAllTransitions());
            }
        }
        for (VisualMergeComponent component : Hierarchy.getDescendantsOfType(xmas.getRoot(), VisualMergeComponent.class)) {
            MergeStg mergeStg = converter.getMergeStg(component);
            if (mergeStg != null) {
                result.addAll(mergeStg.a.dn.getAllTransitions());
                result.addAll(mergeStg.b.dn.getAllTransitions());
                result.addAll(mergeStg.o.dn.getAllTransitions());
            }
        }
        for (VisualQueueComponent component : Hierarchy.getDescendantsOfType(xmas.getRoot(), VisualQueueComponent.class)) {
            QueueStg queueStg = converter.getQueueStg(component);
            if (queueStg != null) {
                result.addAll(queueStg.i.dn.getAllTransitions());
                result.addAll(queueStg.o.dn.getAllTransitions());
                for (SlotStg slot: queueStg.slotList) {
                    result.addAll(slot.hd.rdy.getAllTransitions());
                    result.addAll(slot.hd.dn.getAllTransitions());
                    result.addAll(slot.tl.rdy.getAllTransitions());
                    result.addAll(slot.tl.dn.getAllTransitions());
                }
            }
        }
        return result;
    }

}
