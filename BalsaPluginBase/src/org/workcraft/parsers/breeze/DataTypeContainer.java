package org.workcraft.parsers.breeze;

import org.workcraft.parsers.breeze.expressions.Expression;

public interface DataTypeContainer {
	Expression<Integer> getType(String name);
	void registerType(String name, Expression<Integer> type);
}
