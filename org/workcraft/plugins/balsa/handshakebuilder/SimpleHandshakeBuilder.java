package org.workcraft.plugins.balsa.handshakebuilder;

import org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveProcess;
import org.workcraft.plugins.balsa.handshakestgbuilder.HandshakeStgBuilder;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveProcess;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg;

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

	public ActivePush CreateActivePush(final int width) {
		return new ActivePush()
		{
			public ActivePushStg buildStg(HandshakeStgBuilder builder) {
				return builder.create(this);
			}

			public int getWidth() {
				return width;
			}
		};
	}

	public ActiveSync CreateActiveSync() {
		return new ActiveSync(){
			public ActiveProcess buildStg(HandshakeStgBuilder builder) {
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

	public PassivePush CreatePassivePush(final int width) {
		return new PassivePush()
		{
			public PassivePushStg buildStg(HandshakeStgBuilder builder) {
				return builder.create(this);
			}

			public int getWidth() {
				return width;
			}
		};
	}

	public PassiveSync CreatePassiveSync() {
		return new PassiveSync(){
			public PassiveProcess buildStg(HandshakeStgBuilder builder) {
				return builder.create(this);
			}
		};
	}
}
