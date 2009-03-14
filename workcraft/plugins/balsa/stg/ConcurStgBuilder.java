package org.workcraft.plugins.balsa.stg;

import java.util.Map;

import org.workcraft.plugins.balsa.components.Concur;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSyncStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSyncStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgHandshake;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;

public class ConcurStgBuilder extends ComponentStgBuilder<Concur> {

	@Override
	public void buildStg(Concur component, Map<String, StgHandshake> handshakes, StgBuilder builder) {
		PassiveSyncStg activate = (PassiveSyncStg)handshakes.get("activate");

		for(int i=0;i<component.getOutputCount();i++)
		{
			ActiveSyncStg out = (ActiveSyncStg)handshakes.get("activateOut"+i);
			builder.addConnection(activate.getActivationNotificator(), out.getActivator());
			builder.addConnection(out.getDeactivationNotificator(), activate.getDeactivator());
		}
	}

}
