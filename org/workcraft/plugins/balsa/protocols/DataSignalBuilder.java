package org.workcraft.plugins.balsa.protocols;

import org.workcraft.plugins.balsa.handshakebuilder.DataHandshake;
import org.workcraft.plugins.balsa.stgbuilder.SignalId;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.OutputPlace;
import org.workcraft.plugins.balsa.stgbuilder.StgSignal;

class DataSignalBuilder {

	public static InputDataSignal[] buildInputDataSignals(DataHandshake handshake,
			final StgBuilder builder) {
		final InputDataSignal [] signals = new InputDataSignal [handshake.getWidth()];

		for(int i=0;i<handshake.getWidth();i++)
		{
			signals[i] = new InputDataSignal();
			OutputPlace d0 = builder.buildPlace(1);
			OutputPlace d1 = builder.buildPlace();
			signals[i].p0 = d0;
			signals[i].p1 = d1;
			StgSignal dataSignal = builder.buildSignal(new SignalId(handshake, "data"+i), false);
			builder.connect(d0, dataSignal.getPlus());
			builder.connect(dataSignal.getPlus(), d1);
			builder.connect(d1, dataSignal.getMinus());
			builder.connect(dataSignal.getMinus(), d0);
		}
		return signals;
	}
}
