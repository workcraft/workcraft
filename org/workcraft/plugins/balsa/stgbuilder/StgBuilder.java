package org.workcraft.plugins.balsa.stgbuilder;

public interface StgBuilder
{
	OutputPlace buildPlace();
	InputPlace buildInputPlace();
	OutputPlace buildPlace(int tokenCount);
	InputOutputEvent buildTransition();
	StgSignal buildSignal(SignalId id, boolean isOutput);
	void connect(OutputPlace place, OutputEvent transition);
	void connect(InputPlace place, InputEvent transition);
	void connect(Event transition, OutputPlace place);
	void connect(Event transition, InputPlace place);
	void addReadArc(OutputPlace place, OutputEvent transition);
	void addReadArc(InputPlace place, InputEvent transition);
	void connect(Event t1, OutputEvent t2);
	void connect(Event t1, InputOutputEvent t2);
	void connect(Event t1, InputEvent t2);
}
