package org.workcraft.plugins.cpog.optimisation;

interface NumberProvider<Formula>
{
	public Formula generate(String varPrefix, int range);
	public BooleanFormula select(BooleanFormula[] vars, Formula number);
	public BooleanFormula getConstraints();
	public BooleanFormula less(Formula a, Formula b);
}
