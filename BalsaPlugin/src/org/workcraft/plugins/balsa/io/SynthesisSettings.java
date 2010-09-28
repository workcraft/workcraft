package org.workcraft.plugins.balsa.io;

public class SynthesisSettings {

	public enum DummyContractionMode {
		NONE,
		DESIJ,
		PETRIFY;

		public String toString()
		{
			switch (this) {
			case NONE:
				return "None";
			case DESIJ:
				return "DesiJ";
			case PETRIFY:
				return "Petrify";
			}
			return "?";
		}
	}

	public enum SynthesisTool {
		MPSAT,
		PETRIFY;

		public String toString()
		{
			switch (this) {
			case MPSAT:
				return "MPSat";
			case PETRIFY:
				return "Petrify";
			}
			return "?";
		}
	}

	private SynthesisTool synthesisTool = SynthesisTool.MPSAT;
	private DummyContractionMode dummyContractionMode = DummyContractionMode.DESIJ;

	public SynthesisTool getSynthesisTool() {
		return synthesisTool;
	}
	public DummyContractionMode getDummyContractionMode() {
		return dummyContractionMode;
	}

	public SynthesisSettings(SynthesisTool synthesisTool, DummyContractionMode dummyContractionMode) {
		this.synthesisTool = synthesisTool;
		this.dummyContractionMode = dummyContractionMode;
	}

	public SynthesisSettings() {
	}
}
