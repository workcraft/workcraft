package org.workcraft;

import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.plugins.balsa.BalsaCircuit;

public class BalsaModelDescriptor implements ModelDescriptor {
	@Override
	public String getDisplayName() {
		return "Breeze circuit";
	}

	@Override
	public MathModel createMathModel() {
		return new BalsaCircuit();
	}

	@Override
	public VisualModelDescriptor getVisualModelDescriptor() {
		return new BalsaVisualModelDescriptor();
	}
}
