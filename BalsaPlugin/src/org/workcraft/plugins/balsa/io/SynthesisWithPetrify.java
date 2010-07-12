package org.workcraft.plugins.balsa.io;

import org.workcraft.plugins.balsa.io.BalsaExportConfig.SynthesisTool;

public class SynthesisWithPetrify extends BalsaToGatesExporter{

	@Override
	protected BalsaExportConfig getConfig()
	{
		BalsaExportConfig def = BalsaExportConfig.DEFAULT;
		return new BalsaExportConfig(SynthesisTool.PETRIFY, def.dummyContractionMode(), def.compositionMode());
	}

	public String getDescription() {
		return ".eqn (Synthesise complex gates with PETRIFY)";
	}

}
