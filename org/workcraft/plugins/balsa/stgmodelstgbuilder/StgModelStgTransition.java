package org.workcraft.plugins.balsa.stgmodelstgbuilder;

import org.workcraft.plugins.balsa.stgbuilder.StgTransition;
import org.workcraft.plugins.stg.SignalTransition;

public class StgModelStgTransition implements StgTransition
{
	private final SignalTransition modelTransition;

	StgModelStgTransition(SignalTransition modelTransition)
	{
		this.modelTransition = modelTransition;
	}

	public SignalTransition getModelTransition() {
		return modelTransition;
	}
}
