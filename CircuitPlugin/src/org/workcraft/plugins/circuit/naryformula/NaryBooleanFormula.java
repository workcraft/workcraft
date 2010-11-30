package org.workcraft.plugins.circuit.naryformula;

public interface NaryBooleanFormula {
	<T> T accept(NaryBooleanFormulaVisitor<T> visitor);
}
