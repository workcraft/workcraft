package org.workcraft.plugins.cpog.optimisation;

import java.util.ArrayList;
import java.util.List;

import static org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations.*;

class ThermometerNumberProvider implements NumberProvider<ThermometerBooleanFormula>
{
	private final List<BooleanFormula> rho = new ArrayList<BooleanFormula>();

	public ThermometerNumberProvider()
	{
	}

	@Override
	public ThermometerBooleanFormula generate(String varPrefix, int range) {
		List<BooleanVariable> vars = new ArrayList<BooleanVariable>();
		for(int i=0;i<range-1;i++)
			vars.add(new FV(varPrefix + "sel"+i));

		for(int i=0;i<range-2;i++)
			rho.add(imply(vars.get(i+1), vars.get(i)));

		return new ThermometerBooleanFormula(vars);
	}

	@Override
	public BooleanFormula select(BooleanFormula[] vars,
			ThermometerBooleanFormula number) {
		List<BooleanFormula> conditions = new ArrayList<BooleanFormula>();

		List<BooleanVariable> digits = number.getVars();
		int N = digits.size();
		if(N+1 != vars.length)
			throw new RuntimeException("Lengths do not match");
		if(N==0)
			return vars[0];

		conditions.add(imply(not(digits.get(0)), vars[0]));
		conditions.add(imply(digits.get(N-1), vars[N]));
		for(int i=0;i<N-1;i++)
		{
			conditions.add(imply(and(digits.get(i), not(digits.get(i+1))), vars[i+1]));
		}

		return and(conditions);
	}

	@Override
	public BooleanFormula getConstraints() {
		return and(rho);
	}

	public BooleanFormula lessOrEquals(ThermometerBooleanFormula a, ThermometerBooleanFormula b) {
		List<BooleanFormula> conditions = new ArrayList<BooleanFormula>();
		List<BooleanVariable> aVars = a.getVars();
		List<BooleanVariable> bVars = b.getVars();
		for(int i=0;i<aVars.size();i++)
			conditions.add(imply(aVars.get(i), bVars.get(i)));
		return and(conditions);
	}

	@Override
	public BooleanFormula less(ThermometerBooleanFormula a, ThermometerBooleanFormula b) {
		return not(lessOrEquals(b, a));
	}

}
