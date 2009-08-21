package org.workcraft.plugins.balsa.handshakestgbuilder;

import org.workcraft.plugins.balsa.stgbuilder.StgTransition;


public interface ActiveSyncStg extends StgHandshake
{
	public StgTransition getActivator();
	public StgTransition getDeactivationNotificator();
}
