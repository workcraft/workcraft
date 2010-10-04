package org.workcraft.plugins.balsa.stg.implementations;

import org.workcraft.plugins.balsa.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.stg.generated.*;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class CaseStgBuilder extends CaseStgBuilderBase {

	@Override
	public void buildStg(Case component, CaseStgInterface h, StrictPetriBuilder b) {
		b.connect(h.inp.go(), h.dp.go());
		StgPlace done = b.buildPlace(0);
		for(int i=0;i<h.activateOut.size();i++)
		{
			b.connect(h.dp.result().get(i), h.activateOut.get(i).go());
			b.connect(h.activateOut.get(i).done(), done);
		}
		b.connect(done, h.inp.done());
	}

	@Override
	public HandshakeComponentLayout getLayout(Case properties, final CaseHandshakes hs) {
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
						{hs.inp}
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
