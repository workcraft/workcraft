package org.workcraft.plugins.balsa.stg.implementations;

import org.workcraft.plugins.balsa.stg.generated.*;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class ArbiterStgBuilder extends ArbiterStgBuilderBase {

	@Override
	public void buildStg(Arbiter component, ArbiterHandshakes h,
			StrictPetriBuilder b) {
		StgPlace place = b.buildPlace(1);
		b.connect(h.inpA.go(), h.outA.go());
		b.connect(h.inpB.go(), h.outB.go());
		b.connect(place, h.outA.go());
		b.connect(place, h.outB.go());
		b.connect(h.outA.done(), place);
		b.connect(h.outB.done(), place);
		b.connect(h.outA.done(), h.inpA.done());
		b.connect(h.outB.done(), h.inpB.done());
	}
}
