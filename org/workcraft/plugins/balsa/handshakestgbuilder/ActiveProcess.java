package org.workcraft.plugins.balsa.handshakestgbuilder;

import org.workcraft.plugins.balsa.stgbuilder.InputEvent;
import org.workcraft.plugins.balsa.stgbuilder.OutputEvent;


public interface ActiveProcess extends Process
{
	public OutputEvent go();
	public InputEvent done();
}
