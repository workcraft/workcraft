package org.workcraft.plugins.interop;

import java.util.UUID;

import org.workcraft.interop.Exporter;
import org.workcraft.plugins.balsa.protocols.TwoPhaseProtocol;
import org.workcraft.serialisation.Format;

public class BalsaToStgExporter_TwoPhase extends BalsaToStgExporter implements Exporter {
	public BalsaToStgExporter_TwoPhase()
	{
		super(new TwoPhaseProtocol(), "two-phase");
	}

	public UUID getFormatUUID() {
		return Format.STG;
	}
}
