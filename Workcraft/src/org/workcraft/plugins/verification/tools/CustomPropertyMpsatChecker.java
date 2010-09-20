package org.workcraft.plugins.verification.tools;

import java.io.File;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.plugins.shared.MpsatChainResultHandler;
import org.workcraft.plugins.shared.MpsatSettings;
import org.workcraft.plugins.shared.MpsatSettingsSerialiser;
import org.workcraft.plugins.shared.presets.PresetManager;
import org.workcraft.plugins.shared.tasks.MpsatChainTask;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.plugins.verification.gui.MpsatConfigurationDialog;
import org.workcraft.util.GUI;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class CustomPropertyMpsatChecker implements Tool {

	public CustomPropertyMpsatChecker(Framework framework) {
		this.framework = framework;
	}

	private final Framework framework;

	@Override
	public String getSection() {
		return "Verification";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, STGModel.class);
	}

	@Override
	public void run(WorkspaceEntry we) {
		PresetManager<MpsatSettings> pmgr = new PresetManager<MpsatSettings>(new File("config/mpsat_presets.xml"), new MpsatSettingsSerialiser());
		MpsatConfigurationDialog dialog = new MpsatConfigurationDialog(framework.getMainWindow(), pmgr);
		GUI.centerAndSizeToParent(dialog, framework.getMainWindow());
		dialog.setVisible(true);
		if (dialog.getModalResult() == 1)
		{
			final MpsatChainTask mpsatTask = new MpsatChainTask(we, dialog.getSettings(), framework);
			framework.getTaskManager().queue(mpsatTask, "MPSat tool chain",
					new MpsatChainResultHandler(mpsatTask));
		}
	}

	@Override
	public String getDisplayName() {
		return "Check custom property (punf, MPSat)";
	}
}