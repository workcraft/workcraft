package org.workcraft.plugins.stg.tools;

import java.util.Set;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.workspace.WorkspaceEntry;

public class SignalMirrorTool implements Tool {
	private final Framework framework;

	public SignalMirrorTool(Framework framework) {
		this.framework = framework;
	}

	@Override
	public String getDisplayName() {
		return "Mirror signals";
	}

	@Override
	public String getSection() {
		return "Transformations";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return we.getModelEntry().getMathModel() instanceof STG;
	}

	@Override
	public void run(WorkspaceEntry we) {
		final STG stg = (STG)we.getModelEntry().getMathModel();
		we.saveMemento();
		Set<String> inputSignals = stg.getSignalReferences(Type.INPUT);
		Set<String> outputSignals = stg.getSignalReferences(Type.OUTPUT);
		for (String signalName: inputSignals) {
			stg.setSignalType(signalName, Type.OUTPUT);
		}
		for (String signalName: outputSignals) {
			stg.setSignalType(signalName, Type.INPUT);
		}
	}

}
