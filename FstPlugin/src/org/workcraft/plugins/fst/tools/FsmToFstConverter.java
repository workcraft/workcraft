package org.workcraft.plugins.fst.tools;

import java.util.Map;

import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.graph.tools.DefaultModelConverter;
import org.workcraft.plugins.fsm.Event;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.State;
import org.workcraft.plugins.fsm.Symbol;
import org.workcraft.plugins.fsm.VisualEvent;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.fsm.VisualState;
import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.fst.Signal;
import org.workcraft.plugins.fst.Signal.Type;
import org.workcraft.plugins.fst.SignalEvent;
import org.workcraft.plugins.fst.VisualFst;
import org.workcraft.plugins.fst.VisualSignalEvent;

public class FsmToFstConverter extends DefaultModelConverter<VisualFsm, VisualFst> {

    public FsmToFstConverter(VisualFsm srcModel, VisualFst dstModel) {
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
        if ((srcConnection instanceof VisualEvent) && (dstConnection instanceof VisualSignalEvent)) {
            Event srcEvent = (Event) srcConnection.getReferencedConnection();
            Symbol srcSymbol = srcEvent.getSymbol();
            Fsm fsm = (Fsm) getSrcModel().getMathModel();
            String name = fsm.getName(srcSymbol);
            Fst fst = (Fst) getDstModel().getMathModel();
            Signal dstSignal = fst.getOrCreateSignal(name, Type.DUMMY);
            SignalEvent dstSignalEvent = (SignalEvent) dstConnection.getReferencedConnection();
            dstSignalEvent.setSymbol(dstSignal);
        }
        return dstConnection;
    }

    @Override
    public void postprocessing() {
        VisualFsm fsm = getSrcModel();
        for (VisualState srcState: fsm.getVisualStates()) {
            if (srcState.getReferencedState().isInitial()) {
                VisualState dstState = (VisualState) getSrcToDstNode(srcState);
                if (dstState != null) {
                    dstState.getReferencedState().setInitial(true);
                }
            }
        }
    }

}
