package org.workcraft.plugins.verification.tools;

import java.io.File;

import org.workcraft.Framework;
import org.workcraft.plugins.shared.MpsatSettings;
import org.workcraft.plugins.shared.MpsatSettingsSerialiser;
import org.workcraft.plugins.shared.presets.Preset;
import org.workcraft.plugins.shared.presets.PresetManager;

public abstract class PresetMpsatChecker extends AbstractMpsatChecker {

	public PresetMpsatChecker(Framework framework) {
		super(framework);
	}

	protected abstract String getPresetName();

	@Override
	protected MpsatSettings getSettings() {
		PresetManager<MpsatSettings> pmgr = new PresetManager<MpsatSettings>(new File("config/mpsat_settings.xml"), new MpsatSettingsSerialiser());
		String presetName = getPresetName();
		Preset<MpsatSettings> preset = pmgr.find(presetName);

		if (preset == null)
			throw new RuntimeException ("Built-in MPSat preset \"" + presetName + "\" not found.");

		return preset.getSettings();
	}
}
