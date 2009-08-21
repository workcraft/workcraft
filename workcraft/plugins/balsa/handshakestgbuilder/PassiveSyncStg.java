package org.workcraft.plugins.balsa.handshakestgbuilder;

import org.workcraft.plugins.balsa.stgbuilder.StgTransition;

public interface PassiveSyncStg extends StgHandshake
{
	public StgTransition getActivationNotificator();
	public StgTransition getDeactivator();
}
