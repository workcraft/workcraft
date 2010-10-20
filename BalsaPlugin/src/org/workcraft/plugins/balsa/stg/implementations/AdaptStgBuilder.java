package org.workcraft.plugins.balsa.stg.implementations;

import org.workcraft.plugins.balsa.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.stg.generated.*;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class AdaptStgBuilder extends AdaptStgBuilderBase {

	@Override
	public void buildStg(Adapt component, AdaptStgInterface h, StrictPetriBuilder b) {
		b.connect(h.out.go(), h.inp.go());
		b.connect(h.inp.done(), h.out.done());
		b.connect(h.out.dataRelease(), h.inp.dataRelease());
	}

	@Override
	public HandshakeComponentLayout getLayout(Adapt properties, final AdaptHandshakes hs) {
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
				return new Handshake[][]{{hs.inp}};
			}

			@Override
			public Handshake[][] getRight() {
				return new Handshake[][]{{hs.out}};
			}
		};
	}
}
