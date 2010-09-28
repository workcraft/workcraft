package org.workcraft.plugins.balsa.stg.implementations;

import java.util.Map;

import org.workcraft.plugins.balsa.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.components.DynamicComponent;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface;
import org.workcraft.plugins.balsa.stg.generated.*;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class CallStgBuilder extends CallStgBuilderBase {

	@Override
	public void buildStg(Call component, CallStgInterface h, StrictPetriBuilder b) {
		StgPlace go = b.buildPlace(0);
		StgPlace done = b.buildPlace(0);
		for(PassiveSync in : h.inp)
		{
			b.connect(in.go(), go);
			b.connect(in.go(), in.done());
			b.connect(done, in.done());
		}
		b.connect(go, h.out.go());
		b.connect(h.out.done(), done);
	}
	@Override
	public void buildEnvironment(DynamicComponent component,
			Map<String, StgInterface> handshakes, StrictPetriBuilder builder) {
		Call properties = makeProperties(component.parameters());
		CallStgInterfaceEnv hs = new CallStgInterfaceEnv(properties, handshakes);
		StgPlace choice = builder.buildPlace(1);
		for(ActiveSync i : hs.inp)
		{
			builder.connect(choice, i.go());
			builder.connect(i.done(), choice);
		}
	}
	@Override
	public HandshakeComponentLayout getLayout(Call properties, final CallHandshakes hs) {
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
				return new Handshake[][]{hs.inp.toArray(new Handshake[0])};
			}

			@Override
			public Handshake[][] getRight() {
				return new Handshake[][]{{hs.out}};
			}
		};
	}
}
