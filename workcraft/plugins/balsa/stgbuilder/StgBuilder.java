package org.workcraft.plugins.balsa.stgbuilder;

public interface StgBuilder
{
	StgPlace buildPlace();
	StgPlace buildPlace(int tokenCount);
	StgTransition buildTransition();
	StgSignal buildSignal(SignalId id, boolean isOutput);
	void addConnection(StgPlace place, StgTransition transition);
	void addConnection(StgTransition transition, StgPlace place);
	void addConnection(TransitionOutput transition, StgPlace place);
	void addReadArc(ReadablePlace place, StgTransition transition);
}
