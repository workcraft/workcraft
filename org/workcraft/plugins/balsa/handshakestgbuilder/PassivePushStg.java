package org.workcraft.plugins.balsa.handshakestgbuilder;

import org.workcraft.plugins.balsa.stgbuilder.OutputEvent;

public interface PassivePushStg extends PassiveProcess
{
	public OutputEvent dataRelease();
}
