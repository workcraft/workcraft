package org.workcraft.plugins.balsa.handshakestgbuilder;

import org.workcraft.plugins.balsa.stgbuilder.OutputEvent;

public interface ActivePullStg extends ActiveProcess
{
	public OutputEvent dataRelease();
}
