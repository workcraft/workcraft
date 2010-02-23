package org.workcraft.plugins.balsa.stg.implementations;

import org.workcraft.plugins.balsa.stg.generated.*;
import org.workcraft.plugins.balsa.stgbuilder.InputOutputEvent;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

import static org.workcraft.plugins.balsa.stg.ArrayPortUtils.*;
import static org.workcraft.plugins.balsa.stg.StgBuilderUtils.*;

public final class SynchStgBuilder extends SynchStgBuilderBase {

	@Override
	public void buildStg(Synch component, SynchHandshakes h,
			StrictPetriBuilder b) {
		InputOutputEvent allGo = b.buildTransition();
		join(b, go(h.inp), h.out.go());
		fork(b, h.out.done(), done(h.inp));
	}
}
