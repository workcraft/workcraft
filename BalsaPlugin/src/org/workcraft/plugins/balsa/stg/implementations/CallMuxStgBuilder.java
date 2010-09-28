package org.workcraft.plugins.balsa.stg.implementations;

import java.util.Map;

import org.workcraft.plugins.balsa.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.components.DynamicComponent;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSync;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSync;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface;
import org.workcraft.plugins.balsa.stg.generated.CallMuxStgBuilderBase;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class CallMuxStgBuilder extends CallMuxStgBuilderBase {
	@Override
	public void buildStg(CallMux component, CallMuxStgInterface h, StrictPetriBuilder b) {
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
		CallMux properties = makeProperties(component.parameters());
		CallMuxStgInterfaceEnv hs = new CallMuxStgInterfaceEnv(properties, handshakes);
		StgPlace choice = builder.buildPlace(1);
		for(ActiveSync i : hs.inp)
		{
			builder.connect(choice, i.go());
			builder.connect(i.done(), choice);
		}
	}
	@Override
	public HandshakeComponentLayout getLayout(CallMux properties, final CallMuxHandshakes hs) {
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
