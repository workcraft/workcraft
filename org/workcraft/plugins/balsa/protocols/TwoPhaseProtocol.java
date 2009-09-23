package org.workcraft.plugins.balsa.protocols;

import org.workcraft.plugins.balsa.handshakebuilder.ActivePull;
import org.workcraft.plugins.balsa.handshakebuilder.ActivePush;
import org.workcraft.plugins.balsa.handshakebuilder.ActiveSync;
import org.workcraft.plugins.balsa.handshakebuilder.PassivePull;
import org.workcraft.plugins.balsa.handshakebuilder.PassivePush;
import org.workcraft.plugins.balsa.handshakebuilder.PassiveSync;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveProcess;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActivePushStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.HandshakeStgBuilder;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveProcess;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassivePullStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg;
import org.workcraft.plugins.balsa.stgbuilder.AnyPlace;
import org.workcraft.plugins.balsa.stgbuilder.InputEvent;
import org.workcraft.plugins.balsa.stgbuilder.InputPlace;
import org.workcraft.plugins.balsa.stgbuilder.OutputEvent;
import org.workcraft.plugins.balsa.stgbuilder.OutputPlace;
import org.workcraft.plugins.balsa.stgbuilder.SignalId;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.StgSignal;

public class TwoPhaseProtocol implements HandshakeStgBuilder {

	private StgBuilder builder;

	public ActiveProcess create(ActiveSync handshake) {
		StgSignal rq = builder.buildSignal(new SignalId(handshake, "rq"), true);
		StgSignal ac = builder.buildSignal(new SignalId(handshake, "ac"), false);

		buildSignalAutoControl(rq);
		buildSignalAutoControl(ac);

		final OutputEvent activator = builder.buildTransition();
		final InputEvent deactivation = builder.buildTransition();

		OutputPlace activated = builder.buildPlace();

		builder.connect(activator, activated);
		builder.connect(activated, rq.getMinus());
		builder.connect(activated, rq.getPlus());

		OutputPlace working = builder.buildPlace();

		builder.connect(rq.getMinus(), working);
		builder.connect(rq.getPlus(), working);
		builder.connect(working, ac.getMinus());
		builder.connect(working, ac.getPlus());

		InputPlace done = builder.buildInputPlace();

		builder.connect(ac.getMinus(), done);
		builder.connect(ac.getPlus(), done);
		builder.connect(done, deactivation);


		return new ActiveProcess()
		{
			public OutputEvent go() {
				return activator;
			}
			public InputEvent done() {
				return deactivation;
			}
		};
	}

	private void buildSignalAutoControl(StgSignal rq) {
		OutputPlace zero = builder.buildPlace(1);
		OutputPlace one = builder.buildPlace();
		builder.connect(rq.getMinus(), zero);
		builder.connect(rq.getPlus(), one);
		builder.connect(zero, rq.getPlus());
		builder.connect(one, rq.getMinus());
	}

	public PassiveProcess create(PassiveSync handshake) {
		StgSignal rq = builder.buildSignal(new SignalId(handshake, "rq"), false);
		StgSignal ac = builder.buildSignal(new SignalId(handshake, "ac"), true);

		buildSignalAutoControl(rq);
		buildSignalAutoControl(ac);

		final InputEvent activation = builder.buildTransition();
		final OutputEvent deactivator = builder.buildTransition();

		OutputPlace deactivated = builder.buildPlace();

		builder.connect(deactivator, deactivated);
		builder.connect(deactivated, ac.getMinus());
		builder.connect(deactivated, ac.getPlus());

		OutputPlace waiting = builder.buildPlace(1);

		builder.connect(ac.getMinus(), waiting);
		builder.connect(ac.getPlus(), waiting);
		builder.connect(waiting, rq.getMinus());
		builder.connect(waiting, rq.getPlus());

		InputPlace requested = builder.buildInputPlace();

		builder.connect(rq.getMinus(), requested);
		builder.connect(rq.getPlus(), requested);
		builder.connect(requested, activation);


		return new PassiveProcess()
		{
			public InputEvent go() {
				return activation;
			}
			public OutputEvent done() {
				return deactivator;
			}
		};
	}

	public PassivePullStg create(PassivePull handshake) {
		// TODO Auto-generated method stub
		return null;
	}

	public ActivePullStg create(ActivePull handshake) {
		final ActiveProcess sync = create((ActiveSync)handshake);

		final InputDataSignal[] dataSignals = DataSignalBuilder.buildInputDataSignals(handshake, builder);

		return new ActivePullStg()
		{
			//TODO: think where to stick data signals
			@SuppressWarnings("unused")
			public AnyPlace getData(int index, boolean value) {
				InputDataSignal signal = dataSignals[index];
				return value ? signal.p1 : signal.p0;
			}
			@Override
			public InputEvent done() {
				return sync.done();
			}
			@Override
			public OutputEvent dataRelease() {
				return sync.go();
			}
			@Override
			public OutputEvent go() {
				return sync.go();
			}
		};
	}

	public PassivePushStg create(PassivePush handshake) {
		final PassiveProcess sync = create((PassiveSync)handshake);

		final InputDataSignal[] dataSignals = DataSignalBuilder.buildInputDataSignals(handshake, builder);

		return new PassivePushStg()
		{
			//TODO: think where to stick data signals
			@SuppressWarnings("unused")
			public AnyPlace getData(int index, boolean value) {
				InputDataSignal signal = dataSignals[index];
				return value ? signal.p1 : signal.p0;
			}
			public InputEvent go() {
				return sync.go();
			}
			public OutputEvent done() {
				return sync.done();
			}
			@Override
			public OutputEvent dataRelease() {
				return sync.done();
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
