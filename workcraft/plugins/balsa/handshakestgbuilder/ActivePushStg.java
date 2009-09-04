package org.workcraft.plugins.balsa.handshakestgbuilder;

import org.workcraft.plugins.balsa.stgbuilder.StgTransition;
import org.workcraft.plugins.balsa.stgbuilder.TransitionOutput;

public interface ActivePushStg extends StgHandshake
{
	StgTransition getActivate();
	TransitionOutput getDataReleased();
	TransitionOutput getDeactivate();
}
