package org.workcraft.plugins.balsa.stg.implementations;

import org.workcraft.plugins.balsa.stg.generated.*;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

import static org.workcraft.plugins.balsa.stg.ArrayPortUtils.*;
import static org.workcraft.plugins.balsa.stg.StgBuilderUtils.*;

public final class CallDemuxPushStgBuilder extends CallDemuxPushStgBuilderBase {

	@Override
	public void buildStg(CallDemuxPush component, CallDemuxPushHandshakes h,
			StrictPetriBuilder b) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}
}
