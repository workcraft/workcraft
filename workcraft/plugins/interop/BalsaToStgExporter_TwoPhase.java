package org.workcraft.plugins.interop;

import org.workcraft.framework.Exporter;
import org.workcraft.plugins.balsa.protocols.TwoPhaseProtocol;

public class BalsaToStgExporter_TwoPhase extends BalsaToStgExporter implements Exporter {
	public BalsaToStgExporter_TwoPhase()
	{
		super(new TwoPhaseProtocol(), "two-phase");
	}
}
