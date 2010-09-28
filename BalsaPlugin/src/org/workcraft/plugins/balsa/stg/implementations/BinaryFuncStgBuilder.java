package org.workcraft.plugins.balsa.stg.implementations;

import org.workcraft.plugins.balsa.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.stg.generated.BinaryFuncStgBuilderBase;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class BinaryFuncStgBuilder extends BinaryFuncStgBuilderBase {

	@Override
	public void buildStg(BinaryFunc component, BinaryFuncStgInterface h,
			StrictPetriBuilder b) {
		b.connect(h.out.go(), h.inpA.go());
		b.connect(h.out.go(), h.inpB.go());
		b.connect(h.inpA.done(), h.out.done());
		b.connect(h.inpB.done(), h.out.done());
	}

	@Override
	public HandshakeComponentLayout getLayout(BinaryFunc properties, final BinaryFuncHandshakes hs) {
		return new HandshakeComponentLayout()
		{
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
				return new Handshake[][]{{hs.inpA}, {hs.inpB}};
			}

			@Override
			public Handshake[][] getRight() {
				return new Handshake[][]{{hs.out}};
			}
		};
	}
}
