package org.workcraft.plugins.balsa.components;

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

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class FourPhaseProtocol implements HandshakeStgBuilder
{
	private final StgBuilder builder;

	public FourPhaseProtocol (StgBuilder builder)
	{
		this.builder = builder;
	}

	public ActivePullStg create(ActivePull handshake) {
		final ActiveSyncWithRtz sync = createActiveSyncWithRtz(handshake);

		final int width = handshake.getWidth();

		final StgPlace [] data0 = new StgPlace [width];
		final StgPlace [] data1 = new StgPlace [width];

		for(int i=0;i<width;i++)
		{
			data0[i] = builder.buildPlace(1);
			data1[i] = builder.buildPlace();
			StgSignal dataSignal = builder.buildSignal(new SignalId(handshake, "data"+i), false);
			builder.addConnection(data0[i], dataSignal.getPlus());
			builder.addConnection(dataSignal.getPlus(), data1[i]);
			builder.addConnection(data1[i], dataSignal.getMinus());
			builder.addConnection(dataSignal.getMinus(), data0[i]);
		}

		return new ActivePullStg()
		{
			public StgTransition getActivator() {
				return sync.getActiveSync().getActivator();
			}
			public ReadablePlace getData(int index, boolean value) {
				return value ? data1[index] : data0[index];
			}

			public TransitionOutput getDeactivationNotificator() {
				return sync.getActiveSync().getDeactivationNotificator();
			}
			public StgPlace getReleaseDataPlace() {
				return sync.getRtz();
			}
		};
	}

	public ActivePushStg create(ActivePush handshake) {
		throw new NotImplementedException();
	}


	private interface ActiveSyncWithRtz
	{
		public ActiveSyncStg getActiveSync();
		public StgPlace getRtz();
	}


	private ActiveSyncWithRtz createActiveSyncWithRtz(Handshake handshake) {
		final StgPlace ready = builder.buildPlace(1);
		final StgPlace activated = builder.buildPlace();
		final StgPlace rtz = builder.buildPlace();
		final StgPlace rtzRemote = builder.buildPlace();

		StgSignal rq = builder.buildSignal(new SignalId(handshake, "rq"), true);
		StgSignal ac = builder.buildSignal(new SignalId(handshake, "ac"), false);

		final StgTransition rqP = rq.getPlus();
		final StgTransition rqM = rq.getMinus();
		final StgTransition acP = ac.getPlus();
		final StgTransition acM = ac.getMinus();

		builder.addConnection(ready, rqP);
		builder.addConnection(rqP, activated);
		builder.addConnection(activated, acP);
		builder.addConnection(rtz, rqM);
		builder.addConnection(rqM, rtzRemote);
		builder.addConnection(rtzRemote, acM);
		builder.addConnection(acM, ready);

		return new ActiveSyncWithRtz()
		{
			public ActiveSyncStg getActiveSync() {
				return new ActiveSyncStg()
				{
					public StgTransition getActivator() {
						return rqP;
					}
					public TransitionOutput getDeactivationNotificator() {
						return acP;
					}
				};
			}
			public StgPlace getRtz() {
				return rtz;
			}
		};
	}
	public ActiveSyncStg create(ActiveSync handshake) {
		ActiveSyncWithRtz withRtz = createActiveSyncWithRtz(handshake);
		ActiveSyncStg result = withRtz.getActiveSync();
		builder.addConnection(result.getDeactivationNotificator(), withRtz.getRtz());
		return result;
	}

	public PassivePullStg create(PassivePull handshake) {
		// TODO Auto-generated method stub
		throw new NotImplementedException();
	}

	public PassivePushStg create(PassivePush handshake) {
		// TODO Auto-generated method stub
		throw new NotImplementedException();
	}

	public PassiveSyncStg create(PassiveSync handshake) {

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

		return new PassiveSyncStg()
		{
			public TransitionOutput getActivationNotificator() {
				return rqP;
			}

			public StgTransition getDeactivator() {
				return acP;
			}
		};
	}

	public StgBuilder getStgBuilder() {
		return builder;
	}
}
