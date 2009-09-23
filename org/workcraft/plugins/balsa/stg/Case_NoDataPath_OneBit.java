package org.workcraft.plugins.balsa.stg;

import java.util.Map;

import org.workcraft.plugins.balsa.components.Case;
import org.workcraft.plugins.balsa.handshakestgbuilder.ActiveProcess;
import org.workcraft.plugins.balsa.handshakestgbuilder.PassivePushStg;
import org.workcraft.plugins.balsa.handshakestgbuilder.Process;
import org.workcraft.plugins.balsa.stgbuilder.SignalId;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.OutputPlace;
import org.workcraft.plugins.balsa.stgbuilder.StgSignal;
import org.workcraft.plugins.balsa.stgbuilder.OutputEvent;

public class Case_NoDataPath_OneBit extends ComponentStgBuilder<Case> {

	public void buildStg(Case component, Map<String, Process> handshakes, StgBuilder builder)
	{
		//if(component.getInputWidth()!=1)
		//	throw new RuntimeException("Only input width of 1 is supported");

		PassivePushStg in = (PassivePushStg)handshakes.get("inp");

		OutputPlace guardChangeAllowed = builder.buildPlace(1);

		StgSignal dataSignal = builder.buildSignal(new SignalId(component, "dp"), false);
		final OutputPlace dataOne = builder.buildPlace();
		final OutputPlace dataZero = builder.buildPlace(1);
		builder.connect(dataOne, dataSignal.getMinus());
		builder.connect(dataSignal.getMinus(), dataZero);
		builder.connect(dataZero, dataSignal.getPlus());
		builder.connect(dataSignal.getPlus(), dataOne);

		builder.connect(in.dataRelease(), guardChangeAllowed);
		//TODO: Externalise the enviromnent specification
		builder.connect(guardChangeAllowed, (OutputEvent)in.go());
		builder.addReadArc(guardChangeAllowed, dataSignal.getMinus());
		builder.addReadArc(guardChangeAllowed, dataSignal.getPlus());

		OutputPlace activationFinished = builder.buildPlace();

		OutputPlace activated = builder.buildPlace();
		builder.connect(in.go(), activated);

		for(int i=0;i<component.getOutputCount();i++)
		{
			ActiveProcess activateOut = (ActiveProcess)handshakes.get("activateOut"+i);

			builder.connect(activated, activateOut.go());
			if(i == 1)
				builder.addReadArc(dataOne, activateOut.go());
			else
				builder.addReadArc(dataZero, activateOut.go());

			builder.connect(activateOut.done(), activationFinished);
		}

		builder.connect(activationFinished, in.dataRelease());
	}
}
