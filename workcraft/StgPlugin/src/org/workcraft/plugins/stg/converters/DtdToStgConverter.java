package org.workcraft.plugins.stg.converters;

import org.workcraft.dom.Container;
import org.workcraft.dom.converters.DefaultModelConverter;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.ConnectionUtils;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.dtd.*;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.*;
import org.workcraft.utils.LogUtils;

import java.awt.geom.Point2D;
import java.util.Collection;
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
        if (dstStylable instanceof VisualConnection dstConnection) {
            ConnectionUtils.setDefaultStyle(dstConnection);
        }
        if ((srcStylable instanceof VisualTransitionEvent srcTransition) && (dstStylable instanceof VisualSignalTransition dstTransition)) {
            Signal.Type type = convertSignalType(srcTransition.getVisualSignal().getType());
            dstTransition.getReferencedComponent().setSignalType(type);
        }
    }

    private Signal.Type convertSignalType(org.workcraft.plugins.dtd.Signal.Type type) {
        return switch (type) {
            case INPUT -> Signal.Type.INPUT;
            case OUTPUT -> Signal.Type.OUTPUT;
            case INTERNAL -> Signal.Type.INTERNAL;
        };
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
        return switch (direction) {
            case FALL -> SignalTransition.Direction.MINUS;
            case RISE -> SignalTransition.Direction.PLUS;
            default -> SignalTransition.Direction.TOGGLE;
        };
    }

    @Override
    public void postprocessing() {
        createEntryStructure();
        createExitStructure();
    }

    private void createEntryStructure() {
        Collection<VisualEntryEvent> entryEvents = getSrcModel().getVisualSignalEntries(null);
        for (VisualEntryEvent entryEvent : entryEvents) {
            VisualStgPlace entryPlace = getDstModel().createVisualPlace(null);
            entryPlace.setRootSpacePosition(scalePosition(entryEvent.getRootSpacePosition()));
            entryPlace.getReferencedComponent().setTokens(1);
            Set<VisualTransitionEvent> firstTransitionEvents = getSrcModel().getPostset(entryEvent, VisualTransitionEvent.class);
            for (VisualTransitionEvent transitionEvent : firstTransitionEvents) {
                VisualNode dstNode = getSrcToDstNode(transitionEvent);
                try {
                    getDstModel().connect(entryPlace, dstNode);
                } catch (InvalidConnectionException e) {
                    LogUtils.logWarning(e.getMessage());
                }
            }
        }
    }

    private void createExitStructure() {
        Collection<VisualExitEvent> exitEvents = getSrcModel().getVisualSignalExits(null);
        for (VisualExitEvent exitEvent : exitEvents) {
            VisualStgPlace exitPlace = getDstModel().createVisualPlace(null);
            exitPlace.setRootSpacePosition(scalePosition(exitEvent.getRootSpacePosition()));
            Set<VisualTransitionEvent> lastTransitionEvents = getSrcModel().getPreset(exitEvent, VisualTransitionEvent.class);
            for (VisualTransitionEvent lastTransitionEvent : lastTransitionEvents) {
                VisualNode dstNode = getSrcToDstNode(lastTransitionEvent);
                try {
                    getDstModel().connect(dstNode, exitPlace);
                } catch (InvalidConnectionException e) {
                    LogUtils.logWarning(e.getMessage());
                }
            }
        }
    }

    @Override
    public Point2D getScale() {
        return new Point2D.Double(2.0, 2.0);
    }

}
