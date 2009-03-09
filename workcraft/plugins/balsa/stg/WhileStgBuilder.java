package org.workcraft.plugins.balsa.stg;

import java.util.Map;

import org.workcraft.plugins.balsa.components.While;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSyncStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSyncStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgHandshake;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;

public class WhileStgBuilder extends ComponentStgBuilder<While> {
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
		public static void buildStg(WhileStgHandshakes handshakes, StgBuilder builder)
		{
			StgPlace activated = builder.buildPlace();
			StgPlace dataReady = builder.buildPlace();

			PassiveSyncStg activate = handshakes.getActivate();
			ActiveSyncStg activateOut = handshakes.getActivateOut();
			ActivePullStg guard = handshakes.getGuard();

			// Call guard
			builder.addConnection(activate.getActivationNotificator(), activated);
			builder.addConnection(activated, guard.getActivator());
			builder.addConnection(guard.getDeactivationNotificator(), dataReady);

			// Activate and repeatedly call guard
			builder.addConnection(dataReady, activateOut.getActivator());
			builder.addReadArc(guard.getData(0, true), activateOut.getActivator());
			builder.addConnection(activateOut.getDeactivationNotificator(), activated);

			// Return
			builder.addConnection(dataReady, activate.getDeactivator());
			builder.addReadArc(guard.getData(0, false), activate.getDeactivator());

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
