package org.workcraft.plugins.balsa.handshakestgbuilder;

import org.workcraft.plugins.balsa.stgbuilder.StgTransition;
import org.workcraft.plugins.balsa.stgbuilder.TransitionOutput;

public interface PassivePullStg extends StgHandshake
{
	public TransitionOutput getActivate();
	public StgTransition getDataReady();
	public TransitionOutput getDataRelease();
}
