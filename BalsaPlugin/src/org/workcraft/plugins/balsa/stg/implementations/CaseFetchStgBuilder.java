package org.workcraft.plugins.balsa.stg.implementations;

import org.workcraft.plugins.balsa.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.stg.generated.*;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class CaseFetchStgBuilder extends CaseFetchStgBuilderBase {

	@Override
	public void buildStg(CaseFetch component, CaseFetchStgInterface h, StrictPetriBuilder b) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	@Override
	public HandshakeComponentLayout getLayout(CaseFetch properties, final CaseFetchHandshakes hs) {
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
						{hs.index}, {hs.out}
				};
			}

			@Override
			public Handshake[][] getRight() {
				return new Handshake[][]{
						hs.inp.toArray(new Handshake[0])
				};
			}
		};
	}
}
