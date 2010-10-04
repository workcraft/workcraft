package org.workcraft.plugins.balsa.stg.implementations;

import static org.workcraft.plugins.balsa.stg.ActiveArrayPortUtils.done;
import static org.workcraft.plugins.balsa.stg.ActiveArrayPortUtils.go;
import static org.workcraft.plugins.balsa.stg.StgBuilderUtils.fork;
import static org.workcraft.plugins.balsa.stg.StgBuilderUtils.join;

import org.workcraft.plugins.balsa.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.stg.generated.ForkStgBuilderBase;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class ForkStgBuilder extends ForkStgBuilderBase {

	@Override
	public void buildStg(Fork component, ForkStgInterface h, StrictPetriBuilder b) {
		//if(true)throw new RuntimeException("not sure if it is correct");
		fork(b, h.inp.go(), go(h.out));
		join(b, done(h.out), h.inp.done());
	}

	@Override
	public HandshakeComponentLayout getLayout(Fork properties, final ForkHandshakes hs) {

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
