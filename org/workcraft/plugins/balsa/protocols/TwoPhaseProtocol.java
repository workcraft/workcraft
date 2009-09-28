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

package org.workcraft.plugins.balsa.protocols;

import org.workcraft.plugins.balsa.handshakebuilder.BooleanPull;
import org.workcraft.plugins.balsa.handshakebuilder.BooleanPush;
import org.workcraft.plugins.balsa.handshakebuilder.PullHandshake;
import org.workcraft.plugins.balsa.handshakebuilder.PushHandshake;
import org.workcraft.plugins.balsa.handshakebuilder.Sync;
import org.workcraft.plugins.balsa.handshakeevents.DataPullStg;
import org.workcraft.plugins.balsa.handshakeevents.DataPushStg;
import org.workcraft.plugins.balsa.handshakeevents.FullDataPullStg;
import org.workcraft.plugins.balsa.handshakeevents.SyncStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.HandshakeProtocol;
import org.workcraft.plugins.balsa.handshakestgbuilder.HandshakeStgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.ActiveSignal;
import org.workcraft.plugins.balsa.stgbuilder.PassiveSignal;
import org.workcraft.plugins.balsa.stgbuilder.SignalId;

public class TwoPhaseProtocol implements HandshakeProtocol {

	class Instance implements HandshakeStgBuilder
	{
		private StgBuilderForHandshakes builder;

		public Instance(StgBuilderForHandshakes stgBuilder) {
			this.builder = stgBuilder;
		}

		private void buildSignalAutoControl(ActiveSignal rq) {
			ActiveState zero = builder.buildActivePlace(1);
			ActiveState one = builder.buildActivePlace(0);
			builder.connect(rq.getMinus(), zero);
			builder.connect(rq.getPlus(), one);
			builder.connect(zero, rq.getPlus());
			builder.connect(one, rq.getMinus());
		}

		private void buildSignalAutoControl(PassiveSignal rq) {
			PassiveState zero = builder.buildPassivePlace(1);
			PassiveState one = builder.buildPassivePlace(0);
			builder.connect(rq.getMinus(), zero);
			builder.connect(rq.getPlus(), one);
			builder.connect(zero, rq.getPlus());
			builder.connect(one, rq.getMinus());
		}

		@Override public SyncStg create(Sync handshake) {
			ActiveSignal rq = builder.buildActiveSignal(new SignalId(handshake, "rq"));
			PassiveSignal ac = builder.buildPassiveSignal(new SignalId(handshake, "ac"));

			buildSignalAutoControl(rq);
			buildSignalAutoControl(ac);

			final ActiveDummy go = builder.buildActiveTransition();
			final PassiveDummy going = builder.buildPassiveTransition();
			final ActiveDummy done = builder.buildActiveTransition();

			ActiveState ready = builder.buildActivePlace(1);

			builder.connect(ready, go);

			ActiveState requesting = builder.buildActivePlace(0);

			builder.connect(requesting, rq.getMinus());
			builder.connect(requesting, rq.getPlus());

			PassiveState requested = builder.buildPassivePlace(0);

			builder.connect(rq.getMinus(), requested);
			builder.connect(rq.getPlus(), requested);
			builder.connect(requested, going);

			builder.connect(done, ready);

			PassiveState deactivating = builder.buildPassivePlace(0);

			builder.connect(going, deactivating);
			builder.connect(deactivating, ac.getMinus());
			builder.connect(deactivating, ac.getPlus());

			ActiveState rtzFinished = builder.buildActivePlace(1);

			builder.connect(ac.getMinus(), rtzFinished);
			builder.connect(ac.getPlus(), rtzFinished);
			builder.connect(rtzFinished, done);

			return new SyncStg()
			{
				@Override public ActiveEvent go() {
					return builder.get(go, going);
				}
				@Override public PassiveEvent done() {
					return builder.get(going, done);
				}
			};
		}

		public DataPullStg create(PullHandshake handshake) {
			final SyncStg sync = create((Sync)handshake);

			return new DataPullStg()
			{
				@Override public PassiveEvent done() {
					return sync.done();
				}
				@Override public ActiveEvent dataRelease() {
					return sync.go();
				}
				@Override public ActiveEvent go() {
					return sync.go();
				}
			};
		}

		public DataPushStg create(PushHandshake handshake) {
			final SyncStg sync = create((Sync)handshake);

			return new DataPushStg()
			{
				@Override public ActiveEvent go() {
					return sync.go();
				}
				@Override public PassiveEvent done() {
					return sync.done();
				}
				@Override public PassiveEvent dataRelease() {
					return sync.done();
				}
			};
		}

		@Override
		public FullDataPullStg create(BooleanPull handshake) {
			throw new RuntimeException("Not implemented!");// TODO Implement
		}

		@Override
		public FullDataPullStg create(BooleanPush handshake) {
			throw new RuntimeException("Not implemented!");// TODO Implement
		}
	}

	@Override
	public HandshakeStgBuilder get(StgBuilderForHandshakes stgBuilder) {
		return new Instance(stgBuilder);
	}
}
