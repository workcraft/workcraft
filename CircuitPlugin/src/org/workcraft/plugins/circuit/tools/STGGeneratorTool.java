package org.workcraft.plugins.circuit.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.stg.STGModelDescriptor;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class STGGeneratorTool implements Tool {
	private final Framework framework;

	public STGGeneratorTool(Framework framework) {
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
		return we.getModelEntry().getMathModel() instanceof Circuit;
	}

	@Override
	public void run(WorkspaceEntry we) {
		final VisualCircuit circuit = (VisualCircuit)we.getModelEntry().getVisualModel();
		final VisualSTG vstg = STGGenerator.generate(circuit);
		final Workspace workspace = framework.getWorkspace();
		final Path<String> directory = we.getWorkspacePath().getParent();
		final String name = we.getWorkspacePath().getNode();
		final ModelEntry me = new ModelEntry(new STGModelDescriptor(), vstg);
		workspace.add(directory, name, me, false, true);
	}

}
