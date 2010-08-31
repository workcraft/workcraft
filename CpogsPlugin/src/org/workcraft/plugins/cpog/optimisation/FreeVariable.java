package org.workcraft.plugins.cpog.optimisation;

import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor;

public class FreeVariable implements BooleanVariable, Comparable<FreeVariable> {

	private final String label;

	public FreeVariable(String label) {
		this.label = label;
	}

	@Override
	public <T> T accept(BooleanVisitor<T> visitor) {
		return visitor.visit(this);
	}

	public String getLabel() {
		return label;
	}

	@Override
	public int compareTo(FreeVariable var) {
		return -var.getLabel().compareTo(getLabel());
	}

}
