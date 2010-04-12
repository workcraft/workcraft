package org.workcraft.plugins.cpog.optimisation;

import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor;

public class FV implements BooleanVariable, Comparable<FV> {

	private final String label;

	public FV(String label) {
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
	public int compareTo(FV var) {
		return -var.getLabel().compareTo(getLabel());
	}

}
