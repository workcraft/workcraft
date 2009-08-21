package org.workcraft.plugins.balsa.handshakestgbuilder;

import org.workcraft.plugins.balsa.stgbuilder.ReadablePlace;
import org.workcraft.plugins.balsa.stgbuilder.StgTransition;

public interface DataReadStg {
	public StgTransition getDataReleaser();
	public ReadablePlace getData(int index, boolean value);
}
