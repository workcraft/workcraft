package org.workcraft.plugins.balsa.handshakebuilder;

import org.workcraft.plugins.balsa.handshakestgbuilder.HandshakeStgBuilder;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg;

public interface PassivePull extends PassiveSync
{
	PassivePullStg buildStg(HandshakeStgBuilder builder);
}
