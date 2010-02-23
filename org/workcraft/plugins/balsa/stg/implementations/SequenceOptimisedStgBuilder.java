package org.workcraft.plugins.balsa.stg.implementations;

import static org.workcraft.plugins.balsa.stg.ProcessOperations.enclosure;
import static org.workcraft.plugins.balsa.stg.ProcessOperations.sequence;

import org.workcraft.plugins.balsa.stg.generated.SequenceOptimisedStgBuilderBase;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class SequenceOptimisedStgBuilder extends SequenceOptimisedStgBuilderBase {

	@Override
	public void buildStg(SequenceOptimised properties, SequenceOptimisedHandshakes handshakes, StrictPetriBuilder b) {
		enclosure(b, handshakes.activate, sequence(b, handshakes.activateOut));
	}
}
