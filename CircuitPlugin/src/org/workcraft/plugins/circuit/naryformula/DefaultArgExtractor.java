package org.workcraft.plugins.circuit.naryformula;

import java.util.Arrays;
import java.util.List;

import org.workcraft.plugins.cpog.optimisation.BooleanVariable;

public class DefaultArgExtractor implements NaryBooleanFormulaVisitor<List<NaryBooleanFormula>> {

	private List<NaryBooleanFormula> defaultList(NaryBooleanFormula f) {
		return Arrays.asList(new NaryBooleanFormula[]{f});
	}

	@Override
	public List<NaryBooleanFormula> visit(BooleanVariable var) {
		return defaultList(Util.getVar(var));
	}

	@Override
	public List<NaryBooleanFormula> visitNot(NaryBooleanFormula not) {
		return defaultList(Util.getNot(not));
	}

	@Override
	public List<NaryBooleanFormula> visitAnd(List<NaryBooleanFormula> args) {
		return defaultList(Util.getAnd(args));
	}

	@Override
	public List<NaryBooleanFormula> visitOr(List<NaryBooleanFormula> args) {
		return defaultList(Util.getOr(args));
	}

	@Override
	public List<NaryBooleanFormula> visitXor(List<NaryBooleanFormula> args) {
		return defaultList(Util.getXor(args));
	}
}
