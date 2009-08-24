package org.workcraft.plugins.serialisation;

import java.util.UUID;

import org.workcraft.framework.serialisation.Exporter;
import org.workcraft.framework.serialisation.Format;
import org.workcraft.plugins.balsa.protocols.FourPhaseProtocol_NoDataPath;

public class BalsaToStgExporter_FourPhase extends BalsaToStgExporter implements Exporter {
	public BalsaToStgExporter_FourPhase()
	{
		super(new FourPhaseProtocol_NoDataPath(), "four-phase (no data path)");
	}

	public UUID getFormatUUID() {
		return Format.STG;
	}
}
