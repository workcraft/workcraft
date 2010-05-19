package org.workcraft.parsers.breeze.splitting;

import org.workcraft.plugins.balsa.BreezeHandshake;

class SplitPort
{
	public SplitPort(BreezeHandshake controlPort, BreezeHandshake dataPort)
	{
		this.controlPort = controlPort;
		this.dataPort = dataPort;
	}
	public final BreezeHandshake controlPort;
	public final BreezeHandshake dataPort;
}
