package org.workcraft.plugins.stg;

import org.workcraft.Framework;
import org.workcraft.gui.workspace.Path;
import org.workcraft.util.Func;
import org.workcraft.workspace.WorkspaceEntry;

public class STGWorkspaceFilter implements Func<Path<String>, Boolean> {
	private final Framework framework;

	public STGWorkspaceFilter(Framework framework) {
		this.framework = framework;
	}

	@Override
	public Boolean eval(Path<String> arg) {
		WorkspaceEntry entry = framework.getWorkspace().getOpenFile(arg);

		if (entry != null && entry.getModelEntry() != null && entry.getModelEntry().getMathModel() instanceof STGModel)
			return true;
		if (arg.getNode().endsWith(".g"))
			return true;
		return false;
	}
}
