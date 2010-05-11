package org.workcraft.plugins.stg;

import org.workcraft.plugins.petri.Transition;

public interface StgTransition
{
	public DummyTransition asDummy();
	public SignalTransition asSignal();
	public Transition getTransition();
}
