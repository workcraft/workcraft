package org.workcraft.plugins.interop;

import org.workcraft.plugins.balsa.protocols.FourPhaseProtocol;

public class BalsaToStgExporter_FourPhase extends BalsaToStgExporter {
	public BalsaToStgExporter_FourPhase()
	{
		super(new FourPhaseProtocol(), "four-phase");
	}
}
