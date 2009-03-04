package org.workcraft.plugins.balsa.components;

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
import org.workcraft.plugins.balsa.stgbuilder.StgTransition;
import org.workcraft.plugins.balsa.stgbuilder.TransitionOutput;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class FourPhaseProtocol implements HandshakeStgBuilder
{
	private final StgBuilder builder;

	public FourPhaseProtocol (StgBuilder builder)
	{
		this.builder = builder;
	}

	@Override
	public ActivePullStg createActivePull(int width) {
		final ActiveSyncWithRtz sync = createActiveSyncWithRtz();

		final StgTransition [] dataP = new StgTransition [width];
		final StgTransition [] dataM = new StgTransition [width];

		final StgPlace [] data0 = new StgPlace [width];
		final StgPlace [] data1 = new StgPlace [width];

		for(int i=0;i<width;i++)
		{
			data0[i] = builder.buildPlace(1);
			data1[i] = builder.buildPlace();
			dataP[i] = builder.buildTransition();
			dataM[i] = builder.buildTransition();
			builder.addConnection(data0[i], dataP[i]);
			builder.addConnection(dataP[i], data1[i]);
			builder.addConnection(data1[i], dataM[i]);
			builder.addConnection(dataM[i], data0[i]);
		}

		return new ActivePullStg()
		{
			@Override
			public StgTransition getActivator() {
				return sync.getActiveSync().getActivator();
			}
			@Override
			public ReadablePlace getData(int index, boolean value) {
				return value ? data1[index] : data0[index];
			}

			@Override
			public TransitionOutput getDeactivationNotificator() {
				return sync.getActiveSync().getDeactivationNotificator();
			}
			@Override
			public StgPlace getReleaseDataPlace() {
				return sync.getRtz();
			}
		};
	}

	@Override
	public ActivePushStg createActivePush(int width) {
		throw new NotImplementedException();
	}


	private interface ActiveSyncWithRtz
	{
		public ActiveSyncStg getActiveSync();
		public StgPlace getRtz();
	}


	private ActiveSyncWithRtz createActiveSyncWithRtz() {
		final StgPlace ready = builder.buildPlace(1);
		final StgPlace activated = builder.buildPlace();
		final StgPlace rtz = builder.buildPlace();
		final StgPlace rtzRemote = builder.buildPlace();

		final StgTransition rqP = builder.buildTransition();
		final StgTransition rqM = builder.buildTransition();
		final StgTransition acP = builder.buildTransition();
		final StgTransition acM = builder.buildTransition();

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
					@Override
					public StgTransition getActivator() {
						return rqP;
					}
					@Override
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
	@Override
	public ActiveSyncStg createActiveSync() {
		ActiveSyncWithRtz withRtz = createActiveSyncWithRtz();
		ActiveSyncStg result = withRtz.getActiveSync();
		builder.addConnection(result.getDeactivationNotificator(), withRtz.getRtz());
		return result;
	}

	@Override
	public PassivePullStg createPassivePull(int width) {
		// TODO Auto-generated method stub
		throw new NotImplementedException();
	}

	@Override
	public PassivePushStg createPassivePush(int width) {
		// TODO Auto-generated method stub
		throw new NotImplementedException();
	}

	@Override
	public PassiveSyncStg createPassiveSync() {

		final StgPlace ready = builder.buildPlace(1);
		final StgPlace exiting = builder.buildPlace();
		final StgPlace rtz = builder.buildPlace();

		final StgTransition rqP = builder.buildTransition();
		final StgTransition rqM = builder.buildTransition();
		final StgTransition acP = builder.buildTransition();
		final StgTransition acM = builder.buildTransition();

		builder.addConnection(acP, exiting);
		builder.addConnection(exiting, rqM);
		builder.addConnection(rqM, rtz);
		builder.addConnection(rtz, acM);
		builder.addConnection(acM, ready);
		builder.addConnection(ready, rqP);

		return new PassiveSyncStg()
		{
			@Override
			public TransitionOutput getActivationNotificator() {
				return rqP;
			}

			@Override
			public StgTransition getDeactivator() {
				return acP;
			}
		};
	}

	@Override
	public StgBuilder getStgBuilder() {
		return builder;
	}
}
