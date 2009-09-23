package org.workcraft.plugins.balsa.handshakestgbuilder;

import org.workcraft.plugins.balsa.handshakebuilder.ActivePull;
import org.workcraft.plugins.balsa.handshakebuilder.ActivePush;
import org.workcraft.plugins.balsa.handshakebuilder.ActiveSync;
import org.workcraft.plugins.balsa.handshakebuilder.PassivePull;
import org.workcraft.plugins.balsa.handshakebuilder.PassivePush;
import org.workcraft.plugins.balsa.handshakebuilder.PassiveSync;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;

public interface HandshakeStgBuilder
{
	public StgBuilder getStgBuilder();
	void setStgBuilder(StgBuilder builder);
	public ActiveProcess create(ActiveSync handshake);
	public PassiveProcess create(PassiveSync handshake);
	public PassivePullStg create(PassivePull handshake);
	public ActivePullStg create(ActivePull handshake);
	public PassivePushStg create(PassivePush handshake);
	public ActivePushStg create(ActivePush handshake);
}
