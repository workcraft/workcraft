package org.workcraft.plugins.interop;

import org.workcraft.framework.Exporter;
import org.workcraft.plugins.balsa.protocols.FourPhaseProtocol_NoDataPath;

public class BalsaToStgExporter_FourPhase extends BalsaToStgExporter implements Exporter {
	public BalsaToStgExporter_FourPhase()
	{
		super(new FourPhaseProtocol_NoDataPath(), "four-phase (no data path)");
	}
}
