package org.workcraft.plugins.balsa.handshakebuilder;

import org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.HandshakeStgBuilder;

public interface ActivePull extends ActiveSync
{
	ActivePullStg buildStg(HandshakeStgBuilder builder);
}
