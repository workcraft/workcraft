package org.workcraft.plugins.balsa.stg.implementations;

import org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg;
import org.workcraft.plugins.balsa.stg.generated.VariableStgBuilderBase;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class VariableStgBuilder extends VariableStgBuilderBase {

	@Override
	public void buildStg(Variable component, VariableHandshakes h,
			StrictPetriBuilder b) {
		for(PassivePullStg read : h.read)
			b.connect(read.go(), read.done());
		b.connect(h.write.go(), h.write.done());
	}
}
