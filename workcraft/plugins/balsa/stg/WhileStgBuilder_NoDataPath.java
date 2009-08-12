package org.workcraft.plugins.balsa.stg;

import java.util.Map;

import org.workcraft.plugins.balsa.components.While;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSyncStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSyncStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgHandshake;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;
import org.workcraft.plugins.balsa.stgbuilder.StgTransition;

public class WhileStgBuilder_NoDataPath extends ComponentStgBuilder<While> {
	static interface WhileStgHandshakes
	{
		public ActivePullStg getGuard();
		public PassiveSyncStg getActivate();
		public ActiveSyncStg getActivateOut();
	}

	class WhileStgHandshakesFromCollection implements WhileStgHandshakes
	{
		private final Map<String, StgHandshake> map;

		public WhileStgHandshakesFromCollection(Map<String, StgHandshake> map)
		{
			this.map = map;
		}

		public PassiveSyncStg getActivate() {
			return (PassiveSyncStg)map.get("activate");
		}

		public ActiveSyncStg getActivateOut() {
			return (ActiveSyncStg)map.get("activateOut");
		}

		public ActivePullStg getGuard() {
			return (ActivePullStg)map.get("guard");
		}
	}

	static class WhileInternalStgBuilder
	{
		interface WireStg
		{
			public StgPlace getZero();
			public StgPlace getOne();
			public StgTransition getMinus();
			public StgTransition getPlus();
		}

		private static WireStg buildWire(final StgBuilder builder)
		{
			final StgPlace one = builder.buildPlace();
			final StgPlace zero = builder.buildPlace();
			final StgTransition plus = builder.buildTransition();
			final StgTransition minus = builder.buildTransition();
			builder.addConnection(one, minus);
			builder.addConnection(minus, zero);
			builder.addConnection(zero, plus);
			builder.addConnection(plus, one);

			return new WireStg()
			{
				public StgTransition getMinus() { return minus; }
				public StgPlace getOne() { return one; }
				public StgTransition getPlus() { return plus; }
				public StgPlace getZero() { return zero; }
			};
		}

		public static void buildStg(WhileStgHandshakes handshakes, StgBuilder builder)
		{
			StgPlace activated = builder.buildPlace();
			StgPlace dataReady = builder.buildPlace();

			PassiveSyncStg activate = handshakes.getActivate();
			ActiveSyncStg activateOut = handshakes.getActivateOut();
			ActivePullStg guard = handshakes.getGuard();

			WireStg guardWire = buildWire(builder);

			// Call guard
			builder.addConnection(activate.getActivationNotificator(), activated);
			builder.addConnection(activated, guard.getActivator());
			builder.addConnection(guard.getDeactivationNotificator(), dataReady);

			// Activate and repeatedly call guard
			builder.addConnection(dataReady, activateOut.getActivator());
			builder.addReadArc(guardWire.getOne(), activateOut.getActivator());
			builder.addConnection(activateOut.getDeactivationNotificator(), activated);

			// Return
			builder.addConnection(dataReady, activate.getDeactivator());
			builder.addReadArc(guardWire.getZero(), activate.getDeactivator());

			StgPlace dataRelease = guard.getReleaseDataPlace();
			if(dataRelease != null)
			{
				builder.addConnection(activateOut.getActivator(), dataRelease);
				builder.addConnection(activate.getDeactivator(), dataRelease);
			}
		}
	}

	public void buildStg(While component, Map<String, StgHandshake> handshakes, StgBuilder builder)
	{
		WhileInternalStgBuilder.buildStg(new WhileStgHandshakesFromCollection(handshakes), builder);
	}
}
