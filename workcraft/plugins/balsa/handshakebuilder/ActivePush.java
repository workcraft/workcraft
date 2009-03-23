package org.workcraft.plugins.balsa.handshakebuilder;

import org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.HandshakeStgBuilder;

public interface ActivePush extends ActiveSync, DataHandshake
{
	ActivePushStg buildStg(HandshakeStgBuilder builder);
}
