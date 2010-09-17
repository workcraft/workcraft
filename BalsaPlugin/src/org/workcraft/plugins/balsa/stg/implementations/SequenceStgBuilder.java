package org.workcraft.plugins.balsa.stg.implementations;

import org.workcraft.plugins.balsa.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.stg.generated.*;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class SequenceStgBuilder extends SequenceStgBuilderBase {

	@Override
	public void buildStg(Sequence component, SequenceStgInterface h,
			StrictPetriBuilder b) {
		b.connect(h.activate.go(), h.activateOut.get(0).go());
		for(int i=0;i<component.outputCount-1;i++)
		{
			b.connect(
					h.activateOut.get(i).done(),
					h.activateOut.get(i+1).go()
					);
		}
		b.connect(
				h.activateOut.get(component.outputCount-1).done(),
				h.activate.done()
				);
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
