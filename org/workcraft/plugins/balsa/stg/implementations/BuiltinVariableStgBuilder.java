package org.workcraft.plugins.balsa.stg.implementations;

import org.workcraft.plugins.balsa.stg.generated.*;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

import static org.workcraft.plugins.balsa.stg.ArrayPortUtils.*;
import static org.workcraft.plugins.balsa.stg.StgBuilderUtils.*;

public final class BuiltinVariableStgBuilder extends BuiltinVariableStgBuilderBase {

	@Override
	public void buildStg(BuiltinVariable component,
			BuiltinVariableHandshakes h, StrictPetriBuilder b) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}
}
