package org.workcraft.plugins.sdfs.tools;

import org.workcraft.Tool;
import org.workcraft.plugins.sdfs.SDFS;
import org.workcraft.plugins.sdfs.VisualSDFS;
import org.workcraft.plugins.sdfs.stg.STGGenerator;
import org.workcraft.plugins.stg.STGModelDescriptor;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class STGGeneratorTool implements Tool {

	private final Workspace ws;

	public STGGeneratorTool(Workspace ws)
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
		return we.getModelEntry().getMathModel() instanceof SDFS;
	}

	@Override
	public void run(WorkspaceEntry we) {
		VisualSDFS sdfs = (VisualSDFS)we.getModelEntry().getVisualModel();
		STGGenerator generator = new STGGenerator(sdfs);
		ws.add(we.getWorkspacePath().getParent(), we.getWorkspacePath().getNode(),
				new ModelEntry(new STGModelDescriptor(), generator.getSTG()),	false);
	}
}
