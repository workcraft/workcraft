package org.workcraft.plugins.balsa.handshakebuilder;

import org.workcraft.plugins.balsa.handshakestgbuilder.HandshakeStgBuilder;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSyncStg;

public interface PassiveSync extends Handshake
{
	PassiveSyncStg buildStg(HandshakeStgBuilder builder);
}
