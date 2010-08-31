package org.workcraft.plugins.balsa.stg.implementations;

import org.workcraft.plugins.balsa.stg.generated.DecisionWaitStgBuilderBase;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public final class DecisionWaitStgBuilder extends DecisionWaitStgBuilderBase {

	@Override
	public void buildStg(DecisionWait component, DecisionWaitHandshakes h, StrictPetriBuilder b) {
		for(int i=0;i<component.portCount;i++)
		{
			b.connect(h.activate.go(), h.out.get(i).go());
			b.connect(h.inp.get(i).go(), h.out.get(i).go());
			b.connect(h.out.get(i).done(), h.inp.get(i).done());
			b.connect(h.out.get(i).done(), h.activate.done());
		}
	}
}
