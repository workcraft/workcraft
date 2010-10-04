package org.workcraft.plugins.balsa.stg.implementations;

import org.workcraft.plugins.balsa.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.stg.generated.*;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class PassiveSyncEagerFalseVariableStgBuilder extends PassiveSyncEagerFalseVariableStgBuilderBase {

	@Override
	public void buildStg(PassiveSyncEagerFalseVariable component,
			PassiveSyncEagerFalseVariableStgInterface h, StrictPetriBuilder b) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	@Override
	public HandshakeComponentLayout getLayout(PassiveSyncEagerFalseVariable properties, final PassiveSyncEagerFalseVariableHandshakes hs) {

		return new HandshakeComponentLayout() {

			@Override
			public Handshake getTop() {
				return hs.trigger;
			}

			@Override
			public Handshake getBottom() {
				return hs.signal;
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
						{}
				};
			}
		};
	}
}
