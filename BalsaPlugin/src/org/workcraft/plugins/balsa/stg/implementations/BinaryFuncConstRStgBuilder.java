package org.workcraft.plugins.balsa.stg.implementations;

import org.workcraft.plugins.balsa.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.stg.generated.BinaryFuncConstRStgBuilderBase;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class BinaryFuncConstRStgBuilder extends
		BinaryFuncConstRStgBuilderBase {

	@Override
	public void buildStg(BinaryFuncConstR component,
			BinaryFuncConstRStgInterface h, StrictPetriBuilder b) {
		b.connect(h.out.go(), h.inpA.go());
		b.connect(h.inpA.done(), h.out.done());
		b.connect(h.out.dataRelease(), h.inpA.dataRelease());
	}

	@Override
	public HandshakeComponentLayout getLayout(BinaryFuncConstR properties,
			final BinaryFuncConstRHandshakes hs) {
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
				return new Handshake[][] { { hs.inpA } };
			}

			@Override
			public Handshake[][] getRight() {
				return new Handshake[][] { {hs.out} };
			}
		};
	}
}
