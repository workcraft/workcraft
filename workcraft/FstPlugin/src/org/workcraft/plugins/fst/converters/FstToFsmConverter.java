package org.workcraft.plugins.fst.converters;

import org.workcraft.dom.converters.DefaultModelConverter;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.fsm.*;
import org.workcraft.plugins.fst.*;

import java.util.Map;

public class FstToFsmConverter extends DefaultModelConverter<VisualFst, VisualFsm> {

    public FstToFsmConverter(VisualFst srcModel, VisualFsm dstModel) {
        super(srcModel, dstModel);
    }

    @Override
    public Map<Class<? extends MathNode>, Class<? extends MathNode>> getComponentClassMap() {
        Map<Class<? extends MathNode>, Class<? extends MathNode>> result = super.getComponentClassMap();
        result.put(State.class, State.class);
        return result;
    }

    @Override
    public VisualConnection convertConnection(VisualConnection srcConnection) {
        VisualConnection dstConnection = super.convertConnection(srcConnection);
        if ((srcConnection instanceof VisualSignalEvent) && (dstConnection instanceof VisualEvent)) {
            Fst fst = getSrcModel().getMathModel();
            SignalEvent srcSignalEvent = (SignalEvent) srcConnection.getReferencedConnection();
            Signal srcSignal = srcSignalEvent.getSymbol();
            String directionSuffix = "";
            if (srcSignal.hasDirection()) {
                switch (srcSignalEvent.getDirection()) {
                case PLUS:
                    directionSuffix = "_PLUS";
                    break;
                case MINUS:
                    directionSuffix = "_MINUS";
                    break;
                case TOGGLE:
                    directionSuffix = "_TOGGLE";
                    break;
                }
            }
            String name = fst.getName(srcSignal) + directionSuffix;
            Fsm fsm = getDstModel().getMathModel();
            Event dstEvent = (Event) dstConnection.getReferencedConnection();
            Symbol symbol = fsm.getOrCreateSymbol(name);
            dstEvent.setSymbol(symbol);
        }
        return dstConnection;
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
