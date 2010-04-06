package org.workcraft.plugins.cpog;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Encoding
{
	private Map<Variable, VariableState> states = new TreeMap<Variable, VariableState>();

	public Map<Variable, VariableState> getStates()
	{
		return Collections.unmodifiableMap(states);
	}

	public void setState(Variable variable, VariableState state)
	{
		states.put(variable, state);
	}

	public String toString()
	{
		String result = "";
		for(Variable var : states.keySet()) result += var.getState().value;
		return result;
	}
}
