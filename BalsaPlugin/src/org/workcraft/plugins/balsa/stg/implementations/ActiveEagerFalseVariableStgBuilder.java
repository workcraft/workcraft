package org.workcraft.plugins.balsa.stg.implementations;

import org.workcraft.plugins.balsa.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg;
import org.workcraft.plugins.balsa.stg.generated.ActiveEagerFalseVariableStgBuilderBase;
import org.workcraft.plugins.balsa.stgbuilder.InputOutputEvent;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class ActiveEagerFalseVariableStgBuilder extends ActiveEagerFalseVariableStgBuilderBase {

	@Override
	public void buildStg(ActiveEagerFalseVariable properties, ActiveEagerFalseVariableStgInterface h, StrictPetriBuilder b) {
		b.connect(h.trigger.go(),h.signal.go());
		b.connect(h.trigger.go(),h.write.go());
		b.connect(h.signal.done(), h.trigger.done());
		StgPlace haveData = b.buildPlace(0);
		b.connect(h.write.done(), haveData);
		for(int i=0;i<h.read.size();i++)
		{
			PassivePullStg read = h.read.get(i);
			InputOutputEvent readDone = b.buildTransition();
			b.connect(read.go(), readDone);
			b.connect(haveData, readDone);
			b.connect(readDone, haveData);
			b.connect(readDone, read.done());
		}
		b.connect(haveData, h.trigger.done());
	}
	//TODO: Environment

	@Override
	public HandshakeComponentLayout getLayout(
			ActiveEagerFalseVariable properties,
			final ActiveEagerFalseVariableHandshakes handshakes) {
		return new HandshakeComponentLayout()
		{

			@Override
			public Handshake getTop() {
				return handshakes.trigger;
			}

			@Override
			public Handshake getBottom() {
				return handshakes.signal;
			}

			@Override
			public Handshake[][] getLeft() {
				return new Handshake[][]{
					{handshakes.write}
				};
			}

			@Override
			public Handshake[][] getRight() {
				return new Handshake[][]{
						handshakes.read.toArray(new Handshake[]{})
					};
			}

		};
	}
}
