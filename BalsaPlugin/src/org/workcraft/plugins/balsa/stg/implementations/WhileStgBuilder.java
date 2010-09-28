package org.workcraft.plugins.balsa.stg.implementations;

import org.workcraft.plugins.balsa.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.stg.generated.*;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class WhileStgBuilder extends WhileStgBuilderBase {

	@Override
	public void buildStg(While component, WhileStgInterface h, StrictPetriBuilder b) {
		StgPlace askGuard = b.buildPlace(0);
		b.connect(h.activate.go(), askGuard);
		b.connect(askGuard, h.guard.go());
		StgPlace answerTrue = b.buildPlace(0);
		StgPlace answerFalse = b.buildPlace(0);
		b.connect(h.guard.result().get(0), answerFalse);
		b.connect(h.guard.result().get(1), answerTrue);
		b.connect(answerTrue, h.activateOut.go());
		b.connect(h.activateOut.done(), askGuard);
		b.connect(answerFalse, h.activate.done());
	}

	@Override
	public HandshakeComponentLayout getLayout(While properties, final WhileHandshakes hs) {

		return new HandshakeComponentLayout() {

			@Override
			public Handshake getTop() {
				return hs.activate;
			}

			@Override
			public Handshake getBottom() {
				return null;
			}

			@Override
			public Handshake[][] getLeft() {
				return new Handshake[][]{
						{hs.guard}
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
