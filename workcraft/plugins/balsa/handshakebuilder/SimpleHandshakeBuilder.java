package org.workcraft.plugins.balsa.handshakebuilder;

import org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSyncStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.HandshakeStgBuilder;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSyncStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class SimpleHandshakeBuilder implements HandshakeBuilder {

	public static SimpleHandshakeBuilder getInstance()
	{
		return new SimpleHandshakeBuilder();
	}

	public ActivePull CreateActivePull(final int width) {
		return new ActivePull(){
			public ActivePullStg buildStg(HandshakeStgBuilder builder) {
				return builder.create(this);
			}

			public int getWidth() {
				return width;
			}
		};
	}

	public ActivePush CreateActivePush(int width) {
		throw new NotImplementedException();
	}

	public ActiveSync CreateActiveSync() {
		return new ActiveSync(){
			public ActiveSyncStg buildStg(HandshakeStgBuilder builder) {
				return builder.create(this);
			}
		};
	}

	public PassivePull CreatePassivePull(final int width) {
		return new PassivePull(){
			public PassivePullStg buildStg(HandshakeStgBuilder builder) {
				return builder.create(this);
			}

			public int getWidth() {
				return width;
			}
		};
	}

	public PassivePush CreatePassivePush(int width) {
		throw new NotImplementedException();
	}

	public PassiveSync CreatePassiveSync() {
		return new PassiveSync(){
			public PassiveSyncStg buildStg(HandshakeStgBuilder builder) {
				return builder.create(this);
			}
		};
	}
}
