package org.workcraft.plugins.balsa.handshakestgbuilder;

import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;

public interface HandshakeStgBuilder
{
	public StgBuilder getStgBuilder();
	public ActiveSyncStg createActiveSync();
	public PassiveSyncStg createPassiveSync();
	public PassivePullStg createPassivePull(int width);
	public ActivePullStg createActivePull(int width);
	public PassivePushStg createPassivePush(int width);
	public ActivePushStg createActivePush(int width);
}
