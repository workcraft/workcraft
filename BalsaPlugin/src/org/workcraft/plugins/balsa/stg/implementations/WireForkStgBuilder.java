package org.workcraft.plugins.balsa.stg.implementations;

import static org.workcraft.plugins.balsa.stg.ActiveArrayPortUtils.go;
import static org.workcraft.plugins.balsa.stg.StgBuilderUtils.fork;

import org.workcraft.plugins.balsa.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.stg.generated.WireForkStgBuilderBase;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class WireForkStgBuilder extends WireForkStgBuilderBase {

	@Override
	public void buildStg(WireFork properties, final WireForkStgInterface h, StrictPetriBuilder b) {
		fork(b, h.inp.go(), go(h.out));
		StgPlace never = b.buildPlace(0);
		b.connect(never, h.inp.done());
	}

	@Override
	public HandshakeComponentLayout getLayout(WireFork properties, final WireForkHandshakes hs) {

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
						hs.out.toArray(new Handshake[0])
				};
			}
		};
	}
}
