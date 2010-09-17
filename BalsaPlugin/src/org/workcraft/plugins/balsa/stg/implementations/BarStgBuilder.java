package org.workcraft.plugins.balsa.stg.implementations;

import org.workcraft.plugins.balsa.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.stg.generated.*;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class BarStgBuilder extends BarStgBuilderBase {

	@Override
	public void buildStg(Bar component, BarStgInterface h, StrictPetriBuilder b) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	@Override
	public HandshakeComponentLayout getLayout(Bar properties, final BarHandshakes hs) {
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
				return new Handshake[][]{hs.guardInput.toArray(new Handshake[0]), hs.activateOut.toArray(new Handshake[0])};
			}

			@Override
			public Handshake[][] getRight() {
				return new Handshake[][]{{hs.guard}, {hs.activate}};
			}
		};
	}
}
