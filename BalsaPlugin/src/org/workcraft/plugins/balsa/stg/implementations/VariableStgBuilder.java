package org.workcraft.plugins.balsa.stg.implementations;

import org.workcraft.plugins.balsa.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg;
import org.workcraft.plugins.balsa.stg.generated.VariableStgBuilderBase;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class VariableStgBuilder extends VariableStgBuilderBase {

	@Override
	public void buildStg(Variable component, VariableStgInterface h,
			StrictPetriBuilder b) {
		for(PassivePullStg read : h.read)
			b.connect(read.go(), read.done());
		b.connect(h.write.go(), h.write.done());
	}

	@Override
	public HandshakeComponentLayout getLayout(Variable properties, final VariableHandshakes hs) {

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
						{hs.write}
				};
			}

			@Override
			public Handshake[][] getRight() {
				return new Handshake[][]{
						hs.read.toArray(new Handshake[0])
				};
			}
		};
	}
}
