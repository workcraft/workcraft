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
import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.fst.Signal;
import org.workcraft.plugins.fst.SignalEvent;
import org.workcraft.plugins.fst.VisualFst;
import org.workcraft.plugins.fst.VisualSignalEvent;

public 	class FstToFsmConverter extends DefaultModelConverter<VisualFst, VisualFsm>  {

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
		if ( (srcConnection instanceof VisualSignalEvent) && (dstConnection instanceof VisualEvent) ) {
			Fst fst = (Fst)getSrcModel().getMathModel();
			SignalEvent srcSignalEvent = (SignalEvent)srcConnection.getReferencedConnection();
			Signal srcSignal = srcSignalEvent.getSignal();
			String name = fst.getName(srcSignal);
			if (srcSignal.hasDirection()) {
				name += srcSignalEvent.getDirection();
				name = name.replace("+", "_PLUS").replace("-", "_MINUS").replace("~", "_TOGGLE");
			}
			Fsm fsm = (Fsm)getDstModel().getMathModel();
			Event dstEvent = (Event)dstConnection.getReferencedConnection();
			Symbol symbol = fsm.getOrCreateSymbol(name);
			dstEvent.setSymbol(symbol);
		}
		return dstConnection;
	}

}
