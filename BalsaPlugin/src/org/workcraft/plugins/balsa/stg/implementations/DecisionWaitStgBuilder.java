package org.workcraft.plugins.balsa.stg.implementations;

import org.workcraft.plugins.balsa.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.stg.generated.DecisionWaitStgBuilderBase;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class DecisionWaitStgBuilder extends DecisionWaitStgBuilderBase {

	@Override
	public void buildStg(DecisionWait component, DecisionWaitStgInterface h, StrictPetriBuilder b) {
		for(int i=0;i<component.portCount;i++)
		{
			b.connect(h.activate.go(), h.out.get(i).go());
			b.connect(h.inp.get(i).go(), h.out.get(i).go());
			b.connect(h.out.get(i).done(), h.inp.get(i).done());
			b.connect(h.out.get(i).done(), h.activate.done());
		}
	}

	@Override
	public HandshakeComponentLayout getLayout(DecisionWait properties, final DecisionWaitHandshakes hs) {

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
						hs.inp.toArray(new Handshake[0])
				};
			}

			@Override
			public Handshake[][] getRight() {
				return new Handshake[][]{
						hs.out.toArray(new Handshake[0])
				};
			}
		};
	}
}
