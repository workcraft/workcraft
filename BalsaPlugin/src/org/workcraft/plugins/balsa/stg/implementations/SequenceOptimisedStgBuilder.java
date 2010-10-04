package org.workcraft.plugins.balsa.stg.implementations;

import static org.workcraft.plugins.balsa.stg.ProcessOperations.enclosure;
import static org.workcraft.plugins.balsa.stg.ProcessOperations.sequence;

import org.workcraft.plugins.balsa.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.stg.generated.SequenceStgBuilderBase;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class SequenceOptimisedStgBuilder extends SequenceStgBuilderBase {

	@Override
	public void buildStg(Sequence properties, final SequenceStgInterface handshakes, StrictPetriBuilder b) {
		enclosure(b, handshakes.activate, sequence(b, handshakes.activateOut));
	}

	@Override
	public HandshakeComponentLayout getLayout(Sequence properties, final SequenceHandshakes hs) {

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
