package org.workcraft.plugins.balsa.stg.implementations;

import org.workcraft.plugins.balsa.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.stg.generated.FetchStgBuilderBase;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class FetchStgBuilder extends FetchStgBuilderBase {

	@Override
	public void buildStg(Fetch component, FetchStgInterface h,
			StrictPetriBuilder b) {
		b.connect(h.activate.go(), h.inp.go());
		b.connect(h.inp.done(), h.out.go());
		b.connect(h.out.done(), h.inp.dataRelease());
		b.connect(h.out.done(), h.activate.done());
	}

	@Override
	public HandshakeComponentLayout getLayout(Fetch properties, final FetchHandshakes hs) {

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
						{hs.inp}
				};
			}

			@Override
			public Handshake[][] getRight() {
				return new Handshake[][]{
						{hs.out}
				};
			}
		};
	}
}
