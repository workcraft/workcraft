package org.workcraft.plugins.balsa.stg.implementations;

import java.util.Map;

import org.workcraft.plugins.balsa.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.components.DynamicComponent;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface;
import org.workcraft.plugins.balsa.stg.generated.*;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class FalseVariableStgBuilder extends FalseVariableStgBuilderBase {

	@Override
	public void buildStg(FalseVariable component, FalseVariableStgInterface h,
			StrictPetriBuilder b) {
		b.connect(h.write.go(), h.signal.go());
		b.connect(h.signal.done(), h.write.done());
		for(int i=0;i<component.readPortCount;i++)
			b.connect(h.read.get(i).go(), h.read.get(i).done());
	}

	@Override
	public void buildEnvironment(DynamicComponent component, Map<String, StgInterface> handshakes, StrictPetriBuilder b) {

		StgPlace dataValid = b.buildPlace(0);
		FalseVariable c = new FalseVariable(component.parameters());
		FalseVariableStgInterfaceEnv h = new FalseVariableStgInterfaceEnv(c, handshakes);
		b.connect(h.signal.go(), dataValid);
		for(int i=0;i<c.readPortCount;i++)
		{
			b.connect(dataValid, h.read.get(i).go());
			b.connect(h.read.get(i).done(), dataValid);
		}
		b.connect(dataValid, h.signal.done());
	}

	@Override
	public HandshakeComponentLayout getLayout(FalseVariable properties, final FalseVariableHandshakes hs) {
		return new HandshakeComponentLayout() {

			@Override
			public Handshake getTop() {
				return null;
			}

			@Override
			public Handshake getBottom() {
				return hs.signal;
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
