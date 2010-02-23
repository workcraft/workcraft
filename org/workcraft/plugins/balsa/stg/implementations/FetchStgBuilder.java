package org.workcraft.plugins.balsa.stg.implementations;

import org.workcraft.plugins.balsa.stg.generated.FetchStgBuilderBase;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class FetchStgBuilder extends FetchStgBuilderBase {

	@Override
	public void buildStg(Fetch component, FetchHandshakes h,
			StrictPetriBuilder b) {
		b.connect(h.activate.go(), h.inp.go());
		b.connect(h.inp.done(), h.out.go());
		b.connect(h.out.done(), h.inp.dataRelease());
		b.connect(h.out.done(), h.activate.done());
	}
}
