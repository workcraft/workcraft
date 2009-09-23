package org.workcraft.plugins.balsa.stg;

import java.util.Map;

import org.workcraft.plugins.balsa.components.Loop;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveProcess;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveProcess;
import org.workcraft.plugins.balsa.handshakestgbuilder.Process;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.OutputPlace;

public class LoopStgBuilder extends ComponentStgBuilder<org.workcraft.plugins.balsa.components.Loop> {

	@Override
	public void buildStg(Loop component, Map<String, Process> handshakes, StgBuilder builder) {
		PassiveProcess activate = (PassiveProcess)handshakes.get("activate");
		ActiveProcess activateOut = (ActiveProcess)handshakes.get("activateOut");

		OutputPlace activated = builder.buildPlace();

		OutputPlace never = builder.buildPlace();

		builder.connect(activate.go(), activated);
		builder.connect(activated, activateOut.go());
		builder.connect(activateOut.done(), activated);

		builder.connect(never, activate.done());
	}

}
