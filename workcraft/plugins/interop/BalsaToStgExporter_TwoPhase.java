package org.workcraft.plugins.interop;

import org.workcraft.plugins.balsa.protocols.TwoPhaseProtocol;

public class BalsaToStgExporter_TwoPhase extends BalsaToStgExporter {
	public BalsaToStgExporter_TwoPhase()
	{
		super(new TwoPhaseProtocol(), "two-phase");
	}
}
