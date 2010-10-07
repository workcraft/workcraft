package org.workcraft.plugins.cpog.optimisation.dnf;

import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BooleanVariable;
import org.workcraft.plugins.cpog.optimisation.Literal;
import org.workcraft.plugins.cpog.optimisation.expressions.And;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor;
import org.workcraft.plugins.cpog.optimisation.expressions.Iff;
import org.workcraft.plugins.cpog.optimisation.expressions.Imply;
import org.workcraft.plugins.cpog.optimisation.expressions.Not;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.plugins.cpog.optimisation.expressions.Or;
import org.workcraft.plugins.cpog.optimisation.expressions.Xor;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;

public class DnfGenerator {
	public static Dnf generate(BooleanFormula formula)
	{
		return formula.accept(new BooleanVisitor<Dnf>()
				{
					boolean negation = false;

					@Override
					public Dnf visit(And node) {
						Dnf left = node.getX().accept(this);
						Dnf right = node.getY().accept(this);
						return and(left, right);
					}

					private Dnf and(Dnf left,Dnf right) {
						return negation?addDnf(left,right):multiplyDnf(left, right);
					}

					private Dnf or(Dnf left,Dnf right) {
						return negation?multiplyDnf(left,right):addDnf(left, right);
					}

					@Override
					public Dnf visit(Iff node) {
						Dnf a = node.getX().accept(this);
						Dnf b = node.getY().accept(this);
						negation = !negation;
						Dnf na = node.getX().accept(this);
						Dnf nb = node.getY().accept(this);
						negation = !negation;
						return or(and(a,b), and(na, nb));
					}

					@Override
					public Dnf visit(Xor node) {
						Dnf a = node.getX().accept(this);
						Dnf b = node.getY().accept(this);
						negation = !negation;
						Dnf na = node.getX().accept(this);
						Dnf nb = node.getY().accept(this);
						negation = !negation;
						return or(and(a,nb), and(na, b));
					}

					@Override
					public Dnf visit(Zero node) {
						return new Dnf();
					}

					@Override
					public Dnf visit(One node) {
						return new Dnf(new DnfClause());
					}

					@Override
					public Dnf visit(Not node) {
						negation = !negation;
						try{
						return node.getX().accept(this);
						}
						finally{negation=!negation;}
					}

					@Override
					public Dnf visit(Imply node) {
						negation=!negation;
						Dnf x = node.getX().accept(this);
						negation=!negation;
						Dnf y = node.getY().accept(this);
						return or(x,y);
					}

					@Override
					public Dnf visit(BooleanVariable variable) {
						return new Dnf(new DnfClause(new Literal(variable, negation)));
					}

					@Override
					public Dnf visit(Or node) {
						return or(node.getX().accept(this), node.getY().accept(this));
					}

				});
	}

	private static Dnf addDnf(Dnf left, Dnf right) {
		Dnf result = new Dnf(left.getClauses());
		result.add(right);
		return result;
	}

	private static Dnf multiplyDnf(Dnf left, Dnf right) {
		Dnf result = new Dnf();
		for(DnfClause leftClause : left.getClauses())
			for(DnfClause rightClause : right.getClauses())
			{
				DnfClause newClause = new DnfClause();
				newClause.add(leftClause.getLiterals());
				newClause.add(rightClause.getLiterals());
				result.add(newClause);
			}
		return result;
	}
}
