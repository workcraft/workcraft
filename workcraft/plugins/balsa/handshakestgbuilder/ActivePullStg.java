package org.workcraft.plugins.balsa.handshakestgbuilder;

import org.workcraft.plugins.balsa.stgbuilder.ReadablePlace;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;

public interface ActivePullStg extends ActiveSyncStg
{
	public StgPlace getReleaseDataPlace();
	public ReadablePlace getData(int index, boolean value);
}
