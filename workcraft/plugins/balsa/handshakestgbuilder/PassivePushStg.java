package org.workcraft.plugins.balsa.handshakestgbuilder;

import org.workcraft.plugins.balsa.stgbuilder.StgTransition;
import org.workcraft.plugins.balsa.stgbuilder.TransitionOutput;

public interface PassivePushStg extends StgHandshake
{
	TransitionOutput getActivate();
	StgTransition getDataReleased();
	StgTransition getDeactivate();
}
