package org.workcraft.plugins.balsa.stg.implementations;

import org.workcraft.plugins.balsa.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.stg.generated.*;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class BinaryFuncConstRPushStgBuilder extends
		BinaryFuncConstRPushStgBuilderBase {

	@Override
	public void buildStg(BinaryFuncConstRPush properties,
			BinaryFuncConstRPushStgInterface handshakes,
			StrictPetriBuilder builder) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	@Override
	public HandshakeComponentLayout getLayout(BinaryFuncConstRPush properties,
			final BinaryFuncConstRPushHandshakes hs) {
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
				return new Handshake[][] { {hs.out}};
			}
		};
	}
}
