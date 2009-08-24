package org.workcraft.plugins.serialisation;

import java.util.UUID;

import org.workcraft.framework.serialisation.Exporter;
import org.workcraft.framework.serialisation.Format;
import org.workcraft.plugins.balsa.protocols.TwoPhaseProtocol;

public class BalsaToStgExporter_TwoPhase extends BalsaToStgExporter implements Exporter {
	public BalsaToStgExporter_TwoPhase()
	{
		super(new TwoPhaseProtocol(), "two-phase");
	}

	public UUID getFormatUUID() {
		return Format.STG;
	}
}
