package org.workcraft.plugins.stg;

import org.workcraft.plugins.petri.Transition;

public interface STGTransition
{
	public DummyTransition asDummy();
	public SignalTransition asSignal();
	public Transition getTransition();
}
