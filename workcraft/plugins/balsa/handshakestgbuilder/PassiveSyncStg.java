package org.workcraft.plugins.balsa.handshakestgbuilder;

import org.workcraft.plugins.balsa.stgbuilder.StgTransition;
import org.workcraft.plugins.balsa.stgbuilder.TransitionOutput;

public interface PassiveSyncStg extends StgHandshake
{
	public TransitionOutput getActivationNotificator();
	public StgTransition getDeactivator();
}
