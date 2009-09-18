package org.workcraft.plugins.balsa.handshakestgbuilder;

import org.workcraft.plugins.balsa.stgbuilder.StgTransition;
import org.workcraft.plugins.balsa.stgbuilder.TransitionOutput;


public interface ActiveSyncStg extends StgHandshake
{
	public StgTransition getActivate();
	public TransitionOutput getDeactivate();
}
