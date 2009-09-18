package org.workcraft.plugins.balsa.handshakestgbuilder;

import org.workcraft.plugins.balsa.stgbuilder.StgTransition;
import org.workcraft.plugins.balsa.stgbuilder.TransitionOutput;

public interface ActivePullStg extends StgHandshake
{
	public StgTransition getActivate();
	public TransitionOutput getDataReady();
	public StgTransition getDataRelease();
}
