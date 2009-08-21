package org.workcraft.plugins.balsa.stg;

import java.util.Map;

import org.workcraft.plugins.balsa.components.Case;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveSyncStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgHandshake;
import org.workcraft.plugins.balsa.stgbuilder.SignalId;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;
import org.workcraft.plugins.balsa.stgbuilder.StgSignal;

public class Case_NoDataPath_OneBit extends ComponentStgBuilder<Case> {

	public void buildStg(Case component, Map<String, StgHandshake> handshakes, StgBuilder builder)
	{
		//if(component.getInputWidth()!=1)
		//	throw new RuntimeException("Only input width of 1 is supported");

		PassivePushStg in = (PassivePushStg)handshakes.get("inp");

		StgPlace guardChangeAllowed = builder.buildPlace(1);

		StgSignal dataSignal = builder.buildSignal(new SignalId(component, "dp"), false);
		final StgPlace dataOne = builder.buildPlace();
		final StgPlace dataZero = builder.buildPlace(1);
		builder.addConnection(dataOne, dataSignal.getMinus());
		builder.addConnection(dataSignal.getMinus(), dataZero);
		builder.addConnection(dataZero, dataSignal.getPlus());
		builder.addConnection(dataSignal.getPlus(), dataOne);

		builder.addConnection(in.getDataReleaser(), guardChangeAllowed);
		builder.addConnection(guardChangeAllowed, in.getActivationNotificator());
		builder.addReadArc(guardChangeAllowed, dataSignal.getMinus());
		builder.addReadArc(guardChangeAllowed, dataSignal.getPlus());

		StgPlace activationFinished = builder.buildPlace();

		StgPlace activated = builder.buildPlace();
		builder.addConnection(in.getActivationNotificator(), activated);

		for(int i=0;i<component.getOutputCount();i++)
		{
			ActiveSyncStg activateOut = (ActiveSyncStg)handshakes.get("activateOut"+i);

			builder.addConnection(activated, activateOut.getActivator());
			if(i == 1)
				builder.addReadArc(dataOne, activateOut.getActivator());
			else
				builder.addReadArc(dataZero, activateOut.getActivator());

			builder.addConnection(activateOut.getDeactivationNotificator(), activationFinished);
		}

		builder.addConnection(activationFinished, in.getDeactivator());
	}
}
