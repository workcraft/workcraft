package org.workcraft.plugins.balsa.handshakebuilder;



public interface HandshakeBuilder
{
	public ActiveSync CreateActiveSync();
	public PassiveSync CreatePassiveSync();
	public PassivePull CreatePassivePull(int width);
	public ActivePull CreateActivePull(int width);
	public PassivePush CreatePassivePush(int width);
	public ActivePush CreateActivePush(int width);
}
