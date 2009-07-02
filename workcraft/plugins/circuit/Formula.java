package org.workcraft.plugins.circuit;

import org.workcraft.dom.DisplayName;
import org.workcraft.dom.VisualClass;

@DisplayName("Formula")
@VisualClass("org.workcraft.plugins.circuit.VisualFormula")

public class Formula extends CircuitComponent {
	private String formula="";

	public void setFormula(String f) {
		formula = f;
	}

	public String getFormula() {
		return formula;
	}
}
