package org.workcraft.plugins.balsa.stg;

import java.util.Map;

import org.workcraft.plugins.balsa.components.While;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActivePullStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveProcess;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassiveProcess;
import org.workcraft.plugins.balsa.handshakestgbuilder.Process;
import org.workcraft.plugins.balsa.stgbuilder.SignalId;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.OutputPlace;
import org.workcraft.plugins.balsa.stgbuilder.StgSignal;
import org.workcraft.plugins.balsa.stgbuilder.OutputEvent;

public class WhileStgBuilder_NoDataPath extends ComponentStgBuilder<While> {
	static interface WhileStgHandshakes
	{
		public ActivePullStg getGuard();
		public PassiveProcess getActivate();
		public ActiveProcess getActivateOut();
	}

	class WhileStgHandshakesFromCollection implements WhileStgHandshakes
	{
		private final Map<String, Process> map;

		public WhileStgHandshakesFromCollection(Map<String, Process> map)
		{
			this.map = map;
		}

		public PassiveProcess getActivate() {
			return (PassiveProcess)map.get("activate");
		}

		public ActiveProcess getActivateOut() {
			return (ActiveProcess)map.get("activateOut");
		}

		public ActivePullStg getGuard() {
			return (ActivePullStg)map.get("guard");
		}
	}

	static class WhileInternalStgBuilder
	{
		public static void buildStg(While component, WhileStgHandshakes handshakes, StgBuilder builder)
		{
			OutputPlace activated = builder.buildPlace();
			OutputPlace dataReady = builder.buildPlace();

			PassiveProcess activate = handshakes.getActivate();
			ActiveProcess activateOut = handshakes.getActivateOut();
			ActivePullStg guard = handshakes.getGuard();

			OutputPlace guardChangeAllowed = builder.buildPlace(1);

			StgSignal guardSignal = builder.buildSignal(new SignalId(component, "dp"), false);
			final OutputPlace guardOne = builder.buildPlace();
			final OutputPlace guardZero = builder.buildPlace(1);
			builder.connect(guardOne, guardSignal.getMinus());
			builder.connect(guardSignal.getMinus(), guardZero);
			builder.connect(guardZero, guardSignal.getPlus());
			builder.connect(guardSignal.getPlus(), guardOne);

			builder.connect(guard.dataRelease(), guardChangeAllowed);
			//TODO: Move environment specification somewhere else
			builder.connect(guardChangeAllowed, (OutputEvent)guard.done());
			builder.addReadArc(guardChangeAllowed, guardSignal.getMinus());
			builder.addReadArc(guardChangeAllowed, guardSignal.getPlus());

			// Call guard
			builder.connect(activate.go(), activated);
			builder.connect(activated, guard.go());
			builder.connect(guard.done(), dataReady);

			// Activate and repeatedly call guard
			builder.connect(dataReady, activateOut.go());
			builder.addReadArc(guardOne, activateOut.go());
			builder.connect(activateOut.done(), activated);

			// Return
			builder.connect(dataReady, activate.done());
			builder.addReadArc(guardZero, activate.done());

			OutputEvent dataRelease = guard.dataRelease();

			OutputPlace releaseAllowed = builder.buildPlace();
			builder.connect(activateOut.go(), releaseAllowed);
			builder.connect(activate.done(), releaseAllowed);
			builder.connect(releaseAllowed, dataRelease);
		}
	}

	public void buildStg(While component, Map<String, Process> handshakes, StgBuilder builder)
	{
		WhileInternalStgBuilder.buildStg(component, new WhileStgHandshakesFromCollection(handshakes), builder);
	}
}
