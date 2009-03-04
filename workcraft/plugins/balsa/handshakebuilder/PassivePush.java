package org.workcraft.plugins.balsa.handshakebuilder;

import org.workcraft.plugins.balsa.handshakestgbuilder.HandshakeStgBuilder;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg;

public interface PassivePush extends PassiveSync
{
	PassivePushStg buildStg(HandshakeStgBuilder builder);
}
