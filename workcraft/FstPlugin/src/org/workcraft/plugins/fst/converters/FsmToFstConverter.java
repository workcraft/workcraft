package org.workcraft.plugins.fst.converters;

import org.workcraft.dom.converters.DefaultModelConverter;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.fsm.*;
import org.workcraft.plugins.fst.*;

import java.util.Map;

public class FsmToFstConverter extends DefaultModelConverter<VisualFsm, VisualFst> {

    public FsmToFstConverter(VisualFsm srcModel) {
        super(srcModel, new VisualFst(new Fst()));
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
        if ((srcConnection instanceof VisualEvent) && (dstConnection instanceof VisualSignalEvent)) {
            Event srcEvent = (Event) srcConnection.getReferencedConnection();
            Symbol srcSymbol = srcEvent.getSymbol();
            String name = (srcSymbol == null) ? Fsm.EPSILON_SERIALISATION : getSrcModel().getMathName(srcSymbol);
            Signal dstSignal = getDstModel().getMathModel().getOrCreateSignal(name, Signal.Type.DUMMY);
            SignalEvent dstSignalEvent = (SignalEvent) dstConnection.getReferencedConnection();
            dstSignalEvent.setSymbol(dstSignal);
        }
        return dstConnection;
    }

    @Override
    public void postprocessing() {
        VisualFsm fsm = getSrcModel();
        for (VisualState srcState: fsm.getVisualStates()) {
            if (srcState.getReferencedComponent().isInitial()) {
                VisualState dstState = (VisualState) getSrcToDstNode(srcState);
                if (dstState != null) {
                    dstState.getReferencedComponent().setInitial(true);
                }
            }
        }
    }

}
