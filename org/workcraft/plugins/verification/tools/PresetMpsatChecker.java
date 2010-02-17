package org.workcraft.plugins.verification.tools;

import org.workcraft.plugins.verification.MpsatPresetManager;
import org.workcraft.plugins.verification.MpsatSettings;

public abstract class PresetMpsatChecker extends AbstractMpsatChecker {
	protected abstract String getPresetName();

	@Override
	protected MpsatSettings getSettings() {
		MpsatPresetManager pmgr = new MpsatPresetManager();
		String presetName = getPresetName();
		MpsatSettings settings = pmgr.findPreset(presetName);

		if (settings == null)
			throw new RuntimeException ("Built-in MPSat preset \"" + presetName + "\" not found.");

		return settings;
	}
}
