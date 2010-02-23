package org.workcraft.plugins.balsa.stg.implementations;

import org.workcraft.plugins.balsa.stg.generated.BinaryFuncConstRStgBuilderBase;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class BinaryFuncConstRStgBuilder extends BinaryFuncConstRStgBuilderBase {

	@Override
	public void buildStg(BinaryFuncConstR component,
			BinaryFuncConstRHandshakes h, StrictPetriBuilder b) {
		b.connect(h.out.go(), h.inpA.go());
		b.connect(h.inpA.done(), h.out.done());
		b.connect(h.out.dataRelease(), h.inpA.dataRelease());
	}
}
