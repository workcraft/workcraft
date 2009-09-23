package org.workcraft.plugins.balsa.handshakestgbuilder;

import org.workcraft.plugins.balsa.stgbuilder.InputEvent;
import org.workcraft.plugins.balsa.stgbuilder.OutputEvent;

public interface PassiveProcess extends Process
{
	public InputEvent go();
	public OutputEvent done();
}
