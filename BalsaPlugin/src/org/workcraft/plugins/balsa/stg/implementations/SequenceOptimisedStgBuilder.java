package org.workcraft.plugins.balsa.stg.implementations;

import static org.workcraft.plugins.balsa.stg.ProcessOperations.enclosure;
import static org.workcraft.plugins.balsa.stg.ProcessOperations.sequence;

import org.workcraft.plugins.balsa.stg.generated.SequenceStgBuilderBase;
import org.workcraft.plugins.balsa.stg.generated.SequenceStgBuilderBase.Sequence;
import org.workcraft.plugins.balsa.stg.generated.SequenceStgBuilderBase.SequenceHandshakes;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class SequenceOptimisedStgBuilder extends SequenceStgBuilderBase {

	@Override
	public void buildStg(Sequence properties, SequenceHandshakes handshakes, StrictPetriBuilder b) {
		enclosure(b, handshakes.activate, sequence(b, handshakes.activateOut));
	}
}
