package org.workcraft.plugins.balsa.stg.implementations;

import org.workcraft.plugins.balsa.stg.generated.BinaryFuncStgBuilderBase;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class BinaryFuncStgBuilder extends BinaryFuncStgBuilderBase {

	@Override
	public void buildStg(BinaryFunc component, BinaryFuncHandshakes h,
			StrictPetriBuilder b) {
		b.connect(h.out.go(), h.inpA.go());
		b.connect(h.out.go(), h.inpB.go());
		b.connect(h.inpA.done(), h.out.done());
		b.connect(h.inpB.done(), h.out.done());
	}
}
