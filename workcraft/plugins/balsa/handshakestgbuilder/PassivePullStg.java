package org.workcraft.plugins.balsa.handshakestgbuilder;

import org.workcraft.plugins.balsa.stgbuilder.TransitionOutput;

public interface PassivePullStg extends PassiveSyncStg
{
	TransitionOutput getDataRelease();
}
