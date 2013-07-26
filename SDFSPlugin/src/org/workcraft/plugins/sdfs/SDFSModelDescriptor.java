package org.workcraft.plugins.sdfs;

import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;

public class SDFSModelDescriptor implements ModelDescriptor {
	@Override
	public String getDisplayName() {
		return "Static Data Flow Structures";
	}

	@Override
	public MathModel createMathModel() {
		return new SDFS();
	}

	@Override
	public VisualModelDescriptor getVisualModelDescriptor() {
		return new VisualSDFSModelDescriptor();
	}
}
