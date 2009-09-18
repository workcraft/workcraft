package org.workcraft.plugins.balsa.handshakebuilder;

import org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.HandshakeStgBuilder;

public interface ActivePull extends ActiveSync, DataHandshake
{
	ActivePullStg buildStg(HandshakeStgBuilder builder);
}
