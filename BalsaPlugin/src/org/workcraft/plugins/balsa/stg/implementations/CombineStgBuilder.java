package org.workcraft.plugins.balsa.stg.implementations;

import org.workcraft.plugins.balsa.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.stg.generated.*;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class CombineStgBuilder extends CombineStgBuilderBase {

	@Override
	public void buildStg(Combine properties, CombineStgInterface handshakes, StrictPetriBuilder builder) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	@Override
	public HandshakeComponentLayout getLayout(Combine properties, final CombineHandshakes hs) {
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
						{hs.LSInp}, {hs.MSInp}
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
