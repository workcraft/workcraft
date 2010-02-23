package org.workcraft.plugins.balsa.stg.implementations;

import org.workcraft.plugins.balsa.stg.generated.*;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

import static org.workcraft.plugins.balsa.stg.ArrayPortUtils.*;
import static org.workcraft.plugins.balsa.stg.StgBuilderUtils.*;

public final class CallDemuxStgBuilder extends CallDemuxStgBuilderBase {

	@Override
	public void buildStg(CallDemux component, CallDemuxHandshakes h,
			StrictPetriBuilder b) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}
}
