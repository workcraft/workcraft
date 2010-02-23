package org.workcraft.plugins.balsa.stg.implementations;

import org.workcraft.plugins.balsa.stg.generated.*;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

import static org.workcraft.plugins.balsa.stg.ArrayPortUtils.*;
import static org.workcraft.plugins.balsa.stg.StgBuilderUtils.*;

public final class NullAdaptStgBuilder extends NullAdaptStgBuilderBase {

	@Override
	public void buildStg(NullAdapt component, NullAdaptHandshakes h,
			StrictPetriBuilder b) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}
}
