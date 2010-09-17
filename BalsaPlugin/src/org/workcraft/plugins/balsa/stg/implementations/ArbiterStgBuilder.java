package org.workcraft.plugins.balsa.stg.implementations;

import org.workcraft.plugins.balsa.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.stg.generated.*;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class ArbiterStgBuilder extends ArbiterStgBuilderBase {

	@Override
	public void buildStg(Arbiter component, ArbiterStgInterface h, StrictPetriBuilder b) {
		StgPlace place = b.buildPlace(1);
		b.connect(h.inpA.go(), h.outA.go());
		b.connect(h.inpB.go(), h.outB.go());
		b.connect(place, h.outA.go());
		b.connect(place, h.outB.go());
		b.connect(h.outA.done(), place);
		b.connect(h.outB.done(), place);
		b.connect(h.outA.done(), h.inpA.done());
		b.connect(h.outB.done(), h.inpB.done());
	}

	@Override
	public HandshakeComponentLayout getLayout(Arbiter properties, final ArbiterHandshakes hs) {
		return new HandshakeComponentLayout()
		{
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
				return new Handshake[][]{{hs.inpA}, {hs.inpB}};
			}

			@Override
			public Handshake[][] getRight() {
				return new Handshake[][]{{hs.outA}, {hs.outB}};
			}
		};
	}
}
