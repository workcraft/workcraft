package org.workcraft.plugins.cpog.optimisation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.workcraft.plugins.cpog.optimisation.booleanvisitors.BooleanReplacer;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations;
import org.workcraft.plugins.cpog.optimisation.expressions.One;

import static org.workcraft.plugins.cpog.optimisation.CnfOperations.*;

class CnfGeneratingOneHotNumberProvider implements NumberProvider<OneHotIntBooleanFormula>
{
	private final List<CnfClause> rho = new ArrayList<CnfClause>();

	public CnfGeneratingOneHotNumberProvider()
	{
	}

	public OneHotIntBooleanFormula generate(String varPrefix, int range) {
		List<BooleanVariable> vars = new ArrayList<BooleanVariable>();
		for(int i=0;i<range;i++)
			vars.add(new FreeVariable(varPrefix + "sel"+i));

		List<CnfLiteral> literals = new ArrayList<CnfLiteral>();
		List<CnfLiteral> sorted = new ArrayList<CnfLiteral>();
		for(int i=0;i<range;i++)
		{
			literals.add(new CnfLiteral(vars.get(i)));
			sorted.add(new CnfLiteral(varPrefix + "sorted"+i));
		}

		Cnf sorting = CnfSorter.sortRound(sorted, literals);

		if(false)
		{
			for(int i=0;i<range;i++)
				for(int j=i+1;j<range;j++)
					rho.add(or(not(vars.get(i)), not(vars.get(j))));

			rho.add(or(vars));
		}
		else
		{
			for(int i=0;i<range-1;i++)
				rho.add(or(not(sorted.get(i))));
			rho.add(or(sorted.get(range-1)));
			rho.addAll(sorting.getClauses());
		}

		return new OneHotIntBooleanFormula(vars);
	}

	public static List<CnfClause> select(CnfLiteral[] vars, OneHotIntBooleanFormula number, boolean inverse) {
		List<CnfClause> conditions = new ArrayList<CnfClause>();

		if(number.getRange() != vars.length)
			throw new RuntimeException("Lengths do not match");

		for(int i=0;i<vars.length;i++)
			conditions.add(or(not(number.get(i)), inverse?not(vars[i]):vars[i]));

		return conditions;
	}

	public static List<CnfClause> select(CnfLiteral result, CnfLiteral[] vars, OneHotIntBooleanFormula code) {
		List<CnfClause> conditions = new ArrayList<CnfClause>();

		if(code.getRange() != vars.length)
			throw new RuntimeException("Lengths do not match");

		CnfLiteral notResult = not(result);
		for(int i=0;i<vars.length;i++)
		{
			conditions.add(or(notResult, not(code.get(i)), vars[i]));
			conditions.add(or(result, not(code.get(i)), not(vars[i])));
		}

		return conditions;
	}

	public List<CnfClause> getConstraintClauses() {
		return rho;
	}

	public BooleanFormula less(OneHotIntBooleanFormula a,
			OneHotIntBooleanFormula b) {
		return One.instance();
	}

	@Override
	public BooleanFormula select(BooleanFormula[] vars, OneHotIntBooleanFormula number) {
		List<BooleanVariable> params = new ArrayList<BooleanVariable>();
		CnfLiteral []literals = new CnfLiteral[vars.length];

		for(int i=0;i<vars.length;i++)
		{
			BooleanVariable var = new FreeVariable("param"+i);
			params.add(var);
			literals[i] = new CnfLiteral(var);
		}

		List<CnfClause> result = select(literals, number, false);

		Cnf cnf = new Cnf(result);
		BooleanFormula res = BooleanReplacer.replace(cnf, params, Arrays.asList(vars));
		return res;
	}

	@Override
	public BooleanFormula getConstraints() {
		return BooleanOperations.and(getConstraintClauses());
	}

}
