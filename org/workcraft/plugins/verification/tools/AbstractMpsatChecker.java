package org.workcraft.plugins.verification.tools;

import org.workcraft.Framework;
import org.workcraft.dom.Model;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.plugins.verification.MpsatPreset;
import org.workcraft.plugins.verification.MpsatPresetManager;
import org.workcraft.plugins.verification.tasks.MpsatVerificationTask;
import org.workcraft.serialisation.Format;
import org.workcraft.util.Export;

public abstract class AbstractMpsatChecker {

	public String getSection() {
		return "Verification";
	}

	protected abstract String getPresetName();

	public boolean isApplicableTo(Model model) {
		if (model instanceof STG || model instanceof VisualSTG)
			return true;
		else
			return false;
	}

	public void run(Model model, Framework framework) {
		STG stg;
		if (model instanceof VisualSTG)
			stg = (STG)((VisualSTG)model).getMathModel();
		else
			stg = (STG)model;

		MpsatPresetManager pmgr = new MpsatPresetManager();
		String presetName = getPresetName();
		MpsatPreset preset = pmgr.findPreset(presetName);

		if (preset == null)
			throw new RuntimeException ("Built-in MPSat configuration \"" + presetName + "\" was not found.");

		String[] args = pmgr.getMpsatArguments(preset);

		framework.getTaskManager().queue(new MpsatVerificationTask(model, args, Export.chooseBestExporter(framework.getPluginManager(), stg, Format.STG), framework), "Verification");
	}

}
