package org.workcraft.parsers.breeze.splitting;

import org.workcraft.plugins.balsa.HandshakeComponent;

class SplitPort
{
	public SplitPort(HandshakeComponent controlPort, HandshakeComponent dataPort)
	{
		this.controlPort = controlPort;
		this.dataPort = dataPort;
	}
	public final HandshakeComponent controlPort;
	public final HandshakeComponent dataPort;
}
