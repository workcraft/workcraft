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
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSyncStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.HandshakeStgBuilder;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSyncStg;
import org.workcraft.plugins.balsa.stgbuilder.SignalId;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;
import org.workcraft.plugins.balsa.stgbuilder.StgSignal;
import org.workcraft.plugins.balsa.stgbuilder.StgTransition;
import org.workcraft.plugins.balsa.stgbuilder.TransitionOutput;

interface HandshakeSignals
{
	StgTransition getRqP();
	StgTransition getRqM();
	StgTransition getAcP();
	StgTransition getAcM();
}

public class FourPhaseProtocol_NoDataPath implements HandshakeStgBuilder
{
	private StgBuilder builder;

	HandshakeSignals createSignals(Handshake handshake, boolean active)
	{
		final StgSignal rq = builder.buildSignal(new SignalId(handshake, "rq"), active);
		final StgSignal ac = builder.buildSignal(new SignalId(handshake, "ac"), !active);

		final StgPlace ready = builder.buildPlace(1);

		final StgTransition rqP = rq.getPlus();
		final StgTransition rqM = rq.getMinus();
		final StgTransition acP = ac.getPlus();
		final StgTransition acM = ac.getMinus();

		builder.addConnection(ready, rqP);
		builder.addConnection(rqP, acP);
		builder.addConnection(acP, rqM);
		builder.addConnection(rqM, acM);
		builder.addConnection(acM, ready);

		return new HandshakeSignals()
		{
			public StgTransition getAcM() {
				return acM;
			}

			public StgTransition getAcP() {
				return acP;
			}

			public StgTransition getRqM() {
				return rqM;
			}

			public StgTransition getRqP() {
				return rqP;
			}
		};
	}

	public ActivePullStg create(ActivePull handshake) {
		final HandshakeSignals signals = createSignals(handshake, true);

		return new ActivePullStg()
		{
			public StgTransition getActivate() {
				return signals.getRqP();
			}

			public TransitionOutput getDataReady() {
				return signals.getAcP();
			}

			public StgTransition getDataRelease() {
				return signals.getRqM();
			}
		};
	}


	public ActivePushStg create(ActivePush handshake) {
		final HandshakeSignals signals = createSignals(handshake, true);

		return new ActivePushStg()
		{
			public StgTransition getActivate() {
				return signals.getRqP();
			}

			public TransitionOutput getDataReleased() {
				return signals.getAcP();
			}

			public TransitionOutput getDeactivate() {
				return signals.getAcM();
			}
		};
	}

	public ActiveSyncStg create(ActiveSync handshake) {
		final HandshakeSignals signals = createSignals(handshake, true);

		return new ActiveSyncStg(){
			public StgTransition getActivate() {
				return signals.getRqP();
			}

			public TransitionOutput getDeactivate() {
				return signals.getAcM();
			}
		};
	}

	public PassivePullStg create(PassivePull handshake) {
		final HandshakeSignals signals = createSignals(handshake, false);

		return new PassivePullStg()
		{
			public StgTransition getActivate() {
				return signals.getRqP();
			}

			public StgTransition getDataReady() {
				return signals.getAcP();
			}

			public TransitionOutput getDataRelease() {
				return signals.getRqM();
			}
		};
	}

	public PassivePushStg create(PassivePush handshake) {
		final HandshakeSignals signals = createSignals(handshake, false);

		return new PassivePushStg()
		{
			public TransitionOutput getActivate() {
				return signals.getRqP();
			}

			public StgTransition getDataReleased() {
				return signals.getAcP();
			}

			public StgTransition getDeactivate() {
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


	public PassiveSyncStg create(PassiveSync handshake) {
		final HandshakeSignals signals = createSignals(handshake, false);

		return new PassiveSyncStg(){
			public TransitionOutput getActivate() {
				return signals.getRqP();
			}

			public StgTransition getDeactivate() {
				return signals.getAcP();
			}
		};
	}
}
