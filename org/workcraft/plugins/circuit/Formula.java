package org.workcraft.plugins.circuit;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;

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
