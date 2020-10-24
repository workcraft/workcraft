package org.workcraft.plugins.stg.converters;

import org.workcraft.dom.Container;
import org.workcraft.dom.converters.DefaultModelConverter;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.MixUtils;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.ConnectionUtils;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.dtd.*;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.*;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DtdToStgConverter extends DefaultModelConverter<VisualDtd, VisualStg> {

    public DtdToStgConverter(VisualDtd srcModel, VisualStg dstModel) {
        super(srcModel, dstModel);
    }

    @Override
    public Map<Class<? extends MathNode>, Class<? extends MathNode>> getComponentClassMap() {
        Map<Class<? extends MathNode>, Class<? extends MathNode>> result = super.getComponentClassMap();
        result.put(TransitionEvent.class, SignalTransition.class);
        return result;
    }

    @Override
    public void shapeConnection(VisualConnection srcConnection, VisualConnection dstConnection) {
        if (!(srcConnection instanceof VisualLevelConnection)) {
            super.shapeConnection(srcConnection, dstConnection);
        }
    }

    @Override
    public void copyStyle(Stylable srcStylable, Stylable dstStylable) {
        super.copyStyle(srcStylable, dstStylable);
        if (dstStylable instanceof VisualConnection) {
            VisualConnection dstConnection = (VisualConnection) dstStylable;
            ConnectionUtils.setDefaultStyle(dstConnection);
        }
        if ((srcStylable instanceof VisualTransitionEvent) && (dstStylable instanceof VisualSignalTransition)) {
            VisualTransitionEvent srcTransition = (VisualTransitionEvent) srcStylable;
            VisualSignalTransition dstTransition = (VisualSignalTransition) dstStylable;
            Signal.Type type = convertSignalType(srcTransition.getVisualSignal().getType());
            dstTransition.getReferencedComponent().setSignalType(type);
        }
    }

    private Signal.Type convertSignalType(org.workcraft.plugins.dtd.Signal.Type type) {
        switch (type) {
        case INPUT:
            return Signal.Type.INPUT;
        case OUTPUT:
            return Signal.Type.OUTPUT;
        case INTERNAL:
            return Signal.Type.INTERNAL;
        default:
            return null;
        }
    }

    @Override
    public String convertNodeName(String srcName, Container container) {
        VisualTransitionEvent event = getSrcModel().getVisualComponentByMathReference(srcName, VisualTransitionEvent.class);
        if (event != null) {
            VisualSignal signal = event.getVisualSignal();
            String signalName = getSrcModel().getMathName(signal);
            SignalTransition.Direction direction = convertDirection(event.getDirection());
            return signalName + direction;
        }
        return srcName;
    }

    private SignalTransition.Direction convertDirection(TransitionEvent.Direction direction) {
        switch (direction) {
        case FALL:
            return SignalTransition.Direction.MINUS;
        case RISE:
            return SignalTransition.Direction.PLUS;
        default:
            return SignalTransition.Direction.TOGGLE;
        }
    }

    @Override
    public void postprocessing() {
        createEntryStructure();
        createExitStructure();
    }

    private void createEntryStructure() {
        Set<VisualTransitionEvent> firstTransitionEvents = new HashSet<>();
        Collection<VisualEntryEvent> entryEvents = getSrcModel().getVisualSignalEntries(null);
        for (VisualEntryEvent entryEvent : entryEvents) {
            firstTransitionEvents.addAll(getSrcModel().getPostset(entryEvent, VisualTransitionEvent.class));
        }
        if (!firstTransitionEvents.isEmpty()) {
            VisualStgPlace entryPlace = getDstModel().createVisualPlace(null);
            entryPlace.getReferencedComponent().setTokens(1);
            VisualDummyTransition entryDummy = getDstModel().createVisualDummyTransition(null);
            try {
                getDstModel().connect(entryPlace, entryDummy);
            } catch (InvalidConnectionException e) {
                e.printStackTrace();
            }

            Point2D entryPosition = MixUtils.middleRootspacePosition(entryEvents);
            double offset = 2.0 * DtdSettings.getTransitionSeparation();
            Point2D offsetEntryPosition = new Point2D.Double(entryPosition.getX() - offset, entryPosition.getY());
            entryPlace.setRootSpacePosition(scalePosition(offsetEntryPosition));
            entryDummy.setRootSpacePosition(scalePosition(entryPosition));

            for (VisualTransitionEvent transitionEvent : firstTransitionEvents) {
                VisualNode dstNode = getSrcToDstNode(transitionEvent);
                if (dstNode instanceof VisualSignalTransition) {
                    try {
                        getDstModel().connect(entryDummy, dstNode);
                    } catch (InvalidConnectionException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void createExitStructure() {
        Set<VisualTransitionEvent> lastTransitionEvents = new HashSet<>();
        Collection<VisualExitEvent> exitEvents = getSrcModel().getVisualSignalExits(null);
        for (VisualExitEvent exitEvent : exitEvents) {
            lastTransitionEvents.addAll(getSrcModel().getPreset(exitEvent, VisualTransitionEvent.class));
        }
        if (!lastTransitionEvents.isEmpty()) {
            VisualStgPlace exitPlace = getDstModel().createVisualPlace(null);
            VisualDummyTransition exitDummy = getDstModel().createVisualDummyTransition(null);
            try {
                getDstModel().connect(exitDummy, exitPlace);
            } catch (InvalidConnectionException e) {
                e.printStackTrace();
            }

            Point2D exitPosition = MixUtils.middleRootspacePosition(exitEvents);
            double offset = 2.0 * DtdSettings.getTransitionSeparation();
            Point2D offsetExitPosition = new Point2D.Double(exitPosition.getX() + offset, exitPosition.getY());
            exitDummy.setRootSpacePosition(scalePosition(exitPosition));
            exitPlace.setRootSpacePosition(scalePosition(offsetExitPosition));

            for (VisualTransitionEvent lastTransitionEvent : lastTransitionEvents) {
                VisualNode dstNode = getSrcToDstNode(lastTransitionEvent);
                if (dstNode instanceof VisualSignalTransition) {
                    try {
                        getDstModel().connect(dstNode, exitDummy);
                    } catch (InvalidConnectionException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public Point2D getScale() {
        return new Point2D.Double(2.0, 2.0);
    }

}
