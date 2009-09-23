package org.workcraft.plugins.balsa.stg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.workcraft.plugins.balsa.components.SequenceOptimised;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveProcess;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveProcess;
import org.workcraft.plugins.balsa.handshakestgbuilder.Process;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;

public class SequenceOptimisedStgBuilder extends
		ComponentStgBuilder<SequenceOptimised> {
	ProcessOperations o;
	@Override
	public void buildStg(SequenceOptimised component,
			Map<String, Process> handshakes, StgBuilder builder) {

		PassiveProcess activate = (PassiveProcess)handshakes.get("activate");
		Collection<ActiveProcess> arr = getHandshakeArray(handshakes, "activateOut", component.getOutputCount(), ActiveProcess.class);
		o.multiple(o.enclosure(activate, o.sequence(arr)));
	}
	private static <T extends Process> Collection<T> getHandshakeArray(Map<String, Process> handshakes,
			String arrayName, int arraySize, Class<T> handshakeType) {
		Collection<T> result = new ArrayList<T>(arraySize);

		for(int i=0;i<arraySize;i++)
			result.add(handshakeType.cast(handshakes.get("activateOut"+i)));

		return result;
	}

}
