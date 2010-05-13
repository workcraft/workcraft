package org.workcraft.plugins.balsa.stg.implementations;

import static org.workcraft.plugins.balsa.stg.ArrayPortUtils.done;
import static org.workcraft.plugins.balsa.stg.ArrayPortUtils.go;
import static org.workcraft.plugins.balsa.stg.StgBuilderUtils.fork;
import static org.workcraft.plugins.balsa.stg.StgBuilderUtils.join;

import org.workcraft.plugins.balsa.stg.generated.SynchStgBuilderBase;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class SynchStgBuilder extends SynchStgBuilderBase {

	@Override
	public void buildStg(Synch component, SynchHandshakes h,
			StrictPetriBuilder b) {
		join(b, go(h.inp), h.out.go());
		fork(b, h.out.done(), done(h.inp));
	}
}
