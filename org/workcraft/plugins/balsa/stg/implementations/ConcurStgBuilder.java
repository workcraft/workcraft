package org.workcraft.plugins.balsa.stg.implementations;

import static org.workcraft.plugins.balsa.stg.ProcessOperations.enclosure;
import static org.workcraft.plugins.balsa.stg.ProcessOperations.parallel;

import org.workcraft.plugins.balsa.stg.generated.ConcurStgBuilderBase;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class ConcurStgBuilder extends ConcurStgBuilderBase {

	@Override
	public void buildStg(Concur component, ConcurHandshakes h,
			StrictPetriBuilder b) {
		enclosure(b, h.activate, parallel(b, h.activateOut));
	}
}
