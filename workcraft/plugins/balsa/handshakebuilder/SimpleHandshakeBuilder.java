package org.workcraft.plugins.balsa.handshakebuilder;

import org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSyncStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.HandshakeStgBuilder;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSyncStg;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class SimpleHandshakeBuilder implements HandshakeBuilder {

	public static SimpleHandshakeBuilder getInstance()
	{
		return new SimpleHandshakeBuilder();
	}

	@Override
	public ActivePull CreateActivePull(final int width) {
		return new ActivePull(){
			@Override
			public ActivePullStg buildStg(HandshakeStgBuilder builder) {
				return builder.create(this);
			}

			@Override
			public int getWidth() {
				return width;
			}
		};
	}

	@Override
	public ActivePush CreateActivePush(int width) {
		throw new NotImplementedException();
	}

	@Override
	public ActiveSync CreateActiveSync() {
		return new ActiveSync(){
			@Override
			public ActiveSyncStg buildStg(HandshakeStgBuilder builder) {
				return builder.create(this);
			}
		};
	}

	@Override
	public PassivePull CreatePassivePull(int width) {
		throw new NotImplementedException();
	}

	@Override
	public PassivePush CreatePassivePush(int width) {
		throw new NotImplementedException();
	}

	@Override
	public PassiveSync CreatePassiveSync() {
		return new PassiveSync(){
			@Override
			public PassiveSyncStg buildStg(HandshakeStgBuilder builder) {
				return builder.create(this);
			}
		};
	}
}
