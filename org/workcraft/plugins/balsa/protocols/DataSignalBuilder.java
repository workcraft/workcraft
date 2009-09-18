package org.workcraft.plugins.balsa.protocols;

import org.workcraft.plugins.balsa.handshakebuilder.DataHandshake;
import org.workcraft.plugins.balsa.stgbuilder.SignalId;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;
import org.workcraft.plugins.balsa.stgbuilder.StgSignal;

class DataSignalBuilder {

	public static InputDataSignal[] buildInputDataSignals(DataHandshake handshake,
			final StgBuilder builder) {
		final InputDataSignal [] signals = new InputDataSignal [handshake.getWidth()];

		for(int i=0;i<handshake.getWidth();i++)
		{
			signals[i] = new InputDataSignal();
			StgPlace d0 = builder.buildPlace(1);
			StgPlace d1 = builder.buildPlace();
			signals[i].p0 = d0;
			signals[i].p1 = d1;
			StgSignal dataSignal = builder.buildSignal(new SignalId(handshake, "data"+i), false);
			builder.addConnection(d0, dataSignal.getPlus());
			builder.addConnection(dataSignal.getPlus(), d1);
			builder.addConnection(d1, dataSignal.getMinus());
			builder.addConnection(dataSignal.getMinus(), d0);
		}
		return signals;
	}
}
