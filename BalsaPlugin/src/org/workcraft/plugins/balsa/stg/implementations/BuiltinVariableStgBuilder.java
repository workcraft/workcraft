package org.workcraft.plugins.balsa.stg.implementations;

import org.workcraft.plugins.balsa.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.stg.generated.*;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class BuiltinVariableStgBuilder extends BuiltinVariableStgBuilderBase {

	@Override
	public void buildStg(BuiltinVariable component,
			BuiltinVariableStgInterface h, StrictPetriBuilder b) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	@Override
	public HandshakeComponentLayout getLayout(BuiltinVariable properties, final BuiltinVariableHandshakes hs) {
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
				return new Handshake[][]{{hs.write}};
			}

			@Override
			public Handshake[][] getRight() {
				return new Handshake[][]{hs.read.toArray(new Handshake[0])};
			}
		};
	}
}
