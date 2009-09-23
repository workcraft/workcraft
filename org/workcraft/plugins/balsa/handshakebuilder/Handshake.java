package org.workcraft.plugins.balsa.handshakebuilder;

import org.workcraft.plugins.balsa.handshakestgbuilder.HandshakeStgBuilder;
import org.workcraft.plugins.balsa.handshakestgbuilder.Process;

public interface Handshake
{
	public Process buildStg(HandshakeStgBuilder builder);
}
