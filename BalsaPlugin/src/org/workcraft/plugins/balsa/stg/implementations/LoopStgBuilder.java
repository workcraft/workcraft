package org.workcraft.plugins.balsa.stg.implementations;

import org.workcraft.plugins.balsa.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.stg.generated.LoopStgBuilderBase;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class LoopStgBuilder extends LoopStgBuilderBase {

	@Override
	public void buildStg(Loop component, LoopStgInterface h, StrictPetriBuilder b) {
		StgPlace finish = b.buildPlace(0);
		StgPlace active = b.buildPlace(0);
		b.connect(h.activate.go(), active);
		b.connect(active, h.activateOut.go());
		b.connect(h.activateOut.done(), active);

		b.connect(finish, h.activate.done());//never finish
	}

	@Override
	public HandshakeComponentLayout getLayout(Loop properties, final LoopHandshakes hs) {

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
						{hs.activateOut}
				};
			}
		};
	}
}
