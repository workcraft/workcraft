package org.workcraft.plugins.dfs.tools;

import org.workcraft.Tool;
import org.workcraft.plugins.dfs.Dfs;
import org.workcraft.plugins.dfs.VisualDfs;
import org.workcraft.plugins.dfs.stg.StgGenerator;
import org.workcraft.plugins.stg.STGModelDescriptor;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class StgGeneratorTool implements Tool {

	private final Workspace ws;

	public StgGeneratorTool(Workspace ws)
	{
		this.ws = ws;
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
		VisualDfs dfs = (VisualDfs)we.getModelEntry().getVisualModel();
		StgGenerator generator = new StgGenerator(dfs);
		ws.add(we.getWorkspacePath().getParent(), we.getWorkspacePath().getNode(),
				new ModelEntry(new STGModelDescriptor(), generator.getSTG()),	false);
	}
}
