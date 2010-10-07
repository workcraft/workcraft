package org.workcraft.plugins.cpog.optimisation.dnf;

import java.util.List;

import org.workcraft.plugins.cpog.optimisation.Clause;
import org.workcraft.plugins.cpog.optimisation.Literal;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor;

public class DnfClause extends Clause {

	public DnfClause()
	{
	}

	public DnfClause(Literal... literals)
	{
		super(literals);
	}

	public DnfClause(List<Literal> literals) {
		super(literals);
	}

	@Override
	public <T> T accept(BooleanVisitor<T> visitor) {
		return BooleanOperations.and(getLiterals()).accept(visitor);
	}
}
