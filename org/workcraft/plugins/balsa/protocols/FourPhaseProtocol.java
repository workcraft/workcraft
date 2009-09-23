package org.workcraft.plugins.balsa.protocols;

import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveProcess;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveProcess;
import org.workcraft.plugins.balsa.stgbuilder.OutputPlace;
import org.workcraft.plugins.balsa.stgbuilder.OutputEvent;
import org.workcraft.plugins.balsa.stgbuilder.Event;

class InputDataSignal
{
	OutputPlace p0;
	OutputPlace p1;
}

interface ActiveSyncWithRtz extends ActiveProcess
{
	public OutputEvent getRtz();
}

interface PassiveSyncWithRtz extends PassiveProcess
{
	public Event getRtz();
}
