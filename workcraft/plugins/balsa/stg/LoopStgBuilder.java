package org.workcraft.plugins.balsa.stg;

import java.util.Map;

import org.workcraft.plugins.balsa.components.Loop;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSyncStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSyncStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgHandshake;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;

public class LoopStgBuilder extends ComponentStgBuilder<org.workcraft.plugins.balsa.components.Loop> {

	@Override
	public void buildStg(Loop component, Map<String, StgHandshake> handshakes, StgBuilder builder) {
		PassiveSyncStg activate = (PassiveSyncStg)handshakes.get("activate");
		ActiveSyncStg activateOut = (ActiveSyncStg)handshakes.get("activateOut");

		StgPlace activated = builder.buildPlace();

		StgPlace never = builder.buildPlace();

		builder.addConnection(activate.getActivationNotificator(), activated);
		builder.addConnection(activated, activateOut.getActivator());
		builder.addConnection(activateOut.getDeactivationNotificator(), activated);

		builder.addConnection(never, activate.getDeactivator());
	}

}
