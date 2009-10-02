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

import org.workcraft.plugins.balsa.handshakebuilder.FullDataPull;
import org.workcraft.plugins.balsa.handshakebuilder.FullDataPush;
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

		private ActiveEvent buildSignalToggle(ActiveSignal rq) {

			ActiveDummy toggle = builder.buildActiveTransition();

			ActiveState toggling = builder.buildActivePlace(0);
			builder.connect(toggle, toggling);

			ActiveState zero = builder.buildActivePlace(1);
			ActiveState one = builder.buildActivePlace(0);
			ActiveDummy setOne = builder.buildActiveTransition();
			ActiveDummy setZero = builder.buildActiveTransition();
			builder.connect(setZero, zero);
			builder.connect(setOne, one);
			builder.connect(zero, setOne);
			builder.connect(one, setZero);
			builder.connect(toggling, setOne);
			builder.connect(toggling, setZero);

			builder.connect(setOne, rq.getPlus());
			builder.connect(setZero, rq.getMinus());

			PassiveState toggled = builder.buildPassivePlace(0);

			PassiveDummy detector = builder.buildPassiveTransition();

			builder.connect(toggled, detector);

			return builder.get(toggle, detector);
		}

		private PassiveEvent buildSignalToggle(PassiveSignal rq) {

			PassiveDummy toggle = builder.buildPassiveTransition();

			PassiveState toggling = builder.buildPassivePlace(0);
			builder.connect(toggle, toggling);

			PassiveState zero = builder.buildPassivePlace(1);
			PassiveState one = builder.buildPassivePlace(0);
			PassiveDummy setOne = builder.buildPassiveTransition();
			PassiveDummy setZero = builder.buildPassiveTransition();
			builder.connect(setZero, zero);
			builder.connect(setOne, one);
			builder.connect(zero, setOne);
			builder.connect(one, setZero);
			builder.connect(toggling, setOne);
			builder.connect(toggling, setZero);

			builder.connect(setOne, rq.getPlus());
			builder.connect(setZero, rq.getMinus());

			ActiveState toggled = builder.buildActivePlace(0);

			builder.connect(rq.getPlus(), toggled);
			builder.connect(rq.getMinus(), toggled);

			ActiveDummy detector = builder.buildActiveTransition();

			builder.connect(toggled, detector);

			return builder.get(toggle, detector);
		}

		@Override public SyncStg create(Sync handshake)
		{
			final ActiveEvent rqToggle = buildSignalToggle(builder.buildActiveSignal(new SignalId(handshake, "rq")));
			final PassiveEvent acToggle = buildSignalToggle(builder.buildPassiveSignal(new SignalId(handshake, "ac")));

			ActiveState ready = builder.buildActivePlace(1);

			builder.connect(ready, rqToggle);

			builder.connect(rqToggle, acToggle);

			builder.connect(acToggle, ready);

			return new SyncStg()
			{
				@Override public ActiveEvent go() {
					return rqToggle;
				}
				@Override public PassiveEvent done() {
					return acToggle;
				}
			};
		}

		@Override public DataPullStg create(PullHandshake handshake) {
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
		public FullDataPullStg create(FullDataPull handshake) {
			throw new RuntimeException("Not implemented!");// TODO Implement
		}

		@Override
		public FullDataPullStg create(FullDataPush handshake) {
			throw new RuntimeException("Not implemented!");// TODO Implement
		}
	}

	@Override
	public HandshakeStgBuilder get(StgBuilderForHandshakes stgBuilder) {
		return new Instance(stgBuilder);
	}
}
