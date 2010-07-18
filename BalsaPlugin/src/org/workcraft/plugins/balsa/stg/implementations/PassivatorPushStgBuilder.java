package org.workcraft.plugins.balsa.stg.implementations;

import static org.workcraft.plugins.balsa.stg.ArrayPortUtils.dataRelease;
import static org.workcraft.plugins.balsa.stg.ArrayPortUtils.done;
import static org.workcraft.plugins.balsa.stg.ArrayPortUtils.go;
import static org.workcraft.plugins.balsa.stg.StgBuilderUtils.fork;
import static org.workcraft.plugins.balsa.stg.StgBuilderUtils.join;

import org.workcraft.plugins.balsa.stg.generated.PassivatorPushStgBuilderBase;
import org.workcraft.plugins.balsa.stgbuilder.InputOutputEvent;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class PassivatorPushStgBuilder extends PassivatorPushStgBuilderBase {

	@Override
	public void buildStg(PassivatorPush properties, PassivatorPushHandshakes h, StrictPetriBuilder b) {
		InputOutputEvent go = b.buildTransition();
		InputOutputEvent release = b.buildTransition();
		join(b, go(h.out), go);
		b.connect(h.inp.go(), go);
		fork(b, go, done(h.out));
		join(b, dataRelease(h.out), release);
		b.connect(release, h.inp.done());
	}
}
