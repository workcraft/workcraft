package org.workcraft.plugins.balsa.handshakestgbuilder;

import org.workcraft.plugins.balsa.stgbuilder.Event;

public interface PassivePullStg extends PassiveProcess
{
	public Event dataRelease();
}
