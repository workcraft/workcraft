package org.workcraft.plugins.interop;

import java.util.UUID;

import org.workcraft.interop.Exporter;
import org.workcraft.plugins.balsa.protocols.FourPhaseProtocol_NoDataPath;
import org.workcraft.serialisation.Format;

public class BalsaToStgExporter_FourPhase extends BalsaToStgExporter implements Exporter {
	public BalsaToStgExporter_FourPhase()
	{
		super(new FourPhaseProtocol_NoDataPath(), "four-phase (no data path)");
	}

	public UUID getFormatUUID() {
		return Format.STG;
	}
}
