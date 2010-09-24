package org.workcraft.plugins.workflow;

import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.math.MathModel;

public class WorkflowModelDescriptor implements ModelDescriptor {

	@Override
	public String getDisplayName() {
		return "Workflow";
	}

	@Override
	public MathModel createMathModel() {
		return new Workflow();
	}
}
