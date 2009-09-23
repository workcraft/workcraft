package org.workcraft.plugins.balsa.stgmodelstgbuilder;

import org.workcraft.plugins.balsa.stgbuilder.InputOutputEvent;
import org.workcraft.plugins.stg.SignalTransition;

public class StgModelStgTransition implements InputOutputEvent
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
