package org.workcraft.plugins.balsa.protocols;

import org.workcraft.plugins.balsa.handshakebuilder.ActivePull;
import org.workcraft.plugins.balsa.handshakebuilder.ActivePush;
import org.workcraft.plugins.balsa.handshakebuilder.ActiveSync;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.handshakebuilder.PassivePull;
import org.workcraft.plugins.balsa.handshakebuilder.PassivePush;
import org.workcraft.plugins.balsa.handshakebuilder.PassiveSync;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveProcess;
import org.workcraft.plugins.balsa.handshakestgbuilder.HandshakeStgBuilder;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveProcess;
import org.workcraft.plugins.balsa.stgbuilder.InputEvent;
import org.workcraft.plugins.balsa.stgbuilder.InputOutputEvent;
import org.workcraft.plugins.balsa.stgbuilder.SignalId;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.OutputPlace;
import org.workcraft.plugins.balsa.stgbuilder.StgSignal;
import org.workcraft.plugins.balsa.stgbuilder.OutputEvent;
import org.workcraft.plugins.balsa.stgbuilder.Event;

interface HandshakeSignals
{
	InputOutputEvent getRqP();
	InputOutputEvent getRqM();
	InputOutputEvent getAcP();
	InputOutputEvent getAcM();
}

public class FourPhaseProtocol_NoDataPath implements HandshakeStgBuilder
{
	private StgBuilder builder;

	HandshakeSignals createSignals(Handshake handshake, boolean active)
	{
		final StgSignal rq = builder.buildSignal(new SignalId(handshake, "rq"), active);
		final StgSignal ac = builder.buildSignal(new SignalId(handshake, "ac"), !active);

		final OutputPlace ready = builder.buildPlace(1);

		final InputOutputEvent rqP = rq.getPlus();
		final InputOutputEvent rqM = rq.getMinus();
		final InputOutputEvent acP = ac.getPlus();
		final InputOutputEvent acM = ac.getMinus();

		builder.connect(ready, rqP);
		builder.connect(rqP, acP);
		builder.connect(acP, rqM);
		builder.connect(rqM, acM);
		builder.connect(acM, ready);

		return new HandshakeSignals()
		{
			public InputOutputEvent getAcM() {
				return acM;
			}

			public InputOutputEvent getAcP() {
				return acP;
			}

			public InputOutputEvent getRqM() {
				return rqM;
			}

			public InputOutputEvent getRqP() {
				return rqP;
			}
		};
	}

	public ActivePullStg create(ActivePull handshake) {
		final HandshakeSignals signals = createSignals(handshake, true);

		return new ActivePullStg()
		{
			public OutputEvent go() {
				return signals.getRqP();
			}

			public InputEvent done() {
				return signals.getAcP();
			}

			public OutputEvent dataRelease() {
				return signals.getRqM();
			}
		};
	}


	public ActivePushStg create(ActivePush handshake) {
		final HandshakeSignals signals = createSignals(handshake, true);

		return new ActivePushStg()
		{
			public OutputEvent go() {
				return signals.getRqP();
			}

			public Event dataRelease() {
				return signals.getAcP();
			}

			public InputEvent done() {
				return signals.getAcM();
			}
		};
	}

	public ActiveProcess create(ActiveSync handshake) {
		final HandshakeSignals signals = createSignals(handshake, true);

		return new ActiveProcess(){
			public OutputEvent go() {
				return signals.getRqP();
			}

			public InputEvent done() {
				return signals.getAcM();
			}
		};
	}

	public PassivePullStg create(PassivePull handshake) {
		final HandshakeSignals signals = createSignals(handshake, false);

		return new PassivePullStg()
		{
			public InputEvent go() {
				return signals.getRqP();
			}

			public OutputEvent done() {
				return signals.getAcP();
			}

			public Event dataRelease() {
				return signals.getRqM();
			}
		};
	}

	public PassivePushStg create(PassivePush handshake) {
		final HandshakeSignals signals = createSignals(handshake, false);

		return new PassivePushStg()
		{
			public InputEvent go() {
				return signals.getRqP();
			}

			public OutputEvent dataRelease() {
				return signals.getAcP();
			}

			public OutputEvent done() {
				return signals.getAcM();
			}
		};
	}

	public StgBuilder getStgBuilder() {
		return builder;
	}

	public void setStgBuilder(StgBuilder builder) {
		this.builder = builder;
	}


	public PassiveProcess create(PassiveSync handshake) {
		final HandshakeSignals signals = createSignals(handshake, false);

		return new PassiveProcess(){
			public InputEvent go() {
				return signals.getRqP();
			}

			public OutputEvent done() {
				return signals.getAcP();
			}
		};
	}
}
