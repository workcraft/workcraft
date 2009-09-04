package org.workcraft.plugins.balsa.protocols;

import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSyncStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSyncStg;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;
import org.workcraft.plugins.balsa.stgbuilder.StgTransition;
import org.workcraft.plugins.balsa.stgbuilder.TransitionOutput;

class InputDataSignal
{
	StgPlace p0;
	StgPlace p1;
}

interface ActiveSyncWithRtz extends ActiveSyncStg
{
	public StgTransition getRtz();
}

interface PassiveSyncWithRtz extends PassiveSyncStg
{
	public TransitionOutput getRtz();
}
