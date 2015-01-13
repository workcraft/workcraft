package org.workcraft.plugins.fst.tools;

import java.util.HashMap;
import java.util.Map;

import org.workcraft.dom.Connection;
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
	public Map<Class<? extends MathNode>, Class<? extends MathNode>> getNodeClassMap() {
		Map<Class<? extends MathNode>, Class<? extends MathNode>> result = super.getNodeClassMap();
		result.put(State.class, State.class);
//		result.put(Signal.class, Symbol.class);
		return result;
	}

	@Override
	public Map<Class<? extends Connection>, Class<? extends Connection>> getConnectionClassMap() {
		Map<Class<? extends Connection>, Class<? extends Connection>> result = new HashMap<>();
		result.put(SignalEvent.class, Event.class);
		return result;
	}

	@Override
	public void afterConnectionConversion(VisualConnection srcConnection, VisualConnection dstConnection) {
		if ( (srcConnection instanceof VisualSignalEvent) && (dstConnection instanceof VisualEvent) ) {
			VisualSignalEvent srcSignalEvent = (VisualSignalEvent)srcConnection;
			VisualEvent dstEvent = (VisualEvent)dstConnection;
			Fst fst = (Fst)getSrcModel().getMathModel();
			Fsm fsm = (Fsm)getDstModel().getMathModel();
			SignalEvent signalEvent = srcSignalEvent.getReferencedSignalEvent();
			Signal signal = signalEvent.getSignal();
			String name = fst.getName(signal);
			if (signal.hasDirection()) {
				name += signalEvent.getDirection().toString().replace("+", "_PLUS").replace("-", "_MINUS").replace("~", "_TOGGLE");
			}
			Symbol symbol = fsm.createSymbol(name);
			dstEvent.getReferencedEvent().setSymbol(symbol);
		}
	}

}
