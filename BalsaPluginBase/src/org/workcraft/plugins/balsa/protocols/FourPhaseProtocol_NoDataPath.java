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

import java.util.ArrayList;
import java.util.List;

import org.workcraft.plugins.balsa.handshakebuilder.FullDataPull;
import org.workcraft.plugins.balsa.handshakebuilder.FullDataPush;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.handshakebuilder.PullHandshake;
import org.workcraft.plugins.balsa.handshakebuilder.PushHandshake;
import org.workcraft.plugins.balsa.handshakebuilder.Sync;
import org.workcraft.plugins.balsa.handshakeevents.DataPullStg;
import org.workcraft.plugins.balsa.handshakeevents.DataPushStg;
import org.workcraft.plugins.balsa.handshakeevents.FullDataPullStg;
import org.workcraft.plugins.balsa.handshakeevents.FullDataPushStg;
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

		public DataPullStg visit(PullHandshake handshake) {
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


		public DataPushStg visit(PushHandshake handshake) {
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
					return builder.get(signals.getAcP(), signals.getAcM());
				}
			};
		}

		public SyncStg visit(Sync handshake) {
			final HandshakeSignals signals = createSignals(handshake);

			return new SyncStg(){
				public ActiveEvent go() {
					return signals.getRqP();
				}

				public PassiveEvent done() {
					return builder.get(signals.getAcP(), signals.getAcM());
				}
			};
		}

		@Override
		public FullDataPullStg visit(FullDataPull handshake) {
			//The encoding is one-hot (we are trying to preserve a DI protocol)

			final ActiveSignal rq = builder.buildActiveSignal(new SignalId(handshake, "rq"));

			int wireCount = handshake.getValuesCount();
			List<PassiveSignal> dataWires = new ArrayList<PassiveSignal>();
			final List<PassiveEvent> result = new ArrayList<PassiveEvent>();

			for(int i=0;i<wireCount;i++)
			{
				PassiveSignal signal = builder.buildPassiveSignal(new SignalId(handshake, "data"+i));
				dataWires.add(signal);
			}

			ActiveState ready = builder.buildActivePlace(1);
			ActiveState resultSent = builder.buildActivePlace(0);
			PassiveState releaseSent = builder.buildPassivePlace(0);


			final ActiveEvent go = rq.getPlus();

			final ActiveEvent release = rq.getMinus();

			PassiveState resultChoice = builder.buildPassivePlace(0);

			builder.connect(ready, go);
			builder.connect(go, resultChoice);
			builder.connect(resultSent, release);
			builder.connect(release, releaseSent);

			for(int i=0;i<wireCount;i++)
			{
				PassiveDummy produceResult = builder.buildPassiveTransition();
				builder.connect(resultChoice, produceResult);
				PassiveEvent setTransition = dataWires.get(i).getPlus();
				builder.connect(produceResult, setTransition);
				builder.connect(setTransition, resultSent);

				PassiveState resultHigh = builder.buildPassivePlace(0);
				builder.connect(produceResult, resultHigh);

				PassiveEvent resetTransition = dataWires.get(i).getMinus();
				builder.connect(releaseSent, resetTransition);
				builder.connect(resultHigh, resetTransition);

				builder.connect(resetTransition, ready);

				result.add(builder.get(produceResult, setTransition));
			}

			return new FullDataPullStg()
			{
				@Override public ActiveEvent go() {
					return go;
				}
				@Override public List<PassiveEvent> result() {
					return result;
				}
			};
		}

		@Override
		public FullDataPushStg visit(FullDataPush handshake) {
			//The encoding is one-hot (we are trying to preserve a DI protocol)

			final PassiveSignal ac = builder.buildPassiveSignal(new SignalId(handshake, "ac"));

			int wireCount = handshake.getValuesCount();
			List<ActiveSignal> dataWires = new ArrayList<ActiveSignal>();
			final List<ActiveEvent> result = new ArrayList<ActiveEvent>();

			for(int i=0;i<wireCount;i++)
			{
				ActiveSignal signal = builder.buildActiveSignal(new SignalId(handshake, "data"+i));
				dataWires.add(signal);
			}

			PassiveState ready = builder.buildPassivePlace(0);
			PassiveState resultSent = builder.buildPassivePlace(0);
			ActiveState releaseSent = builder.buildActivePlace(0);

			final PassiveEvent go = ac.getMinus();

			final PassiveEvent release = ac.getPlus();

			ActiveState resultChoice = builder.buildActivePlace(1);

			builder.connect(ready, go);
			builder.connect(go, resultChoice);
			builder.connect(resultSent, release);
			builder.connect(release, releaseSent);

			for(int i=0;i<wireCount;i++)
			{
				ActiveDummy produceResult = builder.buildActiveTransition();
				builder.connect(resultChoice, produceResult);
				ActiveEvent setTransition = dataWires.get(i).getPlus();
				builder.connect(produceResult, setTransition);
				builder.connect(setTransition, resultSent);

				ActiveState resultHigh = builder.buildActivePlace(0);
				builder.connect(produceResult, resultHigh);

				ActiveEvent resetTransition = dataWires.get(i).getMinus();
				builder.connect(releaseSent, resetTransition);
				builder.connect(resultHigh, resetTransition);

				builder.connect(resetTransition, ready);

				result.add(builder.get(produceResult, setTransition));
			}

			return new FullDataPushStg()
			{
				@Override public List<ActiveEvent> data() {
					return result;
				}
				@Override public PassiveEvent done() {
					return builder.get(release, go);
				}
			};
		}
	}

	@Override
	public HandshakeStgBuilder get(StgBuilderForHandshakes stgBuilder) {
		return new StgBuilder(stgBuilder);
	}

}
