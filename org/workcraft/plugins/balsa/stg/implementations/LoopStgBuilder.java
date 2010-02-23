package org.workcraft.plugins.balsa.stg.implementations;

import org.workcraft.plugins.balsa.stg.generated.LoopStgBuilderBase;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class LoopStgBuilder extends LoopStgBuilderBase {

	@Override
	public void buildStg(Loop component, LoopHandshakes h, StrictPetriBuilder b) {
		StgPlace finish = b.buildPlace(0);
		StgPlace active = b.buildPlace(0);
		b.connect(h.activate.go(), active);
		b.connect(active, h.activateOut.go());
		b.connect(h.activateOut.done(), active);

		b.connect(finish, h.activate.done());//never finish
	}
}
