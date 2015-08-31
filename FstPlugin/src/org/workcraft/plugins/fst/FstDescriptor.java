package org.workcraft.plugins.fst;

import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;

public class FstDescriptor  implements ModelDescriptor {

	@Override
	public String getDisplayName() {
		return "Finite State Transducer";
	}

	@Override
	public MathModel createMathModel() {
		return new Fst();
	}

	@Override
	public VisualModelDescriptor getVisualModelDescriptor() {
		return new VisualFstDescriptor();
	}

}
