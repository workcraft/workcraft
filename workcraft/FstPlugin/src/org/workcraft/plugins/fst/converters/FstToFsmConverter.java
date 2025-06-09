package org.workcraft.plugins.fst.converters;

import org.workcraft.dom.converters.DefaultModelConverter;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.plugins.fsm.*;
import org.workcraft.plugins.fst.Signal;
import org.workcraft.plugins.fst.VisualFst;
import org.workcraft.plugins.fst.VisualSignalEvent;

import java.util.Map;

public class FstToFsmConverter extends DefaultModelConverter<VisualFst, VisualFsm> {

    public FstToFsmConverter(VisualFst srcModel) {
        super(srcModel, new VisualFsm(new Fsm()));
    }

    @Override
    public Map<Class<? extends MathNode>, Class<? extends MathNode>> getComponentClassMap() {
        Map<Class<? extends MathNode>, Class<? extends MathNode>> result = super.getComponentClassMap();
        result.put(State.class, State.class);
        return result;
    }

    @Override
    public void copyStyle(Stylable srcStylable, Stylable dstStylable) {
        if ((srcStylable instanceof VisualSignalEvent srcSignalEvent) && (dstStylable instanceof VisualEvent dstEvent)) {
            Signal srcSignal = srcSignalEvent.getReferencedConnection().getSymbol();
            String directionSuffix = "";
            if (srcSignal.hasDirection()) {
                directionSuffix = switch (srcSignalEvent.getReferencedConnection().getDirection()) {
                    case PLUS -> "_PLUS";
                    case MINUS -> "_MINUS";
                    case TOGGLE -> "_TOGGLE";
                };
            }
            String signalName = getSrcModel().getMathName(srcSignal) + directionSuffix;
            Symbol symbol = getDstModel().getMathModel().getOrCreateSymbol(signalName);
            dstEvent.getReferencedConnection().setSymbol(symbol);
        } else {
            super.copyStyle(srcStylable, dstStylable);
        }
    }

    @Override
    public void postprocessing() {
        VisualFst fst = getSrcModel();
        for (VisualState srcState: fst.getVisualStates()) {
            if (srcState.getReferencedComponent().isInitial()) {
                VisualState dstState = (VisualState) getSrcToDstNode(srcState);
                if (dstState != null) {
                    dstState.getReferencedComponent().setInitial(true);
                }
            }
        }
    }

}
