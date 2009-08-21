package org.workcraft.plugins.balsa.protocols;

import org.workcraft.plugins.balsa.handshakebuilder.ActivePull;
import org.workcraft.plugins.balsa.handshakebuilder.ActivePush;
import org.workcraft.plugins.balsa.handshakebuilder.ActiveSync;
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
import org.workcraft.plugins.balsa.stgbuilder.SignalId;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;
import org.workcraft.plugins.balsa.stgbuilder.StgSignal;
import org.workcraft.plugins.balsa.stgbuilder.StgTransition;

public class TwoPhaseProtocol implements HandshakeStgBuilder {

	private StgBuilder builder;

	public ActiveSyncStg create(ActiveSync handshake) {
		StgSignal rq = builder.buildSignal(new SignalId(handshake, "rq"), true);
		StgSignal ac = builder.buildSignal(new SignalId(handshake, "ac"), false);

		buildSignalAutoControl(rq);
		buildSignalAutoControl(ac);

		final StgTransition activator = builder.buildTransition();
		final StgTransition deactivation = builder.buildTransition();

		StgPlace activated = builder.buildPlace();

		builder.addConnection(activator, activated);
		builder.addConnection(activated, rq.getMinus());
		builder.addConnection(activated, rq.getPlus());

		StgPlace working = builder.buildPlace();

		builder.addConnection(rq.getMinus(), working);
		builder.addConnection(rq.getPlus(), working);
		builder.addConnection(working, ac.getMinus());
		builder.addConnection(working, ac.getPlus());

		StgPlace done = builder.buildPlace();

		builder.addConnection(ac.getMinus(), done);
		builder.addConnection(ac.getPlus(), done);
		builder.addConnection(done, deactivation);


		return new ActiveSyncStg()
		{
			public StgTransition getActivator() {
				return activator;
			}
			public StgTransition getDeactivationNotificator() {
				return deactivation;
			}
		};
	}

	private void buildSignalAutoControl(StgSignal rq) {
		StgPlace zero = builder.buildPlace(1);
		StgPlace one = builder.buildPlace();
		builder.addConnection(rq.getMinus(), zero);
		builder.addConnection(rq.getPlus(), one);
		builder.addConnection(zero, rq.getPlus());
		builder.addConnection(one, rq.getMinus());
	}

	public PassiveSyncStg create(PassiveSync handshake) {
		StgSignal rq = builder.buildSignal(new SignalId(handshake, "rq"), false);
		StgSignal ac = builder.buildSignal(new SignalId(handshake, "ac"), true);

		buildSignalAutoControl(rq);
		buildSignalAutoControl(ac);

		final StgTransition activation = builder.buildTransition();
		final StgTransition deactivator = builder.buildTransition();

		StgPlace deactivated = builder.buildPlace();

		builder.addConnection(deactivator, deactivated);
		builder.addConnection(deactivated, ac.getMinus());
		builder.addConnection(deactivated, ac.getPlus());

		StgPlace waiting = builder.buildPlace(1);

		builder.addConnection(ac.getMinus(), waiting);
		builder.addConnection(ac.getPlus(), waiting);
		builder.addConnection(waiting, rq.getMinus());
		builder.addConnection(waiting, rq.getPlus());

		StgPlace requested = builder.buildPlace();

		builder.addConnection(rq.getMinus(), requested);
		builder.addConnection(rq.getPlus(), requested);
		builder.addConnection(requested, activation);


		return new PassiveSyncStg()
		{
			public StgTransition getActivationNotificator() {
				return activation;
			}
			public StgTransition getDeactivator() {
				return deactivator;
			}
		};
	}

	public PassivePullStg create(PassivePull handshake) {
		// TODO Auto-generated method stub
		return null;
	}

	public ActivePullStg create(ActivePull handshake) {
		final ActiveSyncStg sync = create((ActiveSync)handshake);

		final InputDataSignal[] dataSignals = DataSignalBuilder.buildInputDataSignals(handshake, builder);

		return new ActivePullStg()
		{
			public StgTransition getActivator() {
				return sync.getActivator();
			}
			public StgTransition getDeactivationNotificator() {
				return sync.getDeactivationNotificator();
			}
			public StgTransition getDataReleaser() {
				return null;
			}
			public ReadablePlace getData(int index, boolean value) {
				InputDataSignal signal = dataSignals[index];
				return value ? signal.p1 : signal.p0;
			}
		};
	}

	public PassivePushStg create(PassivePush handshake) {
		final PassiveSyncStg sync = create((PassiveSync)handshake);

		final InputDataSignal[] dataSignals = DataSignalBuilder.buildInputDataSignals(handshake, builder);

		return new PassivePushStg()
		{
			public ReadablePlace getData(int index, boolean value) {
				InputDataSignal signal = dataSignals[index];
				return value ? signal.p1 : signal.p0;
			}
			public StgTransition getActivationNotificator() {
				return sync.getActivationNotificator();
			}
			public StgTransition getDeactivator() {
				return sync.getDeactivator();
			}
			public StgTransition getDataReleaser() {
				return null;
			}
		};
	}

	public ActivePushStg create(ActivePush handshake) {
		throw new RuntimeException("Not implemented");
		/*		final ActiveSyncStg sync = create((ActiveSync)handshake);

		final InputDataSignal[] dataSignals = DataSignalBuilder.buildOutputDataSignals(handshake, builder);

		return new ActivePushStg()
		{
			public ReadablePlace getData(int index, boolean value) {
				InputDataSignal signal = dataSignals[index];
				return value ? signal.p1 : signal.p0;
			}
			public StgTransition getActivator() {
				return sync.getActivator();
			}
			public TransitionOutput getDeactivationNotificator() {
				return sync.getDeactivationNotificator();
			}
			public StgPlace getReleaseDataPlace() {
				return null;
			}
		};*/
	}

	public StgBuilder getStgBuilder() {
		return builder;
	}

	public void setStgBuilder(StgBuilder builder) {
		this.builder = builder;;
	}

}
