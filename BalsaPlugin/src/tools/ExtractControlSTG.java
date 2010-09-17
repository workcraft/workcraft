package tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.plugins.balsa.BalsaCircuit;
import org.workcraft.util.GUI;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class ExtractControlSTG implements Tool {

	private final Framework framework;

	public ExtractControlSTG(Framework framework) {
		this.framework = framework;
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, BalsaCircuit.class);
	}

	@Override
	public String getSection() {
		return "Synthesis";
	}

	@Override
	public void run(WorkspaceEntry we) {
		ExtractControlSTGDialog dialog = new ExtractControlSTGDialog(framework.getMainWindow());
		GUI.centerAndSizeToParent(dialog, framework.getMainWindow());
		dialog.setVisible(true);
	}

	@Override
	public String getDisplayName() {
		return "Extract control STG";
	}
}
