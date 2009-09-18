package org.workcraft.plugins.balsa.stg;

import java.util.Map;

import org.workcraft.plugins.balsa.components.SequenceOptimised;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSyncStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSyncStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgHandshake;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.TransitionOutput;

public class SequenceOptimisedStgBuilder extends
		ComponentStgBuilder<SequenceOptimised> {

	@Override
	public void buildStg(SequenceOptimised component,
			Map<String, StgHandshake> handshakes, StgBuilder builder) {

		PassiveSyncStg activate = (PassiveSyncStg)handshakes.get("activate");

		TransitionOutput lastHandshake = activate.getActivate();

		for(int i=0;i<component.getOutputCount();i++)
		{
			ActiveSyncStg out = (ActiveSyncStg)handshakes.get("activateOut"+i);
			builder.addConnection(lastHandshake, out.getActivate());
			lastHandshake = out.getDeactivate();
		}

		builder.addConnection(lastHandshake, activate.getDeactivate());
	}

}
