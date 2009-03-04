package org.workcraft.plugins.balsa.components;

import java.util.HashMap;
import java.util.Map;

import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.handshakebuilder.HandshakeBuilder;
import org.workcraft.plugins.balsa.handshakebuilder.SimpleHandshakeBuilder;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSyncStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.HandshakeStgBuilder;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveSyncStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgHandshake;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;

abstract class HandshakedComponent implements IHandshakedStgComponent
{
	public HandshakedComponent()
	{
	}

	public void buildStg(HandshakeStgBuilder builder)
	{
		buildStgInternal(buildHandshakes(builder), builder.getStgBuilder());
	}

	abstract protected void buildStgInternal(Map<String, StgHandshake> buildHandshakes, StgBuilder stgBuilder);

	private Map<String, StgHandshake> buildHandshakes(HandshakeStgBuilder builder) {
		HashMap<String, StgHandshake> result = new HashMap<String, StgHandshake>();
		Map<String, Handshake> handshakes = getHandshakes();
		for(String key : handshakes.keySet())
		{
			result.put(key, handshakes.get(key).buildStg(builder));
		}
		return result;
	}

	Map<String, Handshake> handshakes;

	public Map<String, Handshake> getHandshakes() {
		return createHandshakes(SimpleHandshakeBuilder.getInstance());
	}

	protected abstract Map<String, Handshake> createHandshakes(HandshakeBuilder builder);
}

public class While extends HandshakedComponent{

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

		@Override
		public PassiveSyncStg getActivate() {
			return (PassiveSyncStg)map.get("activate");
		}

		@Override
		public ActiveSyncStg getActivateOut() {
			return (ActiveSyncStg)map.get("activateOut");
		}

		@Override
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

	protected Map<String, Handshake> createHandshakes(HandshakeBuilder builder)
	{
		HashMap<String, Handshake> result = new HashMap<String, Handshake>();
		result.put("guard", builder.CreateActivePull(1));
		result.put("activate", builder.CreatePassiveSync());
		result.put("activateOut", builder.CreateActiveSync());
		return result;
	}

	public void buildStgInternal(Map<String, StgHandshake> handshakes, StgBuilder builder)
	{
		WhileInternalStgBuilder.buildStg(new WhileStgHandshakesFromCollection(handshakes), builder);
	}
}
