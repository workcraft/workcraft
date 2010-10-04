package org.workcraft.plugins.balsa.stg.implementations;

import static org.workcraft.plugins.balsa.stg.ArrayPortUtils.done;
import static org.workcraft.plugins.balsa.stg.ArrayPortUtils.go;
import static org.workcraft.plugins.balsa.stg.StgBuilderUtils.fork;
import static org.workcraft.plugins.balsa.stg.StgBuilderUtils.join;

import org.workcraft.plugins.balsa.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.stg.generated.SynchStgBuilderBase;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class SynchStgBuilder extends SynchStgBuilderBase {

	@Override
	public void buildStg(Synch component, SynchStgInterface h,
			StrictPetriBuilder b) {
		join(b, go(h.inp), h.out.go());
		fork(b, h.out.done(), done(h.inp));
	}

	@Override
	public HandshakeComponentLayout getLayout(Synch properties, final SynchHandshakes hs) {

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
						hs.inp.toArray(new Handshake[0])
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
