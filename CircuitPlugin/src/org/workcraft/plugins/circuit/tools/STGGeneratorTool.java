package org.workcraft.plugins.circuit.tools;

import org.workcraft.Tool;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.stg.STGModelDescriptor;
import org.workcraft.plugins.stg.VisualSTG;
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
		return we.getModelEntry().getMathModel() instanceof Circuit;
	}

	@Override
	public void run(WorkspaceEntry we) {
		VisualCircuit circuit = (VisualCircuit)we.getModelEntry().getVisualModel();
		VisualSTG vstg = STGGenerator.generate(circuit);
		ws.add(we.getWorkspacePath().getParent(), we.getWorkspacePath().getNode(),
				new ModelEntry(new STGModelDescriptor(), vstg),	false);
	}

}
