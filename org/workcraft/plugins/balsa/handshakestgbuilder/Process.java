package org.workcraft.plugins.balsa.handshakestgbuilder;

import org.workcraft.plugins.balsa.stgbuilder.Event;

public interface Process
{
	public Event go();
	public Event done();
}
