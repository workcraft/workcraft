package org.workcraft.parsers.breeze.expressions;

import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.parsers.breeze.ParameterScope;
import org.workcraft.parsers.breeze.expressions.visitors.Visitor;

public class ShiftLeft implements Expression<Integer> {

	private final Expression<Integer> arg;
	private final Expression<Integer> shift;

	public ShiftLeft(Expression<Integer> left, Expression<Integer> right)
	{
		this.arg = left;
		this.shift = right;
	}

	@Override
	public Integer evaluate(ParameterScope parameters) {
		return arg.evaluate(parameters)<<shift.evaluate(parameters);
	}

	@Override
	public <R> R accept(Visitor<R> visitor) {
		throw new NotSupportedException("ShiftLeft is only an internal workcraft expression");
	}

	public static Expression<Integer> create(Expression<Integer> arg, Expression<Integer> shift) {
		return new ShiftLeft(arg, shift);
	}

}
