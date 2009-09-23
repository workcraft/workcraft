package org.workcraft.plugins.balsa.stg;

import java.util.Map;

import org.workcraft.plugins.balsa.components.Concur;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveProcess;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveProcess;
import org.workcraft.plugins.balsa.handshakestgbuilder.Process;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;

public class ConcurStgBuilder extends ComponentStgBuilder<Concur> {

	@Override
	public void buildStg(Concur component, Map<String, Process> handshakes, StgBuilder builder) {
		PassiveProcess activate = (PassiveProcess)handshakes.get("activate");

		for(int i=0;i<component.getOutputCount();i++)
		{
			ActiveProcess out = (ActiveProcess)handshakes.get("activateOut"+i);
			builder.connect(activate.go(), out.go());
			builder.connect(out.done(), activate.done());
		}
	}

}
