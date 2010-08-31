package org.workcraft.plugins.balsa.stg.implementations;

import static org.workcraft.plugins.balsa.stg.ActiveArrayPortUtils.done;
import static org.workcraft.plugins.balsa.stg.ActiveArrayPortUtils.go;
import static org.workcraft.plugins.balsa.stg.StgBuilderUtils.fork;
import static org.workcraft.plugins.balsa.stg.StgBuilderUtils.join;

import org.workcraft.plugins.balsa.stg.generated.ForkStgBuilderBase;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class ForkStgBuilder extends ForkStgBuilderBase {

	@Override
	public void buildStg(Fork component, ForkHandshakes h, StrictPetriBuilder b) {
		if(true)throw new RuntimeException("not sure if it is correct");
		fork(b, h.inp.go(), go(h.out));
		join(b, done(h.out), h.inp.done());
	}
}
