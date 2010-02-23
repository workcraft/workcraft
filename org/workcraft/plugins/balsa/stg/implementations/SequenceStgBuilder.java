package org.workcraft.plugins.balsa.stg.implementations;

import org.workcraft.plugins.balsa.stg.generated.*;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

import static org.workcraft.plugins.balsa.stg.ArrayPortUtils.*;
import static org.workcraft.plugins.balsa.stg.StgBuilderUtils.*;

public final class SequenceStgBuilder extends SequenceStgBuilderBase {

	@Override
	public void buildStg(Sequence component, SequenceHandshakes h,
			StrictPetriBuilder b) {
		b.connect(h.activate.go(), h.activateOut.get(0).go());
		for(int i=0;i<component.outputCount-1;i++)
		{
			b.connect(
					h.activateOut.get(i).done(),
					h.activateOut.get(i+1).go()
					);
		}
		b.connect(
				h.activateOut.get(component.outputCount-1).done(),
				h.activate.done()
				);
	}
}
