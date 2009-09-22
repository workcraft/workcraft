/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.plugins.balsa.handshakebuilder;

import org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSyncStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.HandshakeStgBuilder;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSyncStg;
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
			public PassiveSyncStg buildStg(HandshakeStgBuilder builder) {
				return builder.create(this);
			}
		};
	}
}
