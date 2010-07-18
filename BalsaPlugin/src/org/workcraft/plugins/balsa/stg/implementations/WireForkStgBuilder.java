package org.workcraft.plugins.balsa.stg.implementations;

import static org.workcraft.plugins.balsa.stg.ActiveArrayPortUtils.go;
import static org.workcraft.plugins.balsa.stg.StgBuilderUtils.fork;

import org.workcraft.plugins.balsa.stg.generated.WireForkStgBuilderBase;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class WireForkStgBuilder extends WireForkStgBuilderBase {

	@Override
	public void buildStg(WireFork properties, WireForkHandshakes h, StrictPetriBuilder b) {
		fork(b, h.inp.go(), go(h.out));
		StgPlace never = b.buildPlace(0);
		b.connect(never, h.inp.done());
	}
}
