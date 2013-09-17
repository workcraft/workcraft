package org.workcraft.plugins.dfs.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.dfs.Dfs;
import org.workcraft.plugins.dfs.VisualDfs;
import org.workcraft.plugins.dfs.stg.StgGenerator;
import org.workcraft.plugins.stg.STGModelDescriptor;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class StgGeneratorTool implements Tool {
	private final Framework framework;

	public StgGeneratorTool(Framework framework) {
		this.framework = framework;
	}

	@Override
	public String getDisplayName() {
		return "Generate STG";
	}

	@Override
	public String getSection() {
		return "STG";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return we.getModelEntry().getMathModel() instanceof Dfs;
	}

	@Override
	public void run(WorkspaceEntry we) {
		final VisualDfs dfs = (VisualDfs)we.getModelEntry().getVisualModel();
		final StgGenerator generator = new StgGenerator(dfs);
		final Workspace workspace = framework.getWorkspace();
		final Path<String> directory = we.getWorkspacePath().getParent();
		final String desiredName = we.getWorkspacePath().getNode();
		final ModelEntry me = new ModelEntry(new STGModelDescriptor(), generator.getSTG());
		workspace.add(directory, desiredName, me, false, true);
	}
}
