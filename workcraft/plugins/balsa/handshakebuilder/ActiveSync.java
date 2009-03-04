package org.workcraft.plugins.balsa.handshakebuilder;

import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSyncStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.HandshakeStgBuilder;

public interface ActiveSync extends Handshake
{
	ActiveSyncStg buildStg(HandshakeStgBuilder builder);
}
