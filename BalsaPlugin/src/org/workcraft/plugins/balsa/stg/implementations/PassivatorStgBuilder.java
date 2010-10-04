package org.workcraft.plugins.balsa.stg.implementations;

import org.workcraft.plugins.balsa.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync;
import org.workcraft.plugins.balsa.stg.generated.*;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class PassivatorStgBuilder extends PassivatorStgBuilderBase {

	@Override
	public void buildStg(Passivator component, PassivatorStgInterface h, StrictPetriBuilder b) {

		for(PassiveSync hs : h.inp)
			b.connect(hs.go(), hs.done());
	}

	@Override
	public HandshakeComponentLayout getLayout(Passivator properties, final PassivatorHandshakes hs) {

		return new HandshakeComponentLayout() {

			@Override
			public Handshake getTop() {
				return null;
			}

			@Override
			public Handshake getBottom() {
				return null;
			}

			@Override
			public Handshake[][] getLeft() {
				return new Handshake[][]{
						hs.inp.toArray(new Handshake[0])
				};
			}

			@Override
			public Handshake[][] getRight() {
				return new Handshake[][]{

				};
			}
		};
	}
}
