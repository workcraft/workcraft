package org.workcraft.plugins.balsa.stg.implementations;

import static org.workcraft.plugins.balsa.stg.ProcessOperations.enclosure;
import static org.workcraft.plugins.balsa.stg.ProcessOperations.parallel;

import org.workcraft.plugins.balsa.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.stg.generated.ConcurStgBuilderBase;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class ConcurStgBuilder extends ConcurStgBuilderBase {

	@Override
	public void buildStg(Concur component, ConcurStgInterface h, StrictPetriBuilder b) {
		enclosure(b, h.activate, parallel(b, h.activateOut));
	}

	@Override
	public HandshakeComponentLayout getLayout(Concur properties, final ConcurHandshakes hs) {
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
						{hs.activate}
				};
			}

			@Override
			public Handshake[][] getRight() {
				return new Handshake[][]{
						hs.activateOut.toArray(new Handshake[0])
				};
			}
		};
	}
}
