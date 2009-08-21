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
import org.workcraft.plugins.balsa.stgbuilder.ReadablePlace;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;
import org.workcraft.plugins.balsa.stgbuilder.StgSignal;
import org.workcraft.plugins.balsa.stgbuilder.StgTransition;
import org.workcraft.plugins.balsa.stgbuilder.SignalId;
import org.workcraft.plugins.balsa.stgbuilder.TransitionOutput;

public class FourPhaseProtocol_NoDataPath implements HandshakeStgBuilder
{
	private StgBuilder builder;

	public ActiveSyncWithRtz createActiveSyncWithRtz(Handshake handshake) {
		FourPhaseProtocol fpp = new FourPhaseProtocol();
		fpp.setStgBuilder(builder);
		return fpp.createActiveSyncWithRtz(handshake);
	}

	public ActivePullStg create(ActivePull handshake) {
		final ActiveSyncWithRtz sync = createActiveSyncWithRtz(handshake);

		return new ActivePullStg()
		{
			public StgTransition getActivator() {
				return sync.getActivator();
			}

			public ReadablePlace getData(int index, boolean value) {
				return null;
			}

			public StgTransition getDeactivationNotificator() {
				return sync.getDeactivationNotificator();
			}

			public StgTransition getDataReleaser() {
				return sync.getRtz();
			}
		};
	}


	public ActivePushStg create(ActivePush handshake) {
		final ActiveSyncWithRtz sync = createActiveSyncWithRtz(handshake);

		return new ActivePushStg()
		{
			public StgTransition getActivator() {
				return sync.getActivator();
			}

			public StgTransition getDeactivationNotificator() {
				return sync.getDeactivationNotificator();
			}
		};
	}

	public ActiveSyncStg create(ActiveSync handshake) {
		return createActiveSyncWithRtz(handshake);
	}

	public PassivePullStg create(PassivePull handshake) {
		final PassiveSyncWithRtz sync = create((PassiveSync)handshake);

		return new PassivePullStg()
		{
			public StgTransition getActivationNotificator() {
				return sync.getActivationNotificator();
			}

			public StgTransition getDeactivator() {
				return sync.getDeactivator();
			}

			public TransitionOutput getDataRelease() {
				return sync.getRtz();
			}
		};
	}

	public PassivePushStg create(PassivePush handshake) {
		final PassiveSyncStg sync = create((PassiveSync)handshake);

		return new PassivePushStg()
		{
			public StgTransition getActivationNotificator() {
				return sync.getActivationNotificator();
			}

			public StgTransition getDeactivator() {
				return sync.getDeactivator();
			}

			public ReadablePlace getData(int index, boolean value) {
				return null;
			}

			public StgTransition getDataReleaser() {
				return sync.getDeactivator();
			}
		};
	}

	public PassiveSyncWithRtz create(PassiveSync handshake) {

		final StgPlace ready = builder.buildPlace(1);
		final StgPlace exiting = builder.buildPlace();
		final StgPlace rtz = builder.buildPlace();

		final StgSignal rq = builder.buildSignal(new SignalId(handshake, "rq"), false);
		final StgSignal ac = builder.buildSignal(new SignalId(handshake, "ac"), true);

		final StgTransition rqP = rq.getPlus();
		final StgTransition rqM = rq.getMinus();
		final StgTransition acP = ac.getPlus();
		final StgTransition acM = ac.getMinus();

		builder.addConnection(acP, exiting);
		builder.addConnection(exiting, rqM);
		builder.addConnection(rqM, rtz);
		builder.addConnection(rtz, acM);
		builder.addConnection(acM, ready);
		builder.addConnection(ready, rqP);
		builder.addConnection(rqP, acP);

		return new PassiveSyncWithRtz()
		{
			public StgTransition getActivationNotificator() {
				return rqP;
			}

			public StgTransition getDeactivator() {
				return acP;
			}

			public TransitionOutput getRtz() {
				return rqM;
			}
		};
	}

	public StgBuilder getStgBuilder() {
		return builder;
	}

	public void setStgBuilder(StgBuilder builder) {
		this.builder = builder;
	}
}
