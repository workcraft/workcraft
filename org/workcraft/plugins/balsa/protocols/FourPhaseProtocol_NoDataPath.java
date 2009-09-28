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
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
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

interface HandshakeSignals
{
	ActiveEvent getRqP();
	ActiveEvent getRqM();
	PassiveEvent getAcP();
	PassiveEvent getAcM();
}

public class FourPhaseProtocol_NoDataPath implements HandshakeProtocol
{
	class StgBuilder implements HandshakeStgBuilder
	{
		private StgBuilderForHandshakes builder;

		public StgBuilder(StgBuilderForHandshakes stgBuilder) {
			this.builder = stgBuilder;
		}

		private HandshakeSignals createSignals(Handshake handshake)
		{
			final ActiveSignal rq = builder.buildActiveSignal(new SignalId(handshake, "rq"));
			final PassiveSignal ac = builder.buildPassiveSignal(new SignalId(handshake, "ac"));

			final ActiveState ready = builder.buildActivePlace(1);

			final ActiveEvent rqP = rq.getPlus();
			final ActiveEvent rqM = rq.getMinus();
			final PassiveEvent acP = ac.getPlus();
			final PassiveEvent acM = ac.getMinus();

			builder.connect(ready, rqP);
			builder.connect(rqP, acP);
			builder.connect(acP, rqM);
			builder.connect(rqM, acM);
			builder.connect(acM, ready);

			return new HandshakeSignals()
			{
				public PassiveEvent getAcM() {
					return acM;
				}

				public PassiveEvent getAcP() {
					return acP;
				}

				public ActiveEvent getRqM() {
					return rqM;
				}

				public ActiveEvent getRqP() {
					return rqP;
				}
			};
		}

		public DataPullStg create(PullHandshake handshake) {
			final HandshakeSignals signals = createSignals(handshake);

			return new DataPullStg()
			{
				public ActiveEvent go() {
					return signals.getRqP();
				}

				public PassiveEvent done() {
					return signals.getAcP();
				}

				public ActiveEvent dataRelease() {
					return signals.getRqM();
				}
			};
		}


		public DataPushStg create(PushHandshake handshake) {
			final HandshakeSignals signals = createSignals(handshake);

			return new DataPushStg()
			{
				public ActiveEvent go() {
					return signals.getRqP();
				}

				public PassiveEvent dataRelease() {
					return signals.getAcP();
				}

				public PassiveEvent done() {
					return signals.getAcM();
				}
			};
		}

		public SyncStg create(Sync handshake) {
			final HandshakeSignals signals = createSignals(handshake);

			return new SyncStg(){
				public ActiveEvent go() {
					return signals.getRqP();
				}

				public PassiveEvent done() {
					return signals.getAcM();
				}
			};
		}

		@Override
		public FullDataPullStg create(BooleanPull handshake) {
			throw new RuntimeException("Not supported");
		}

		@Override
		public FullDataPullStg create(BooleanPush handshake) {
			throw new RuntimeException("Not supported");
		}
	}

	@Override
	public HandshakeStgBuilder get(
			StgBuilderForHandshakes stgBuilder) {
		return new StgBuilder(stgBuilder);
	}

}
