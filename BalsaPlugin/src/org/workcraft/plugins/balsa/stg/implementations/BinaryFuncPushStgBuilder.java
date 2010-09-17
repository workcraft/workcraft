package org.workcraft.plugins.balsa.stg.implementations;

import org.workcraft.plugins.balsa.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.stg.generated.*;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class BinaryFuncPushStgBuilder extends BinaryFuncPushStgBuilderBase {

	@Override
	public void buildStg(BinaryFuncPush component, BinaryFuncPushStgInterface h, StrictPetriBuilder b) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	@Override
	public HandshakeComponentLayout getLayout(BinaryFuncPush properties,
			final BinaryFuncPushHandshakes hs) {
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
