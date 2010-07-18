package org.workcraft.plugins.balsa.stg.implementations;

import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync;
import org.workcraft.plugins.balsa.stg.generated.*;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class PassivatorStgBuilder extends PassivatorStgBuilderBase {

	@Override
	public void buildStg(Passivator component, PassivatorHandshakes h, StrictPetriBuilder b) {

		for(PassiveSync hs : h.inp)
			b.connect(hs.go(), hs.done());
	}
}
